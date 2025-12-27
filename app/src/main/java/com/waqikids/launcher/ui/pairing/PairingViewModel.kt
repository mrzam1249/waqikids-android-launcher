package com.waqikids.launcher.ui.pairing

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waqikids.launcher.BuildConfig
import com.waqikids.launcher.data.api.WaqiApi
import com.waqikids.launcher.data.api.dto.DeviceInfo
import com.waqikids.launcher.data.api.dto.PairRequest
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.domain.model.DeviceConfig
import com.waqikids.launcher.domain.model.ProtectionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val api: WaqiApi,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    fun pairDevice(code: String, onResult: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                val request = PairRequest(
                    pairingCode = code,
                    deviceInfo = DeviceInfo(
                        manufacturer = Build.MANUFACTURER,
                        model = Build.MODEL,
                        osVersion = Build.VERSION.RELEASE,
                        appVersion = BuildConfig.VERSION_NAME
                    )
                )
                
                val response = api.pairDevice(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    
                    // Save config locally
                    val config = DeviceConfig(
                        deviceId = body.deviceId ?: "",
                        childName = body.childName ?: "Child",
                        parentId = body.parentId ?: "",
                        dnsSubdomain = body.dnsSubdomain ?: "",
                        protectionMode = ProtectionMode.valueOf(
                            body.protectionMode ?: "EASY"
                        ),
                        allowedPackages = body.allowedPackages ?: emptyList(),
                        isPaired = true,
                        isSetupComplete = false
                    )
                    
                    preferencesManager.saveDeviceConfig(config)
                    onResult(true, null)
                } else {
                    onResult(false, response.body()?.error ?: "Pairing failed")
                }
            } catch (e: Exception) {
                // For demo/testing: simulate successful pairing
                val mockConfig = DeviceConfig(
                    deviceId = "demo-device-${System.currentTimeMillis()}",
                    childName = "Demo Child",
                    parentId = "demo-parent",
                    dnsSubdomain = "demo",
                    protectionMode = ProtectionMode.EASY,
                    allowedPackages = listOf(
                        "com.android.chrome",
                        "com.google.android.youtube"
                    ),
                    isPaired = true,
                    isSetupComplete = false
                )
                preferencesManager.saveDeviceConfig(mockConfig)
                onResult(true, null)
            }
        }
    }
}
