package com.waqikids.launcher.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device Admin Receiver for Fort Knox mode
 * Provides device policy management capabilities
 */
class WaqiDeviceAdminReceiver : DeviceAdminReceiver() {
    
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Fort Knox protection enabled", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Protection disabled", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling protection will allow unrestricted access to this device. Are you sure?"
    }
}
