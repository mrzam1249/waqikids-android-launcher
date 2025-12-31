package com.waqikids.launcher.ui

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.service.DnsVpnService
import com.waqikids.launcher.service.SyncService
import com.waqikids.launcher.ui.navigation.Screen
import com.waqikids.launcher.ui.navigation.WaqiNavHost
import com.waqikids.launcher.ui.theme.BackgroundStart
import com.waqikids.launcher.ui.theme.WaqiKidsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    // VPN permission launcher
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.i(TAG, "VPN permission granted, starting VPN service")
            startVpnService()
        } else {
            Log.w(TAG, "VPN permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.i(TAG, "========== MAINACTIVITY CREATED ==========")
        
        // Observe setup completion and start VPN when it becomes true
        // This handles both: 1) App restart after setup, 2) First time setup completion
        lifecycleScope.launch {
            preferencesManager.isSetupComplete.collect { isComplete ->
                Log.i(TAG, "isSetupComplete changed to: $isComplete")
                if (isComplete) {
                    Log.i(TAG, "Setup is complete - preparing VPN and SyncService")
                    prepareAndStartVpn()
                }
            }
        }
        
        // Log current state
        lifecycleScope.launch {
            val deviceId = preferencesManager.getDeviceId()
            val isPaired = preferencesManager.isPaired.first()
            val isSetupComplete = preferencesManager.isSetupComplete.first()
            Log.i(TAG, "========== DEVICE STATE ==========")
            Log.i(TAG, "Device ID: $deviceId")
            Log.i(TAG, "Is Paired: $isPaired")
            Log.i(TAG, "Is Setup Complete: $isSetupComplete")
            Log.i(TAG, "===================================")
        }
        
        setContent {
            WaqiKidsTheme {
                val isSetupComplete by preferencesManager.isSetupComplete.collectAsState(initial = false)
                val isPaired by preferencesManager.isPaired.collectAsState(initial = false)
                
                val startDestination = when {
                    isSetupComplete -> Screen.Launcher.route
                    isPaired -> Screen.Setup.route  // Already paired, go directly to setup
                    else -> Screen.Splash.route
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundStart
                ) {
                    val navController = rememberNavController()
                    WaqiNavHost(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
    
    private fun prepareAndStartVpn() {
        Log.i(TAG, "======== PREPARING VPN ========")
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            // Need to ask user for VPN permission
            Log.i(TAG, "VPN permission NOT granted yet, requesting from user...")
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            // Already have permission, start VPN
            Log.i(TAG, "VPN permission already granted, starting services...")
            startVpnService()
        }
    }
    
    private fun startVpnService() {
        Log.i(TAG, "======== STARTING SERVICES ========")
        
        // Start VPN service
        val intent = Intent(this, DnsVpnService::class.java).apply {
            action = DnsVpnService.ACTION_START
        }
        startForegroundService(intent)
        Log.i(TAG, ">>> DnsVpnService started")
        
        // Also start SyncService to register FCM token and sync domains
        val syncIntent = Intent(this, SyncService::class.java)
        startForegroundService(syncIntent)
        Log.i(TAG, ">>> SyncService started")
        
        Log.i(TAG, "Both services should now be running!")
        Log.i(TAG, "Check logcat for: DnsVpnService, SyncService tags")
        Log.i(TAG, "===================================")
    }
    
    // Disable back button when launcher is active
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent going back from home screen
    }
}
