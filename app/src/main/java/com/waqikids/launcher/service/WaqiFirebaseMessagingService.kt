package com.waqikids.launcher.service

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.waqikids.launcher.data.api.WaqiApi
import com.waqikids.launcher.data.api.dto.FcmTokenRequest
import com.waqikids.launcher.data.local.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service - Enterprise Standard Push Notifications
 * 
 * Notification Types:
 * - whitelist_sync: Parent added/removed domain → Trigger instant sync
 * - device_unpaired: Parent unpaired device → Clear local data
 * - app_allowed/blocked: Parent changed app permission → Refresh apps
 * 
 * Benefits:
 * - Instant updates (< 1 second vs 5 minutes polling)
 * - Battery efficient (no polling when idle)
 * - Reliable delivery with Firebase infrastructure
 */
@AndroidEntryPoint
class WaqiFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject lateinit var api: WaqiApi
    @Inject lateinit var preferencesManager: PreferencesManager
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "WaqiFCM"
        
        // Notification types from backend
        const val TYPE_WHITELIST_SYNC = "whitelist_sync"
        const val TYPE_DEVICE_UNPAIRED = "device_unpaired"
        const val TYPE_APP_ALLOWED = "app_allowed"
        const val TYPE_APP_BLOCKED = "app_blocked"
    }
    
    /**
     * Called when FCM token is generated or refreshed
     * Register with backend to receive push notifications
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: ${token.take(20)}...")
        
        scope.launch {
            registerTokenWithBackend(token)
        }
    }
    
    /**
     * Called when push notification is received
     * Handle data messages (not display notifications)
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val data = message.data
        val notificationType = data["type"] ?: data["notification_type"]
        
        Log.i(TAG, "FCM received: type=$notificationType")
        
        when (notificationType) {
            TYPE_WHITELIST_SYNC -> handleWhitelistSync(data)
            TYPE_DEVICE_UNPAIRED -> handleDeviceUnpaired()
            TYPE_APP_ALLOWED, TYPE_APP_BLOCKED -> handleAppChange()
            else -> Log.w(TAG, "Unknown notification type: $notificationType")
        }
    }
    
    /**
     * Handle whitelist sync notification - INSTANT domain update
     * Parent added/removed a domain → Sync immediately
     */
    private fun handleWhitelistSync(data: Map<String, String>) {
        val action = data["action"] ?: "update"
        val domain = data["domain"] ?: ""
        
        Log.i(TAG, "Whitelist $action: $domain - triggering instant sync")
        
        // Notify VPN service to reload whitelist
        try {
            val vpnIntent = Intent(this, DnsVpnService::class.java).apply {
                setAction(DnsVpnService.ACTION_RELOAD_WHITELIST)
            }
            startService(vpnIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not notify VPN service", e)
        }
        
        // Also trigger full sync in background
        scope.launch {
            try {
                val deviceId = preferencesManager.getDeviceId() ?: return@launch
                val response = api.syncWhitelist(deviceId)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        preferencesManager.updateAllowedDomains(body.domains.toSet(), body.version)
                        Log.i(TAG, "Instant sync complete: ${body.count} domains")
                        
                        // Reload VPN with new data
                        val reloadIntent = Intent(this@WaqiFirebaseMessagingService, DnsVpnService::class.java).apply {
                            setAction(DnsVpnService.ACTION_RELOAD_WHITELIST)
                        }
                        startService(reloadIntent)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Instant sync failed", e)
            }
        }
    }
    
    /**
     * Handle device unpaired notification
     * Parent removed device → Clear local data
     */
    private fun handleDeviceUnpaired() {
        Log.i(TAG, "Device unpaired by parent - clearing data")
        
        scope.launch {
            try {
                // Stop VPN
                val vpnIntent = Intent(this@WaqiFirebaseMessagingService, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_STOP
                }
                startService(vpnIntent)
                
                // Clear all local data
                preferencesManager.clearAll()
                
                Log.i(TAG, "Local data cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle unpair", e)
            }
        }
    }
    
    /**
     * Handle app allowed/blocked notification
     * Parent changed app permission → Refresh allowed packages
     */
    private fun handleAppChange() {
        Log.i(TAG, "App permission changed - syncing")
        
        scope.launch {
            try {
                val deviceId = preferencesManager.getDeviceId() ?: return@launch
                val response = api.getAllowedPackages(deviceId)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        preferencesManager.updateAllowedPackages(body.packages.toSet())
                        Log.i(TAG, "App sync complete: ${body.count} packages")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "App sync failed", e)
            }
        }
    }
    
    /**
     * Register FCM token with backend for push notifications
     */
    private suspend fun registerTokenWithBackend(token: String) {
        try {
            val deviceId = preferencesManager.getDeviceId()
            if (deviceId == null) {
                Log.w(TAG, "No device ID yet, will register token after pairing")
                return
            }
            
            val request = FcmTokenRequest(
                deviceId = deviceId,
                fcmToken = token,
                platform = "android"
            )
            
            val response = api.registerFcmToken(request)
            
            if (response.isSuccessful) {
                Log.i(TAG, "FCM token registered with backend")
            } else {
                Log.e(TAG, "Failed to register FCM token: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering FCM token", e)
        }
    }
}
