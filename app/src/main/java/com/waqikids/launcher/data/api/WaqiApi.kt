package com.waqikids.launcher.data.api

import com.waqikids.launcher.data.api.dto.HeartbeatRequest
import com.waqikids.launcher.data.api.dto.HeartbeatResponse
import com.waqikids.launcher.data.api.dto.PairRequest
import com.waqikids.launcher.data.api.dto.PairResponse
import com.waqikids.launcher.data.api.dto.SyncAppsRequest
import com.waqikids.launcher.data.api.dto.SyncAppsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WaqiApi {
    
    /**
     * Pair device using 6-digit code from parent app
     */
    @POST("child/pair")
    suspend fun pairDevice(@Body request: PairRequest): Response<PairResponse>
    
    /**
     * Get current configuration for device
     */
    @GET("child/config/{deviceId}")
    suspend fun getConfig(@Path("deviceId") deviceId: String): Response<PairResponse>
    
    /**
     * Sync installed apps to backend
     */
    @POST("child/apps")
    suspend fun syncApps(@Body request: SyncAppsRequest): Response<SyncAppsResponse>
    
    /**
     * Send heartbeat and get updates
     */
    @POST("child/heartbeat")
    suspend fun heartbeat(@Body request: HeartbeatRequest): Response<HeartbeatResponse>
}
