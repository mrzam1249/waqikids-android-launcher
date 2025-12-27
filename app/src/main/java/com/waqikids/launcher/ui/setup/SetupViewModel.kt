package com.waqikids.launcher.ui.setup

import androidx.lifecycle.ViewModel
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.domain.model.DeviceConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val deviceConfig: Flow<DeviceConfig?> = preferencesManager.deviceConfig
    
    suspend fun completeSetup() {
        preferencesManager.setSetupComplete(true)
    }
}
