package com.waqikids.launcher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver that starts our launcher when device boots
 * Ensures protection is active immediately after restart
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "android.intent.action.REBOOT") {
            
            // Start the launcher
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(launcherIntent)
            
            // Start the sync service
            val serviceIntent = Intent(context, SyncService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
