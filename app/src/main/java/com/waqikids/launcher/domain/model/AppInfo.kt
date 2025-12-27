package com.waqikids.launcher.domain.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed app on the device
 */
data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable,
    val isSystemApp: Boolean = false,
    val isAllowed: Boolean = true
)

/**
 * Represents app info for syncing with backend (without drawable)
 */
data class AppSyncInfo(
    val packageName: String,
    val name: String,
    val iconBase64: String? = null,
    val isSystemApp: Boolean = false
)
