package com.waqikids.launcher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receiver that detects when apps are installed or uninstalled
 * Syncs the app list with backend when changes occur
 */
class PackageChangeReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return
        
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                // New app installed
                onPackageAdded(context, packageName)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                // App uninstalled
                onPackageRemoved(context, packageName)
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                // App updated
                onPackageUpdated(context, packageName)
            }
        }
    }
    
    private fun onPackageAdded(context: Context, packageName: String) {
        scope.launch {
            // TODO: Sync new app to backend
            // The parent will see it in their app list
            // By default new apps are hidden until parent approves
        }
    }
    
    private fun onPackageRemoved(context: Context, packageName: String) {
        scope.launch {
            // TODO: Notify backend that app was removed
        }
    }
    
    private fun onPackageUpdated(context: Context, packageName: String) {
        scope.launch {
            // TODO: Update app info on backend if needed
        }
    }
}
