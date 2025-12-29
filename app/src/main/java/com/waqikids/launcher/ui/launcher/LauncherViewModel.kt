package com.waqikids.launcher.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waqikids.launcher.data.islamic.IslamicDataRepository
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.data.repository.AppRepository
import com.waqikids.launcher.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferencesManager: PreferencesManager,
    private val islamicDataRepository: IslamicDataRepository
) : ViewModel() {
    
    val allowedApps: Flow<List<AppInfo>> = appRepository.getAllowedApps()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val childName: Flow<String> = preferencesManager.deviceConfig.map { config ->
        config?.childName ?: "Child"
    }
    
    // TODO: Implement actual time tracking
    val timeRemaining: Flow<String> = kotlinx.coroutines.flow.flowOf("2h 30m")
    
    // Islamic Data
    private val _prayerTimes = MutableStateFlow(islamicDataRepository.getPrayerTimes())
    val prayerTimes: StateFlow<IslamicDataRepository.PrayerTimes> = _prayerTimes.asStateFlow()
    
    private val _currentDhikr = MutableStateFlow(getCurrentDhikr())
    val currentDhikr: StateFlow<IslamicDataRepository.Dhikr> = _currentDhikr.asStateFlow()
    
    private val _currentFact = MutableStateFlow(islamicDataRepository.getFactOfTheDay())
    val currentFact: StateFlow<IslamicDataRepository.IslamicFact> = _currentFact.asStateFlow()
    
    private var dhikrIndex = 0
    
    init {
        // Refresh prayer times every minute
        viewModelScope.launch {
            while (true) {
                delay(60_000L) // 1 minute
                _prayerTimes.value = islamicDataRepository.getPrayerTimes()
            }
        }
    }
    
    private fun getCurrentDhikr(): IslamicDataRepository.Dhikr {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val adhkarList = if (hour in 5..11) {
            islamicDataRepository.getMorningAdhkar()
        } else {
            islamicDataRepository.getEveningAdhkar()
        }
        return adhkarList[dhikrIndex % adhkarList.size]
    }
    
    fun nextDhikr() {
        dhikrIndex++
        _currentDhikr.value = getCurrentDhikr()
    }
    
    fun refreshFact() {
        _currentFact.value = islamicDataRepository.getRandomFact()
    }
    
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
