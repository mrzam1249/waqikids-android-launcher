package com.waqikids.launcher.ui.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.waqikids.launcher.ui.theme.WaqiKidsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * AllowedSitesActivity - Kid-friendly Islamic browser homepage
 * 
 * Shows:
 * - Bismillah header
 * - Islamic greeting with child name
 * - Next prayer countdown
 * - Today's prayer times (horizontal scroll)
 * - Allowed websites grid
 * - Daily Quran verse for kids
 */
@AndroidEntryPoint
class AllowedSitesActivity : ComponentActivity() {
    
    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AllowedSitesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            WaqiKidsTheme {
                AllowedSitesScreen(
                    onWebsiteClick = { domain ->
                        // Open website in browser
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://$domain")
                        }
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            // No browser available
                        }
                    },
                    onGoHome = { finish() }
                )
            }
        }
    }
}
