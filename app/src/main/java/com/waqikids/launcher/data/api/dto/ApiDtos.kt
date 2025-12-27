package com.waqikids.launcher.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Request to pair device with parent account
 */
data class PairRequest(
    @SerializedName("pairing_code")
    val pairingCode: String,
    
    @SerializedName("device_info")
    val deviceInfo: DeviceInfo
)

data class DeviceInfo(
    @SerializedName("manufacturer")
    val manufacturer: String,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("os_version")
    val osVersion: String,
    
    @SerializedName("app_version")
    val appVersion: String
)

/**
 * Response after successful pairing
 */
data class PairResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("device_id")
    val deviceId: String?,
    
    @SerializedName("child_name")
    val childName: String?,
    
    @SerializedName("parent_id")
    val parentId: String?,
    
    @SerializedName("dns_subdomain")
    val dnsSubdomain: String?,
    
    @SerializedName("allowed_packages")
    val allowedPackages: List<String>?,
    
    @SerializedName("protection_mode")
    val protectionMode: String?,
    
    @SerializedName("error")
    val error: String?
)

/**
 * Request to sync installed apps
 */
data class SyncAppsRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("apps")
    val apps: List<AppInfoDto>
)

data class AppInfoDto(
    @SerializedName("package_name")
    val packageName: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("icon_base64")
    val iconBase64: String?,
    
    @SerializedName("is_system")
    val isSystem: Boolean
)

/**
 * Response with updated allowed packages
 */
data class SyncAppsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("allowed_packages")
    val allowedPackages: List<String>?
)

/**
 * Heartbeat request to keep connection alive
 */
data class HeartbeatRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("battery_level")
    val batteryLevel: Int?,
    
    @SerializedName("screen_time_today")
    val screenTimeToday: Int?  // minutes
)

/**
 * Heartbeat response with any updates
 */
data class HeartbeatResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("allowed_packages")
    val allowedPackages: List<String>?,
    
    @SerializedName("is_locked")
    val isLocked: Boolean?,
    
    @SerializedName("daily_limit_minutes")
    val dailyLimitMinutes: Int?,
    
    @SerializedName("message")
    val message: String?  // Message from parent
)
