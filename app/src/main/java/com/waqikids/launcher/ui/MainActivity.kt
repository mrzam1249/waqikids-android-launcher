package com.waqikids.launcher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.ui.navigation.Screen
import com.waqikids.launcher.ui.navigation.WaqiNavHost
import com.waqikids.launcher.ui.theme.BackgroundStart
import com.waqikids.launcher.ui.theme.WaqiKidsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            WaqiKidsTheme {
                val isSetupComplete by preferencesManager.isSetupComplete.collectAsState(initial = false)
                
                val startDestination = if (isSetupComplete) {
                    Screen.Launcher.route
                } else {
                    Screen.Splash.route
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
    
    // Disable back button when launcher is active
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent going back from home screen
    }
}
