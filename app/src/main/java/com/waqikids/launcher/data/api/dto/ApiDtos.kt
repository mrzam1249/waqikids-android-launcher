package com.waqikids.launcher.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Request to pair device with parent account
 * Matches backend: POST /api/devices/pair
 */
data class PairRequest(
    @SerializedName("child_device_id")
    val childDeviceId: String,
    
    @SerializedName("child_device_name")
    val childDeviceName: String,
    
    @SerializedName("platform")
    val platform: String = "android",
    
    @SerializedName("pairing_code")
    val pairingCode: String
)

/**
 * Response after successful pairing
 * Matches backend response from /api/devices/pair
 */
data class PairResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("parent_id")
    val parentId: String?,
    
    @SerializedName("child_device_id")
    val childDeviceId: String?,
    
    @SerializedName("subscription_status")
    val subscriptionStatus: String?,
    
    @SerializedName("error")
    val error: String?
)

/**
 * Response from pairing status check
 * Matches backend: GET /api/device/pairing-status/:device_id
 */
data class PairingStatusResponse(
    @SerializedName("paired")
    val paired: Boolean,
    
    @SerializedName("parent_id")
    val parentId: String?,
    
    @SerializedName("parent_email")
    val parentEmail: String?,
    
    @SerializedName("parent_name")
    val parentName: String?,
    
    @SerializedName("child_nickname")
    val childNickname: String?,
    
    @SerializedName("safety_mode")
    val safetyMode: String?,
    
    @SerializedName("subscription_status")
    val subscriptionStatus: String?,
    
    @SerializedName("subscription_active")
    val subscriptionActive: Boolean?,
    
    @SerializedName("days_remaining")
    val daysRemaining: Int?,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("forced_lock")
    val forcedLock: Boolean?,
    
    @SerializedName("lock_reason")
    val lockReason: String?
)

/**
 * Request to sync installed apps
 * Matches backend: POST /api/device/installed-apps
 */
data class SyncAppsRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("apps")
    val apps: List<String>  // Just package names for now
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
    val success: Boolean
)

/**
 * Heartbeat request to keep connection alive
 * Matches backend: POST /api/device/heartbeat
 */
data class HeartbeatRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("parent_id")
    val parentId: String? = null,
    
    @SerializedName("device_name")
    val deviceName: String? = null,
    
    @SerializedName("device_type")
    val deviceType: String? = null,
    
    @SerializedName("platform")
    val platform: String = "android"
)

/**
 * Heartbeat response with pairing status
 * Matches backend response from /api/device/heartbeat
 */
data class HeartbeatResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("paired")
    val paired: Boolean?
)

/**
 * Request to register FCM token for push notifications
 * Matches backend: POST /api/device/fcm-token
 */
data class FcmTokenRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("fcm_token")
    val fcmToken: String,
    
    @SerializedName("platform")
    val platform: String = "android"
)

/**
 * Request to verify parent PIN for device unlock
 * Matches backend: POST /api/pin/verify
 */
data class PinVerifyRequest(
    @SerializedName("child_device_id")
    val childDeviceId: String,
    
    @SerializedName("pin")
    val pin: String
)

/**
 * Response from PIN verification
 */
data class PinVerifyResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("valid")
    val valid: Boolean?,
    
    @SerializedName("parent_id")
    val parentId: String?,
    
    @SerializedName("parent_mode_expires")
    val parentModeExpires: String?,
    
    @SerializedName("pin_hash")
    val pinHash: String?,  // For local caching
    
    @SerializedName("error")
    val error: String?,
    
    @SerializedName("attempts_remaining")
    val attemptsRemaining: Int?,
    
    @SerializedName("retry_after_minutes")
    val retryAfterMinutes: Int?
)

/**
 * Response from fetching parent PIN hash
 * Matches backend: GET /api/device/{device_id}/parent-pin
 */
data class ParentPinResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("parent_id")
    val parentId: String?,
    
    @SerializedName("pin_hash")
    val pinHash: String?,
    
    @SerializedName("has_pin")
    val hasPin: Boolean?,
    
    @SerializedName("error")
    val error: String?
)
