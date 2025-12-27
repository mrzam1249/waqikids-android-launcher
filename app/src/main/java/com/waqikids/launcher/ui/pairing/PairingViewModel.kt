package com.waqikids.launcher.ui.pairing

import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waqikids.launcher.data.api.WaqiApi
import com.waqikids.launcher.data.api.dto.PairRequest
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.domain.model.DeviceConfig
import com.waqikids.launcher.domain.model.ProtectionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

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
                val deviceId = getOrCreateDeviceId()
                val deviceName = getDeviceName()
                
                val request = PairRequest(
                    childDeviceId = deviceId,
                    childDeviceName = deviceName,
                    platform = "android",
                    pairingCode = code.trim()
                )
                
                val response = api.pairDevice(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    
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
                    onResult(true, null)
                } else {
                    val errorMsg = response.body()?.error 
                        ?: response.errorBody()?.string()
                        ?: "Pairing failed. Please check the code and try again."
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                onResult(false, "Connection error: ${e.message}")
            }
        }
    }
}
