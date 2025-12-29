package com.waqikids.launcher.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.waqikids.launcher.util.Constants

/**
 * Accessibility Service that monitors app launches and blocks access to:
 * - Settings app
 * - Play Store
 * - Other launcher apps
 * 
 * When a blocked app is detected, it immediately returns user to WaqiKids launcher
 * EXCEPTION: When Parent Mode is active (unlocked with PIN), these apps are allowed
 */
class WaqiAccessibilityService : AccessibilityService() {
    
    companion object {
        var isRunning = false
            private set
        
        // Parent Mode expiry timestamp (set when parent unlocks with PIN)
        var parentModeExpiresAt: Long = 0L
        
        fun isParentModeActive(): Boolean {
            return System.currentTimeMillis() < parentModeExpiresAt
        }
        
        fun setParentModeActive(durationMinutes: Int = 10) {
            parentModeExpiresAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        }
        
        fun clearParentMode() {
            parentModeExpiresAt = 0L
        }
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        
        serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val packageName = event.packageName?.toString() ?: return
        
        // Check if this is a blocked app
        if (shouldBlockApp(packageName)) {
            // Return to home screen (our launcher)
            goHome()
        }
    }
    
    private fun shouldBlockApp(packageName: String): Boolean {
        // If Parent Mode is active, allow Settings and Play Store
        if (isParentModeActive()) {
            // Only block other launchers during parent mode
            if (packageName.contains("launcher", ignoreCase = true) &&
                packageName != "com.waqikids.launcher") {
                return true
            }
            return false
        }
        
        // Normal mode - block these packages
        return packageName in Constants.BLOCKED_PACKAGES ||
                packageName == "com.android.settings" ||
                packageName == "com.android.vending" ||  // Play Store
                packageName.contains("launcher", ignoreCase = true) &&
                packageName != "com.waqikids.launcher"
    }
    
    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }
    
    override fun onInterrupt() {
        // Required override
    }
    
    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }
}
