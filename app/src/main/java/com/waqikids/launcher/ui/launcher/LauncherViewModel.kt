package com.waqikids.launcher.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.data.repository.AppRepository
import com.waqikids.launcher.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val allowedApps: Flow<List<AppInfo>> = appRepository.getAllowedApps()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val childName: Flow<String> = preferencesManager.deviceConfig.map { config ->
        config?.childName ?: "Child"
    }
    
    // TODO: Implement actual time tracking
    val timeRemaining: Flow<String> = kotlinx.coroutines.flow.flowOf("2h 30m")
    
    fun launchApp(packageName: String) {
        viewModelScope.launch {
            appRepository.launchApp(packageName)
        }
    }
    
    fun refreshApps() {
        viewModelScope.launch {
            // Trigger a refresh of apps
            // This would sync with backend for latest allowed list
        }
    }
}
