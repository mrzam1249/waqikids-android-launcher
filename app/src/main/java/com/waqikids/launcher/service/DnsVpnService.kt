package com.waqikids.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.waqikids.launcher.R
import com.waqikids.launcher.data.local.PreferencesManager
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
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRunning = AtomicBoolean(false)
    
    // O(1) lookup whitelist - loaded from DataStore
    private val allowedDomains = ConcurrentHashMap.newKeySet<String>()
    
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
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        loadWhitelistFromCache()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpn()
            ACTION_STOP -> stopVpn()
            ACTION_RELOAD_WHITELIST -> reloadWhitelist()
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
            
            Log.i(TAG, "VPN started successfully with ${allowedDomains.size} whitelisted domains")
            
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
            forwardDnsQuery(packet, length, dnsData, outputStream)
        } else {
            // Return NXDOMAIN
            blockedQueries++
            Log.d(TAG, "BLOCKED: $domain")
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
     */
    private fun loadWhitelistFromCache() {
        runBlocking {
            try {
                val domains = preferencesManager.getAllowedDomainsSync()
                allowedDomains.clear()
                allowedDomains.addAll(domains.map { it.lowercase() })
                Log.d(TAG, "Loaded ${allowedDomains.size} domains from cache")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load whitelist from cache", e)
            }
        }
    }
    
    /**
     * Reload whitelist from DataStore (called when sync completes or push notification received)
     */
    private fun reloadWhitelist() {
        scope.launch {
            try {
                val domains = preferencesManager.getAllowedDomainsSync()
                allowedDomains.clear()
                allowedDomains.addAll(domains.map { it.lowercase() })
                Log.i(TAG, "Reloaded ${allowedDomains.size} domains from cache")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reload whitelist", e)
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
            .setContentText("DNS filtering: ${allowedDomains.size} websites allowed")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }
}
