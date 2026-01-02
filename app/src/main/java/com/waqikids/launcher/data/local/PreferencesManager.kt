package com.waqikids.launcher.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.intPreferencesKey
import com.waqikids.launcher.domain.model.DeviceConfig
import com.waqikids.launcher.domain.model.ProtectionMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "waqikids_prefs")

private const val TAG = "PreferencesManager"

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
        val CURRENT_SETUP_STEP = intPreferencesKey("current_setup_step")
        val CACHED_PIN_HASH = stringPreferencesKey("cached_pin_hash")
        val PARENT_MODE_EXPIRY = longPreferencesKey("parent_mode_expiry")
        // Cache timestamps
        val LAST_APPS_SYNC = longPreferencesKey("last_apps_sync")
        val LAST_ALLOWED_PACKAGES_FETCH = longPreferencesKey("last_allowed_packages_fetch")
        // Domain whitelist for VPN DNS filtering
        val ALLOWED_DOMAINS = stringSetPreferencesKey("allowed_domains")
        val PARENT_DOMAINS = stringSetPreferencesKey("parent_domains")  // Only parent-added for display
        val LAST_DOMAINS_SYNC = longPreferencesKey("last_domains_sync")
        val DOMAINS_VERSION = longPreferencesKey("domains_version")
    }
    
    companion object {
        // Cache validity: 5 minutes (FCM push handles instant updates)
        const val ALLOWED_PACKAGES_CACHE_DURATION_MS = 5 * 60 * 1000L
        // Apps sync: once per hour is enough (apps don't change that often)
        const val APPS_SYNC_INTERVAL_MS = 60 * 60 * 1000L
        // Domain whitelist: 5 minutes cache (FCM push handles instant updates)
        const val DOMAINS_CACHE_DURATION_MS = 5 * 60 * 1000L
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
    
    val childName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CHILD_NAME] ?: ""
    }
    
    val currentSetupStep: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENT_SETUP_STEP] ?: 0
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
    
    suspend fun setCurrentSetupStep(step: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_SETUP_STEP] = step
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
    
    // ===== Cache Timestamps for Sync =====
    
    suspend fun setLastAppsSync(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_APPS_SYNC] = timestamp
        }
    }
    
    suspend fun getLastAppsSync(): Long {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.LAST_APPS_SYNC] ?: 0L
        }.first()
    }
    
    suspend fun shouldSyncApps(): Boolean {
        val lastSync = getLastAppsSync()
        return System.currentTimeMillis() - lastSync > APPS_SYNC_INTERVAL_MS
    }
    
    suspend fun setLastAllowedPackagesFetch(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_ALLOWED_PACKAGES_FETCH] = timestamp
        }
    }
    
    suspend fun getLastAllowedPackagesFetch(): Long {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.LAST_ALLOWED_PACKAGES_FETCH] ?: 0L
        }.first()
    }
    
    suspend fun isAllowedPackagesCacheValid(): Boolean {
        val lastFetch = getLastAllowedPackagesFetch()
        return System.currentTimeMillis() - lastFetch < ALLOWED_PACKAGES_CACHE_DURATION_MS
    }
    
    suspend fun getAllowedPackagesSync(): Set<String> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.ALLOWED_PACKAGES] ?: emptySet()
        }.first()
    }
    
    // ===== Domain Whitelist for VPN DNS Filtering =====
    
    val allowedDomains: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.ALLOWED_DOMAINS] ?: emptySet()
    }
    
    // Parent-added domains only (for display in AllowedSitesScreen)
    val parentDomains: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.PARENT_DOMAINS] ?: emptySet()
    }
    
    suspend fun updateAllowedDomains(domains: Set<String>, version: Long, parentDomains: Set<String>? = null) {
        Log.i(TAG, "======== SAVING DOMAINS TO CACHE ========")
        Log.i(TAG, "Domain count: ${domains.size}")
        Log.i(TAG, "Parent domains: ${parentDomains?.size ?: 0}")
        Log.i(TAG, "Version: $version")
        Log.i(TAG, "Sample: ${domains.take(5)}")
        context.dataStore.edit { prefs ->
            prefs[Keys.ALLOWED_DOMAINS] = domains
            prefs[Keys.DOMAINS_VERSION] = version
            prefs[Keys.LAST_DOMAINS_SYNC] = System.currentTimeMillis()
            if (parentDomains != null) {
                prefs[Keys.PARENT_DOMAINS] = parentDomains
            }
        }
        Log.i(TAG, "Domains saved to DataStore successfully")
        Log.i(TAG, "=========================================")
    }
    
    suspend fun getAllowedDomainsSync(): Set<String> {
        val domains = context.dataStore.data.map { prefs ->
            prefs[Keys.ALLOWED_DOMAINS] ?: emptySet()
        }.first()
        Log.d(TAG, "getAllowedDomainsSync: ${domains.size} domains")
        if (domains.isEmpty()) {
            Log.w(TAG, "WARNING: No domains in cache!")
        }
        return domains
    }
    
    suspend fun getDomainsVersion(): Long {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.DOMAINS_VERSION] ?: 0L
        }.first()
    }
    
    suspend fun isDomainsCacheValid(): Boolean {
        val lastSync = context.dataStore.data.map { prefs ->
            prefs[Keys.LAST_DOMAINS_SYNC] ?: 0L
        }.first()
        
        // Also check if domains are empty - force sync if no domains cached
        val domains = getAllowedDomainsSync()
        if (domains.isEmpty()) {
            Log.w(TAG, "isDomainsCacheValid: FALSE - domains empty!")
            return false  // Force sync if no domains
        }
        
        val isValid = System.currentTimeMillis() - lastSync < DOMAINS_CACHE_DURATION_MS
        val ageMinutes = (System.currentTimeMillis() - lastSync) / 60000
        Log.d(TAG, "isDomainsCacheValid: $isValid (age: ${ageMinutes}min, domains: ${domains.size})")
        return isValid
    }
    
    suspend fun setLastDomainsSync(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_DOMAINS_SYNC] = timestamp
        }
    }
}
