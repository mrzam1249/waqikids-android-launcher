package com.waqikids.launcher.data.api

import com.waqikids.launcher.data.api.dto.AllowedPackagesResponse
import com.waqikids.launcher.data.api.dto.FcmTokenRequest
import com.waqikids.launcher.data.api.dto.HeartbeatRequest
import com.waqikids.launcher.data.api.dto.HeartbeatResponse
import com.waqikids.launcher.data.api.dto.PairRequest
import com.waqikids.launcher.data.api.dto.PairResponse
import com.waqikids.launcher.data.api.dto.PairingStatusResponse
import com.waqikids.launcher.data.api.dto.ParentPinResponse
import com.waqikids.launcher.data.api.dto.PinVerifyRequest
import com.waqikids.launcher.data.api.dto.PinVerifyResponse
import com.waqikids.launcher.data.api.dto.SyncAppsRequest
import com.waqikids.launcher.data.api.dto.SyncAppsResponse
import com.waqikids.launcher.data.api.dto.WhitelistSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WaqiApi {
    
    /**
     * Pair device using 6-digit code from parent app
     * Backend: POST /api/devices/pair
     */
    @POST("devices/pair")
    suspend fun pairDevice(@Body request: PairRequest): Response<PairResponse>
    
    /**
     * Check if device is still paired with parent
     * Backend: GET /api/device/pairing-status/:device_id
     */
    @GET("device/pairing-status/{deviceId}")
    suspend fun getPairingStatus(@Path("deviceId") deviceId: String): Response<PairingStatusResponse>
    
    /**
     * Sync installed apps to backend (with full metadata including icons)
     * Backend: POST /api/device/installed-apps
     */
    @POST("device/installed-apps")
    suspend fun syncApps(@Body request: SyncAppsRequest): Response<SyncAppsResponse>
    
    /**
     * Get list of allowed package names for this device
     * Backend: GET /api/device/{device_id}/allowed-packages
     */
    @GET("device/{deviceId}/allowed-packages")
    suspend fun getAllowedPackages(@Path("deviceId") deviceId: String): Response<AllowedPackagesResponse>
    
    /**
     * Send heartbeat and check if still paired
     * Backend: POST /api/device/heartbeat
     */
    @POST("device/heartbeat")
    suspend fun heartbeat(@Body request: HeartbeatRequest): Response<HeartbeatResponse>
    
    /**
     * Register FCM token for push notifications
     * Backend: POST /api/device/fcm-token
     */
    @POST("device/fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): Response<Unit>
    
    /**
     * Verify parent PIN for device unlock
     * Backend: POST /api/pin/verify
     */
    @POST("pin/verify")
    suspend fun verifyParentPin(@Body request: PinVerifyRequest): Response<PinVerifyResponse>
    
    /**
     * Get parent PIN hash for local caching
     * Backend: GET /api/device/{device_id}/parent-pin
     */
    @GET("device/{deviceId}/parent-pin")
    suspend fun getParentPinHash(@Path("deviceId") deviceId: String): Response<ParentPinResponse>
    
    /**
     * Sync domain whitelist for VPN DNS filtering
     * Returns all allowed domains: infrastructure + family + child-specific
     * Backend: GET /api/whitelist/sync/{device_id}
     */
    @GET("whitelist/sync/{deviceId}")
    suspend fun syncWhitelist(@Path("deviceId") deviceId: String): Response<WhitelistSyncResponse>
}
