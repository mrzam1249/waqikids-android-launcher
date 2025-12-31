package com.waqikids.launcher.ui.pairing

import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.waqikids.launcher.data.api.WaqiApi
import com.waqikids.launcher.data.api.dto.FcmTokenRequest
import com.waqikids.launcher.data.api.dto.PairRequest
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.domain.model.DeviceConfig
import com.waqikids.launcher.domain.model.ProtectionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

private const val TAG = "PairingViewModel"

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val api: WaqiApi,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private fun getOrCreateDeviceId(): String {
        // Try to get Android ID first
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
            "android-$androidId"
        } else {
            // Fallback to random UUID
            "android-${UUID.randomUUID()}"
        }
    }
    
    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }
    
    fun pairDevice(code: String, onResult: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.i(TAG, "========== PAIRING DEVICE ==========")
                val deviceId = getOrCreateDeviceId()
                val deviceName = getDeviceName()
                
                Log.i(TAG, "Device ID: $deviceId")
                Log.i(TAG, "Device Name: $deviceName")
                Log.i(TAG, "Pairing Code: $code")
                
                val request = PairRequest(
                    childDeviceId = deviceId,
                    childDeviceName = deviceName,
                    platform = "android",
                    pairingCode = code.trim()
                )
                
                Log.i(TAG, "Calling backend /api/devices/pair ...")
                val response = api.pairDevice(request)
                
                Log.i(TAG, "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    Log.i(TAG, "PAIRING SUCCESS!")
                    Log.i(TAG, "Parent ID: ${body.parentId}")
                    
                    // Save config locally
                    val config = DeviceConfig(
                        deviceId = deviceId,
                        childName = deviceName,
                        parentId = body.parentId ?: "",
                        dnsSubdomain = deviceId,  // Used for DNS profile
                        protectionMode = ProtectionMode.EASY,
                        allowedPackages = emptyList(),  // Will be fetched via heartbeat
                        isPaired = true,
                        isSetupComplete = false
                    )
                    
                    preferencesManager.saveDeviceConfig(config)
                    Log.i(TAG, "Device config saved to DataStore")
                    
                    // IMPORTANT: Register FCM token immediately after pairing!
                    registerFcmTokenAfterPairing(deviceId)
                    
                    Log.i(TAG, "=====================================")
                    onResult(true, null)
                } else {
                    val errorMsg = response.body()?.error 
                        ?: response.errorBody()?.string()
                        ?: "Pairing failed. Please check the code and try again."
                    Log.e(TAG, "PAIRING FAILED: $errorMsg")
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "PAIRING ERROR", e)
                onResult(false, "Connection error: ${e.message}")
            }
        }
    }
    
    /**
     * Register FCM token immediately after successful pairing
     * This ensures push notifications work from the start
     */
    private suspend fun registerFcmTokenAfterPairing(deviceId: String) {
        try {
            Log.i(TAG, "======== REGISTERING FCM TOKEN (post-pairing) ========")
            val token = FirebaseMessaging.getInstance().token.await()
            Log.i(TAG, "FCM Token: ${token.take(40)}...")
            
            val request = FcmTokenRequest(
                deviceId = deviceId,
                fcmToken = token,
                platform = "android"
            )
            
            val response = api.registerFcmToken(request)
            
            if (response.isSuccessful) {
                Log.i(TAG, "SUCCESS: FCM token registered immediately after pairing!")
            } else {
                Log.e(TAG, "FAILED to register FCM token: ${response.code()}")
            }
            Log.i(TAG, "======================================================")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering FCM token after pairing", e)
        }
    }
}
