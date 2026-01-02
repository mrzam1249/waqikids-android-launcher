package com.waqikids.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.waqikids.launcher.R
import com.waqikids.launcher.data.api.WaqiApi
import com.waqikids.launcher.data.api.dto.FcmTokenRequest
import com.waqikids.launcher.data.api.dto.SyncAppItem
import com.waqikids.launcher.data.api.dto.SyncAppsRequest
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.data.repository.AppRepository
import com.waqikids.launcher.ui.MainActivity
import com.waqikids.launcher.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Foreground service that keeps the launcher running and syncs with backend
 * Periodically syncs installed apps and fetches allowed packages from parent
 */
@AndroidEntryPoint
class SyncService : Service() {
    
    @Inject lateinit var api: WaqiApi
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var appRepository: AppRepository
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "SyncService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "waqikids_protection"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "========== SYNC SERVICE CREATED ==========")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startSyncLoop()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY  // Restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
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
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun startSyncLoop() {
        scope.launch {
            // Initial sync after a short delay
            delay(5000)
            
            // Register FCM token on startup (important for push notifications)
            registerFcmToken()
            
            while (isActive) {
                try {
                    syncWithBackend()
                } catch (e: Exception) {
                    Log.e(TAG, "Sync failed", e)
                }
                delay(Constants.SYNC_INTERVAL_MINUTES * 60 * 1000L)
            }
        }
    }
    
    /**
     * Register FCM token with backend for push notifications
     * Called on every app startup to ensure token is always registered
     */
    private suspend fun registerFcmToken() {
        try {
            Log.i(TAG, "======== REGISTERING FCM TOKEN ========")
            val deviceId = preferencesManager.getDeviceId() ?: run {
                Log.w(TAG, "No device ID found, cannot register FCM token")
                Log.w(TAG, "This means device is not paired yet!")
                return
            }
            Log.i(TAG, "Device ID: $deviceId")
            
            // Get current FCM token
            val token = FirebaseMessaging.getInstance().token.await()
            Log.i(TAG, "FCM token obtained: ${token.take(30)}...")
            
            val request = FcmTokenRequest(
                deviceId = deviceId,
                fcmToken = token,
                platform = "android"
            )
            
            Log.i(TAG, "Sending FCM token to backend...")
            val response = api.registerFcmToken(request)
            
            if (response.isSuccessful) {
                Log.i(TAG, "SUCCESS: FCM token registered with backend")
                Log.i(TAG, "Push notifications are now enabled!")
            } else {
                Log.e(TAG, "FAILED: FCM registration failed: ${response.code()} ${response.message()}")
                try {
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                } catch (e: Exception) { }
            }
            Log.i(TAG, "=========================================")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR registering FCM token", e)
            Log.e(TAG, "Exception: ${e.message}")
        }
    }
    
    private suspend fun syncWithBackend() {
        val deviceId = preferencesManager.getDeviceId() ?: run {
            Log.w(TAG, "No device ID, skipping sync - device not paired?")
            return
        }
        
        Log.i(TAG, "========== SYNC WITH BACKEND ==========")
        Log.i(TAG, "Device ID: $deviceId")
        
        // Step 1: Sync installed apps (only if needed - hourly)
        if (preferencesManager.shouldSyncApps()) {
            Log.i(TAG, "Step 1: Syncing installed apps...")
            syncInstalledApps(deviceId)
        } else {
            Log.d(TAG, "Step 1: Skipping app sync - cache still valid")
        }
        
        // Step 2: Fetch allowed packages (every 5 min or if cache expired)
        if (!preferencesManager.isAllowedPackagesCacheValid()) {
            Log.i(TAG, "Step 2: Fetching allowed packages...")
            fetchAllowedPackages(deviceId)
        } else {
            Log.d(TAG, "Step 2: Using cached allowed packages")
        }
        
        // Step 3: Sync domain whitelist for VPN DNS filtering
        if (!preferencesManager.isDomainsCacheValid()) {
            Log.i(TAG, "Step 3: Syncing domain whitelist...")
            syncDomainWhitelist(deviceId)
        } else {
            Log.d(TAG, "Step 3: Using cached domain whitelist")
        }
        
        Log.i(TAG, "=========================================")
    }
    
    /**
     * Sync installed apps to backend with full metadata (including icons)
     */
    private suspend fun syncInstalledApps(deviceId: String) {
        try {
            val apps = appRepository.getAppsForSync()
            
            val syncItems = apps.map { app ->
                SyncAppItem(
                    packageName = app.packageName,
                    appName = app.name,
                    iconData = app.iconBase64,
                    platform = "android"
                )
            }
            
            val request = SyncAppsRequest(
                deviceId = deviceId,
                apps = syncItems
            )
            
            val response = api.syncApps(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "Apps synced successfully: ${body?.synced ?: 0} apps")
                // Record sync timestamp on success
                preferencesManager.setLastAppsSync()
            } else {
                Log.e(TAG, "Failed to sync apps: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing apps", e)
        }
    }
    
    /**
     * Fetch allowed packages from backend and update local cache
     * Cache is persisted in DataStore for offline access
     */
    private suspend fun fetchAllowedPackages(deviceId: String) {
        try {
            val response = api.getAllowedPackages(deviceId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    val packages = body.packages.toSet()
                    preferencesManager.updateAllowedPackages(packages)
                    // Record fetch timestamp on success
                    preferencesManager.setLastAllowedPackagesFetch()
                    Log.d(TAG, "Allowed packages cached: ${packages.size} packages")
                }
            } else {
                Log.e(TAG, "Failed to fetch allowed packages: ${response.code()}")
                // On failure, use cached data (offline-first)
                Log.d(TAG, "Using cached allowed packages due to network error")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching allowed packages", e)
            // On network error, cached data in DataStore is still valid
            Log.d(TAG, "Network error - continuing with cached allowed packages")
        }
    }
    
    /**
     * Sync domain whitelist from backend for VPN DNS filtering
     * Returns all allowed domains: infrastructure + family + child-specific
     * Cache is persisted in DataStore for offline access
     */
    private suspend fun syncDomainWhitelist(deviceId: String) {
        try {
            Log.i(TAG, "======== SYNCING DOMAIN WHITELIST ========")
            Log.i(TAG, "Device ID: $deviceId")
            Log.i(TAG, "Calling backend /api/whitelist/sync/$deviceId ...")
            
            val response = api.syncWhitelist(deviceId)
            
            Log.i(TAG, "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    val domains = body.domains.toSet()
                    val parentDomains = body.parentDomains?.toSet()
                    val version = body.version
                    Log.i(TAG, "SUCCESS: Received ${domains.size} domains, ${parentDomains?.size ?: 0} parent domains")
                    Log.i(TAG, "Version: $version")
                    Log.i(TAG, "Sample domains: ${domains.take(10)}")
                    Log.i(TAG, "Sample parent domains: ${parentDomains?.take(10)}")
                    
                    preferencesManager.updateAllowedDomains(domains, version, parentDomains)
                    Log.i(TAG, "Domains saved to local cache")
                    
                    // Notify VPN service to reload whitelist
                    notifyVpnWhitelistUpdated()
                    Log.i(TAG, "VPN service notified to reload")
                } else {
                    Log.w(TAG, "Response body null or status != success")
                    Log.w(TAG, "Body: $body")
                }
            } else {
                Log.e(TAG, "FAILED: Sync failed with code ${response.code()}")
                try {
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                } catch (e: Exception) { }
                Log.d(TAG, "Using cached domain whitelist due to network error")
            }
            Log.i(TAG, "===========================================")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR syncing domain whitelist", e)
            Log.e(TAG, "Exception: ${e.message}")
            Log.d(TAG, "Network error - continuing with cached domain whitelist")
        }
    }
    
    /**
     * Notify VPN service that whitelist has been updated
     * VPN service will reload domains from DataStore
     */
    private fun notifyVpnWhitelistUpdated() {
        try {
            val intent = Intent(this, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_RELOAD_WHITELIST
            }
            startService(intent)
            Log.d(TAG, "Notified VPN service to reload whitelist")
        } catch (e: Exception) {
            Log.w(TAG, "Could not notify VPN service - may not be running", e)
        }
    }

    /**
     * Force an immediate sync (called when app installs/uninstalls detected or push notification received)
     * Bypasses cache validity checks
     */
    fun forceSync() {
        scope.launch {
            val deviceId = preferencesManager.getDeviceId() ?: return@launch
            Log.d(TAG, "Force sync triggered for device: $deviceId")
            
            try {
                // Force sync apps (bypass cache check)
                syncInstalledApps(deviceId)
                // Force fetch allowed packages (bypass cache check)
                fetchAllowedPackages(deviceId)
                // Force sync domain whitelist (bypass cache check)
                syncDomainWhitelist(deviceId)
            } catch (e: Exception) {
                Log.e(TAG, "Force sync failed", e)
            }
        }
    }
    
    /**
     * Force sync only domain whitelist (called from push notification)
     */
    fun forceSyncDomains() {
        scope.launch {
            val deviceId = preferencesManager.getDeviceId() ?: return@launch
            Log.d(TAG, "Force domain sync triggered for device: $deviceId")
            
            try {
                syncDomainWhitelist(deviceId)
            } catch (e: Exception) {
                Log.e(TAG, "Force domain sync failed", e)
            }
        }
    }
    
    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
