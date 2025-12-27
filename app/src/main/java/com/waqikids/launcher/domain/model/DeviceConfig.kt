package com.waqikids.launcher.domain.model

/**
 * Device configuration received from backend after pairing
 */
data class DeviceConfig(
    val deviceId: String,
    val childName: String,
    val parentId: String,
    val dnsSubdomain: String,
    val protectionMode: ProtectionMode = ProtectionMode.EASY,
    val allowedPackages: List<String> = emptyList(),
    val isPaired: Boolean = false,
    val isSetupComplete: Boolean = false
)

/**
 * Protection mode for the device
 */
enum class ProtectionMode {
    EASY,       // Can be bypassed with factory reset
    FORT_KNOX   // Device Owner mode - unbreakable
}

/**
 * Current setup step
 */
enum class SetupStep {
    ONBOARDING,
    PAIRING,
    SET_LAUNCHER,
    ENABLE_ACCESSIBILITY,
    CONFIGURE_DNS,
    SELECT_MODE,
    COMPLETE
}
