package com.waqikids.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.waqikids.launcher.R
import com.waqikids.launcher.data.api.WaqiApi
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.ui.BlockedActivity
import com.waqikids.launcher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * VPN Service for DNS-based website filtering
 * 
 * Architecture:
 * 1. Intercepts all DNS queries (port 53)
 * 2. Checks domain against local HashSet whitelist (O(1) lookup)
 * 3. Allowed: Forward to upstream DNS (8.8.8.8)
 * 4. Blocked: Return NXDOMAIN response
 * 
 * Performance:
 * - HashSet for O(1) domain lookups
 * - Cached whitelist from DataStore (offline-first)
 * - Push notification triggers immediate sync
 */
@AndroidEntryPoint
class DnsVpnService : VpnService() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var api: WaqiApi
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRunning = AtomicBoolean(false)
    
    // O(1) lookup whitelist - loaded from DataStore
    private val allowedDomains = ConcurrentHashMap.newKeySet<String>()
    
    // Track parent-added domains separately (for notification display)
    private var parentAddedDomainCount = 0
    
    // DNS Statistics
    private var totalQueries = 0L
    private var blockedQueries = 0L
    
    companion object {
        private const val TAG = "DnsVpnService"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "waqikids_vpn"
        
        // Actions
        const val ACTION_START = "com.waqikids.launcher.START_VPN"
        const val ACTION_STOP = "com.waqikids.launcher.STOP_VPN"
        const val ACTION_RELOAD_WHITELIST = "com.waqikids.launcher.RELOAD_WHITELIST"
        
        // Upstream DNS servers
        private val UPSTREAM_DNS = listOf("8.8.8.8", "8.8.4.4")
        private const val DNS_PORT = 53
        private const val VPN_MTU = 1500
        
        // Backend server IP - blocked domains redirect here to show blocked page
        private val BLOCKED_PAGE_IP = byteArrayOf(178.toByte(), 156.toByte(), 160.toByte(), 245.toByte())
        
        // Essential infrastructure domains - ALWAYS allowed even if sync fails
        // These are required for the app to function (Firebase, connectivity checks, etc.)
        private val ESSENTIAL_INFRASTRUCTURE = setOf(
            // Firebase/FCM (push notifications)
            "fcm.googleapis.com",
            "firebase.google.com",
            "firebaseinstallations.googleapis.com",
            "firebaseio.com",
            
            // Google Play Services
            "googleapis.com",
            "google.com",
            "gstatic.com",
            "play.google.com",
            "android.com",
            
            // Connectivity checks
            "connectivitycheck.gstatic.com",
            "clients3.google.com",
            
            // Our backend
            "178.156.160.245",
            "waqikids.com",
            "api.waqikids.com",
            
            // DNS
            "dns.google",
            "cloudflare-dns.com"
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "========== DNS VPN SERVICE CREATED ==========")
        createNotificationChannel()
        loadWhitelistFromCache()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                Log.i(TAG, ">>> ACTION_START received")
                startVpn()
            }
            ACTION_STOP -> {
                Log.i(TAG, ">>> ACTION_STOP received")
                stopVpn()
            }
            ACTION_RELOAD_WHITELIST -> {
                Log.i(TAG, ">>> ACTION_RELOAD_WHITELIST received (push notification triggered)")
                reloadWhitelist()
            }
            null -> {
                Log.w(TAG, ">>> No action provided, starting VPN by default")
                startVpn()
            }
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        stopVpn()
        scope.cancel()
        super.onDestroy()
    }
    
    private fun startVpn() {
        if (isRunning.get()) {
            Log.d(TAG, "VPN already running")
            return
        }
        
        try {
            // Build VPN interface
            val builder = Builder()
                .setSession("WaqiKids DNS Filter")
                .setMtu(VPN_MTU)
                .addAddress("10.0.0.2", 32)
                .addDnsServer("10.0.0.1")  // Fake DNS - we intercept
                .addRoute("10.0.0.1", 32)  // Route only our fake DNS
            
            // Allow our own app to bypass VPN
            builder.addDisallowedApplication(packageName)
            
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                return
            }
            
            isRunning.set(true)
            startForeground(NOTIFICATION_ID, createNotification())
            
            // Start DNS proxy thread
            startDnsProxy()
            
            // Update notification with actual domain count after VPN starts
            updateNotification()
            
            Log.i(TAG, "========================================")
            Log.i(TAG, "VPN STARTED SUCCESSFULLY")
            Log.i(TAG, "Whitelisted domains: ${allowedDomains.size}")
            Log.i(TAG, "First 10 domains: ${allowedDomains.take(10)}")
            Log.i(TAG, "========================================")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN", e)
            stopVpn()
        }
    }
    
    private fun stopVpn() {
        isRunning.set(false)
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.i(TAG, "VPN stopped. Stats: $totalQueries queries, $blockedQueries blocked")
    }
    
    private fun startDnsProxy() {
        scope.launch {
            val vpnFd = vpnInterface ?: return@launch
            val inputStream = FileInputStream(vpnFd.fileDescriptor)
            val outputStream = FileOutputStream(vpnFd.fileDescriptor)
            
            val buffer = ByteArray(VPN_MTU)
            
            while (isRunning.get()) {
                try {
                    val length = inputStream.read(buffer)
                    if (length > 0) {
                        handlePacket(buffer, length, outputStream)
                    }
                } catch (e: Exception) {
                    if (isRunning.get()) {
                        Log.e(TAG, "Error reading packet", e)
                    }
                }
            }
        }
    }
    
    private fun handlePacket(packet: ByteArray, length: Int, outputStream: FileOutputStream) {
        // Parse IP header
        if (length < 20) return
        
        val ipVersion = (packet[0].toInt() shr 4) and 0x0F
        if (ipVersion != 4) return  // Only handle IPv4
        
        val protocol = packet[9].toInt() and 0xFF
        if (protocol != 17) return  // Only handle UDP (17)
        
        val ipHeaderLength = (packet[0].toInt() and 0x0F) * 4
        if (length < ipHeaderLength + 8) return
        
        // Parse UDP header
        val destPort = ((packet[ipHeaderLength + 2].toInt() and 0xFF) shl 8) or
                       (packet[ipHeaderLength + 3].toInt() and 0xFF)
        
        if (destPort != DNS_PORT) return  // Only handle DNS
        
        val udpHeaderLength = 8
        val dnsOffset = ipHeaderLength + udpHeaderLength
        val dnsLength = length - dnsOffset
        
        if (dnsLength < 12) return  // DNS header minimum
        
        // Parse DNS query
        val dnsData = packet.copyOfRange(dnsOffset, length)
        val domain = extractDomainFromDns(dnsData)
        
        if (domain == null) return
        
        totalQueries++
        
        // Check if domain is allowed (O(1) lookup)
        val allowed = isDomainAllowed(domain)
        
        if (allowed) {
            // Forward to upstream DNS
            if (totalQueries % 50 == 1L) {
                Log.d(TAG, "ALLOWED: $domain (total queries: $totalQueries)")
            }
            forwardDnsQuery(packet, length, dnsData, outputStream)
        } else {
            // Block the domain and show blocked page Activity
            blockedQueries++
            Log.w(TAG, "BLOCKED: $domain (blocked: $blockedQueries/$totalQueries) -> showing blocked screen")
            
            // Launch BlockedActivity on main thread
            showBlockedScreen(domain)
            
            // Return NXDOMAIN so browser doesn't show certificate/connection errors
            sendNxdomainResponse(packet, length, dnsData, outputStream)
        }
    }
    
    /**
     * Check if domain is allowed using O(1) HashSet lookup
     * Also checks parent domains for wildcard matching
     */
    private fun isDomainAllowed(domain: String): Boolean {
        val lowerDomain = domain.lowercase()
        
        // Direct match
        if (allowedDomains.contains(lowerDomain)) {
            return true
        }
        
        // Check parent domains (subdomain matching)
        // e.g., "www.youtube.com" → check "youtube.com" → check "com"
        val parts = lowerDomain.split(".")
        for (i in 1 until parts.size) {
            val parentDomain = parts.subList(i, parts.size).joinToString(".")
            if (allowedDomains.contains(parentDomain)) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Extract domain name from DNS query packet
     */
    private fun extractDomainFromDns(dnsData: ByteArray): String? {
        try {
            if (dnsData.size < 12) return null
            
            // Skip DNS header (12 bytes)
            var offset = 12
            val domainParts = mutableListOf<String>()
            
            while (offset < dnsData.size) {
                val labelLength = dnsData[offset].toInt() and 0xFF
                if (labelLength == 0) break
                
                offset++
                if (offset + labelLength > dnsData.size) break
                
                val label = String(dnsData, offset, labelLength, Charsets.US_ASCII)
                domainParts.add(label)
                offset += labelLength
            }
            
            return if (domainParts.isNotEmpty()) {
                domainParts.joinToString(".")
            } else null
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse DNS domain", e)
            return null
        }
    }
    
    /**
     * Forward DNS query to upstream server
     */
    private fun forwardDnsQuery(
        originalPacket: ByteArray,
        length: Int,
        dnsData: ByteArray,
        outputStream: FileOutputStream
    ) {
        scope.launch {
            try {
                DatagramSocket().use { socket ->
                    socket.soTimeout = 5000  // 5 second timeout
                    
                    val upstreamAddress = InetAddress.getByName(UPSTREAM_DNS[0])
                    val requestPacket = DatagramPacket(dnsData, dnsData.size, upstreamAddress, DNS_PORT)
                    socket.send(requestPacket)
                    
                    val responseBuffer = ByteArray(512)
                    val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
                    socket.receive(responsePacket)
                    
                    // Build response packet
                    sendDnsResponse(originalPacket, length, responseBuffer, responsePacket.length, outputStream)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to forward DNS query", e)
            }
        }
    }
    
    // Handler for main thread operations
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Track last blocked domain to avoid spamming the same screen
    private var lastBlockedDomain: String? = null
    private var lastBlockedTime: Long = 0
    private val BLOCK_COOLDOWN_MS = 3000L  // 3 seconds cooldown
    
    /**
     * Show the blocked screen Activity for a domain
     * Uses cooldown to prevent multiple screens for the same domain
     */
    private fun showBlockedScreen(domain: String) {
        val now = System.currentTimeMillis()
        
        // Avoid showing multiple screens for same domain in quick succession
        if (domain == lastBlockedDomain && (now - lastBlockedTime) < BLOCK_COOLDOWN_MS) {
            return
        }
        
        lastBlockedDomain = domain
        lastBlockedTime = now
        
        // Launch BlockedActivity on main thread
        mainHandler.post {
            try {
                val intent = BlockedActivity.createIntent(this, domain)
                startActivity(intent)
                Log.d(TAG, "Launched BlockedActivity for: $domain")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch BlockedActivity", e)
            }
        }
    }
    
    /**
     * Send blocked page response - returns backend IP instead of NXDOMAIN
     * This allows showing a friendly "blocked" page to the user
     */
    private fun sendBlockedPageResponse(
        originalPacket: ByteArray,
        length: Int,
        dnsData: ByteArray,
        domain: String,
        outputStream: FileOutputStream
    ) {
        try {
            // Build DNS response with A record pointing to our backend
            // DNS Header: 12 bytes + Question section + Answer section
            
            // Find end of question section (skip QNAME + QTYPE + QCLASS)
            var questionEnd = 12
            while (questionEnd < dnsData.size && dnsData[questionEnd].toInt() != 0) {
                questionEnd += dnsData[questionEnd].toInt() + 1
            }
            questionEnd += 5 // Skip null byte + QTYPE (2) + QCLASS (2)
            
            // Create response with A record
            val responseSize = questionEnd + 16  // Header + Question + Answer (16 bytes for A record)
            val response = ByteArray(responseSize)
            
            // Copy header and question
            System.arraycopy(dnsData, 0, response, 0, minOf(questionEnd, dnsData.size))
            
            // Set DNS flags
            response[2] = (0x81).toByte()  // QR=1, RD=1
            response[3] = (0x80).toByte()  // RA=1, RCODE=0 (no error)
            
            // Set answer count = 1
            response[6] = 0
            response[7] = 1
            
            // Answer section (A record)
            val answerStart = questionEnd
            response[answerStart] = 0xC0.toByte()  // Pointer to domain name
            response[answerStart + 1] = 0x0C.toByte()  // Offset 12 (start of QNAME)
            response[answerStart + 2] = 0x00  // TYPE A
            response[answerStart + 3] = 0x01
            response[answerStart + 4] = 0x00  // CLASS IN
            response[answerStart + 5] = 0x01
            response[answerStart + 6] = 0x00  // TTL = 60 seconds
            response[answerStart + 7] = 0x00
            response[answerStart + 8] = 0x00
            response[answerStart + 9] = 0x3C.toByte()
            response[answerStart + 10] = 0x00  // RDLENGTH = 4
            response[answerStart + 11] = 0x04
            
            // RDATA = Backend IP (178.156.160.245)
            System.arraycopy(BLOCKED_PAGE_IP, 0, response, answerStart + 12, 4)
            
            sendDnsResponse(originalPacket, length, response, responseSize, outputStream)
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send blocked page response, falling back to NXDOMAIN", e)
            sendNxdomainResponse(originalPacket, length, dnsData, outputStream)
        }
    }
    
    /**
     * Send NXDOMAIN response for blocked domains
     */
    private fun sendNxdomainResponse(
        originalPacket: ByteArray,
        length: Int,
        dnsData: ByteArray,
        outputStream: FileOutputStream
    ) {
        try {
            // Create NXDOMAIN response
            val response = dnsData.copyOf()
            
            // Set QR bit (response), set RCODE to NXDOMAIN (3)
            response[2] = (response[2].toInt() or 0x80).toByte()  // QR = 1 (response)
            response[3] = (response[3].toInt() and 0xF0 or 0x03).toByte()  // RCODE = 3 (NXDOMAIN)
            
            sendDnsResponse(originalPacket, length, response, response.size, outputStream)
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send NXDOMAIN response", e)
        }
    }
    
    /**
     * Send DNS response back through VPN
     */
    private fun sendDnsResponse(
        originalPacket: ByteArray,
        originalLength: Int,
        dnsResponse: ByteArray,
        dnsResponseLength: Int,
        outputStream: FileOutputStream
    ) {
        try {
            val ipHeaderLength = (originalPacket[0].toInt() and 0x0F) * 4
            val responseLength = ipHeaderLength + 8 + dnsResponseLength
            val responsePacket = ByteArray(responseLength)
            
            // Copy IP header and swap source/dest
            System.arraycopy(originalPacket, 0, responsePacket, 0, ipHeaderLength)
            
            // Swap source and destination IP
            System.arraycopy(originalPacket, 12, responsePacket, 16, 4)  // src → dest
            System.arraycopy(originalPacket, 16, responsePacket, 12, 4)  // dest → src
            
            // Update total length
            val totalLength = responseLength
            responsePacket[2] = ((totalLength shr 8) and 0xFF).toByte()
            responsePacket[3] = (totalLength and 0xFF).toByte()
            
            // Swap UDP ports
            responsePacket[ipHeaderLength] = originalPacket[ipHeaderLength + 2]
            responsePacket[ipHeaderLength + 1] = originalPacket[ipHeaderLength + 3]
            responsePacket[ipHeaderLength + 2] = originalPacket[ipHeaderLength]
            responsePacket[ipHeaderLength + 3] = originalPacket[ipHeaderLength + 1]
            
            // UDP length
            val udpLength = 8 + dnsResponseLength
            responsePacket[ipHeaderLength + 4] = ((udpLength shr 8) and 0xFF).toByte()
            responsePacket[ipHeaderLength + 5] = (udpLength and 0xFF).toByte()
            
            // Clear UDP checksum (optional for IPv4)
            responsePacket[ipHeaderLength + 6] = 0
            responsePacket[ipHeaderLength + 7] = 0
            
            // Copy DNS response
            System.arraycopy(dnsResponse, 0, responsePacket, ipHeaderLength + 8, dnsResponseLength)
            
            // Recalculate IP checksum
            recalculateIpChecksum(responsePacket, ipHeaderLength)
            
            synchronized(outputStream) {
                outputStream.write(responsePacket)
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send DNS response", e)
        }
    }
    
    /**
     * Recalculate IP header checksum
     */
    private fun recalculateIpChecksum(packet: ByteArray, headerLength: Int) {
        // Clear existing checksum
        packet[10] = 0
        packet[11] = 0
        
        var sum = 0
        for (i in 0 until headerLength step 2) {
            val word = ((packet[i].toInt() and 0xFF) shl 8) or (packet[i + 1].toInt() and 0xFF)
            sum += word
        }
        
        while (sum shr 16 != 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        
        val checksum = sum.inv() and 0xFFFF
        packet[10] = ((checksum shr 8) and 0xFF).toByte()
        packet[11] = (checksum and 0xFF).toByte()
    }
    
    /**
     * Load whitelist from DataStore cache (offline-first)
     * If cache is empty, triggers immediate sync from backend
     */
    private fun loadWhitelistFromCache() {
        runBlocking {
            try {
                Log.i(TAG, "Loading whitelist from cache...")
                var domains = preferencesManager.getAllowedDomainsSync()
                
                // If cache is empty, sync from backend FIRST before starting VPN filtering
                if (domains.isEmpty()) {
                    Log.w(TAG, "Cache is EMPTY - syncing from backend before VPN starts...")
                    domains = syncDomainsFromBackend()
                }
                
                allowedDomains.clear()
                
                // ALWAYS add essential infrastructure domains as fallback
                // These ensure app functionality even if sync fails
                allowedDomains.addAll(ESSENTIAL_INFRASTRUCTURE.map { it.lowercase() })
                Log.i(TAG, "Added ${ESSENTIAL_INFRASTRUCTURE.size} essential infrastructure domains")
                
                // Add domains from backend/cache
                allowedDomains.addAll(domains.map { it.lowercase() })
                
                // Track parent-added domains count (excluding infrastructure)
                parentAddedDomainCount = domains.size
                
                Log.i(TAG, "======== WHITELIST LOADED ========")
                Log.i(TAG, "Total domains: ${allowedDomains.size}")
                Log.i(TAG, "  - Essential infrastructure: ${ESSENTIAL_INFRASTRUCTURE.size}")
                Log.i(TAG, "  - From backend/cache: ${domains.size}")
                if (domains.isEmpty()) {
                    Log.w(TAG, "NOTE: Only infrastructure domains loaded")
                    Log.w(TAG, "Parent has not added any websites yet, or sync failed")
                } else {
                    Log.i(TAG, "Sample domains: ${allowedDomains.take(20)}")
                }
                Log.i(TAG, "===================================")
            } catch (e: Exception) {
                Log.e(TAG, "FAILED to load whitelist from cache", e)
                // Even on failure, ensure infrastructure domains are allowed
                allowedDomains.addAll(ESSENTIAL_INFRASTRUCTURE.map { it.lowercase() })
                Log.w(TAG, "Added ${ESSENTIAL_INFRASTRUCTURE.size} essential domains as fallback")
            }
        }
    }
    
    /**
     * Sync domains directly from backend when cache is empty
     * Returns the domains fetched, or empty set on failure
     */
    private suspend fun syncDomainsFromBackend(): Set<String> {
        try {
            val deviceId = preferencesManager.getDeviceId()
            if (deviceId == null) {
                Log.w(TAG, "No device ID - cannot sync from backend (device not paired?)")
                return emptySet()
            }
            
            Log.i(TAG, "Calling backend /api/whitelist/sync/$deviceId ...")
            val response = api.syncWhitelist(deviceId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    val domains = body.domains.toSet()
                    Log.i(TAG, "SUCCESS: Fetched ${domains.size} domains from backend")
                    
                    // Save to cache for next time
                    preferencesManager.updateAllowedDomains(domains, body.version)
                    return domains
                }
            } else {
                Log.e(TAG, "Backend sync failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from backend", e)
        }
        return emptySet()
    }
    
    /**
     * Reload whitelist from DataStore (called when sync completes or push notification received)
     */
    private fun reloadWhitelist() {
        scope.launch {
            try {
                Log.i(TAG, "======== RELOADING WHITELIST (triggered by FCM) ========")
                val domains = preferencesManager.getAllowedDomainsSync()
                val oldCount = parentAddedDomainCount
                allowedDomains.clear()
                
                // Add infrastructure domains
                allowedDomains.addAll(ESSENTIAL_INFRASTRUCTURE.map { it.lowercase() })
                
                // Add parent domains
                allowedDomains.addAll(domains.map { it.lowercase() })
                parentAddedDomainCount = domains.size
                
                Log.i(TAG, "Whitelist reloaded: $oldCount -> $parentAddedDomainCount parent domains")
                if (allowedDomains.isEmpty()) {
                    Log.w(TAG, "WARNING: Whitelist is EMPTY after reload!")
                } else {
                    Log.i(TAG, "Sample: ${allowedDomains.take(10)}")
                }
                
                // Update notification with new domain count
                updateNotification()
                
                Log.i(TAG, "========================================================")
            } catch (e: Exception) {
                Log.e(TAG, "FAILED to reload whitelist", e)
            }
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WaqiKids DNS Protection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active DNS filtering for safe browsing"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WaqiKids Protection Active")
            .setContentText("$parentAddedDomainCount websites allowed by parent")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    /**
     * Update the foreground notification with current domain count
     */
    private fun updateNotification() {
        try {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, createNotification())
            Log.d(TAG, "Notification updated: $parentAddedDomainCount parent domains")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update notification", e)
        }
    }
    
    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }
}
