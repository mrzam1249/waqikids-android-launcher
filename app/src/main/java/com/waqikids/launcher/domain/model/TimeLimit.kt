package com.waqikids.launcher.domain.model

/**
 * Time limit configuration for the device
 */
data class TimeLimit(
    val dailyLimitMinutes: Int = 120,     // 2 hours default
    val usedTodayMinutes: Int = 0,
    val isLocked: Boolean = false,
    val scheduleStart: String? = null,     // "08:00"
    val scheduleEnd: String? = null        // "21:00"
) {
    val remainingMinutes: Int
        get() = (dailyLimitMinutes - usedTodayMinutes).coerceAtLeast(0)
    
    val remainingFormatted: String
        get() {
            val hours = remainingMinutes / 60
            val minutes = remainingMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
    
    val isTimeUp: Boolean
        get() = remainingMinutes <= 0
}
