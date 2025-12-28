package com.waqikids.launcher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.waqikids.launcher.domain.model.DeviceConfig
import com.waqikids.launcher.domain.model.ProtectionMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "waqikids_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private object Keys {
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val IS_PAIRED = booleanPreferencesKey("is_paired")
        val IS_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val CHILD_NAME = stringPreferencesKey("child_name")
        val PARENT_ID = stringPreferencesKey("parent_id")
        val DNS_SUBDOMAIN = stringPreferencesKey("dns_subdomain")
        val PROTECTION_MODE = stringPreferencesKey("protection_mode")
        val ALLOWED_PACKAGES = stringSetPreferencesKey("allowed_packages")
        val IS_LAUNCHER_SET = booleanPreferencesKey("is_launcher_set")
        val IS_ACCESSIBILITY_ENABLED = booleanPreferencesKey("is_accessibility_enabled")
        val IS_DNS_CONFIGURED = booleanPreferencesKey("is_dns_configured")
        val CACHED_PIN_HASH = stringPreferencesKey("cached_pin_hash")
        val PARENT_MODE_EXPIRY = longPreferencesKey("parent_mode_expiry")
    }
    
    // Flow to observe setup completion status
    val isSetupComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_SETUP_COMPLETE] ?: false
    }
    
    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_ONBOARDING_COMPLETE] ?: false
    }
    
    val isPaired: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_PAIRED] ?: false
    }
    
    val deviceConfig: Flow<DeviceConfig?> = context.dataStore.data.map { prefs ->
        val deviceId = prefs[Keys.DEVICE_ID] ?: return@map null
        DeviceConfig(
            deviceId = deviceId,
            childName = prefs[Keys.CHILD_NAME] ?: "Child",
            parentId = prefs[Keys.PARENT_ID] ?: "",
            dnsSubdomain = prefs[Keys.DNS_SUBDOMAIN] ?: "",
            protectionMode = ProtectionMode.valueOf(
                prefs[Keys.PROTECTION_MODE] ?: ProtectionMode.EASY.name
            ),
            allowedPackages = prefs[Keys.ALLOWED_PACKAGES]?.toList() ?: emptyList(),
            isPaired = prefs[Keys.IS_PAIRED] ?: false,
            isSetupComplete = prefs[Keys.IS_SETUP_COMPLETE] ?: false
        )
    }
    
    val allowedPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.ALLOWED_PACKAGES] ?: emptySet()
    }
    
    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDING_COMPLETE] = complete
        }
    }
    
    suspend fun saveDeviceConfig(config: DeviceConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEVICE_ID] = config.deviceId
            prefs[Keys.CHILD_NAME] = config.childName
            prefs[Keys.PARENT_ID] = config.parentId
            prefs[Keys.DNS_SUBDOMAIN] = config.dnsSubdomain
            prefs[Keys.PROTECTION_MODE] = config.protectionMode.name
            prefs[Keys.ALLOWED_PACKAGES] = config.allowedPackages.toSet()
            prefs[Keys.IS_PAIRED] = true
        }
    }
    
    suspend fun updateAllowedPackages(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ALLOWED_PACKAGES] = packages
        }
    }
    
    suspend fun setLauncherSet(isSet: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LAUNCHER_SET] = isSet
        }
    }
    
    suspend fun setAccessibilityEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ACCESSIBILITY_ENABLED] = enabled
        }
    }
    
    suspend fun setDnsConfigured(configured: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_DNS_CONFIGURED] = configured
        }
    }
    
    suspend fun setSetupComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SETUP_COMPLETE] = complete
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
    
    // ===== PIN Caching for Parent Mode =====
    
    suspend fun getDeviceId(): String? {
        return deviceConfig.first()?.deviceId
    }
    
    suspend fun getCachedPinHash(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.CACHED_PIN_HASH]
        }.first()
    }
    
    suspend fun setCachedPinHash(hash: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CACHED_PIN_HASH] = hash
        }
    }
    
    suspend fun setParentModeExpiry(expiryTimeMillis: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PARENT_MODE_EXPIRY] = expiryTimeMillis
        }
    }
    
    suspend fun getParentModeExpiry(): Long {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.PARENT_MODE_EXPIRY] ?: 0L
        }.first()
    }
    
    suspend fun isParentModeActive(): Boolean {
        val expiry = getParentModeExpiry()
        return expiry > System.currentTimeMillis()
    }
    
    suspend fun clearParentMode() {
        context.dataStore.edit { prefs ->
            prefs[Keys.PARENT_MODE_EXPIRY] = 0L
        }
    }
}
