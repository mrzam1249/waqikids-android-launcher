package com.waqikids.launcher.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.domain.model.DeviceConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val deviceConfig: Flow<DeviceConfig?> = preferencesManager.deviceConfig
    
    val currentSetupStep: Flow<Int> = preferencesManager.currentSetupStep
    
    fun setCurrentStep(step: Int) {
        viewModelScope.launch {
            preferencesManager.setCurrentSetupStep(step)
        }
    }
    
    suspend fun completeSetup() {
        preferencesManager.setCurrentSetupStep(0) // Reset for next time
        preferencesManager.setSetupComplete(true)
    }
}
