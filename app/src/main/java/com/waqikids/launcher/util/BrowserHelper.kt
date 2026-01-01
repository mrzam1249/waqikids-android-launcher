package com.waqikids.launcher.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility for browser-related operations
 */
object BrowserHelper {
    
    private const val HOME_PAGE_URL = "http://178.156.160.245:8081"
    
    /**
     * Opens the WaqiKids homepage in any available browser.
     * If no browser is installed, shows a kid-friendly message.
     * 
     * @return true if browser was opened, false if no browser available
     */
    fun openHomePage(context: Context): Boolean {
        return openUrl(context, HOME_PAGE_URL)
    }
    
    /**
     * Opens a URL in any available browser.
     * If no browser is installed, shows a message asking parent to install one.
     * 
     * @return true if browser was opened, false if no browser available
     */
    fun openUrl(context: Context, url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        return try {
            // Check if any app can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                showNoBrowserMessage(context)
                false
            }
        } catch (e: ActivityNotFoundException) {
            showNoBrowserMessage(context)
            false
        }
    }
    
    /**
     * Checks if any browser is available on the device
     */
    fun hasBrowser(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"))
        return intent.resolveActivity(context.packageManager) != null
    }
    
    /**
     * Shows a kid-friendly message when no browser is installed
     */
    private fun showNoBrowserMessage(context: Context) {
        Toast.makeText(
            context,
            "🌐 Ask your parent to install a web browser!",
            Toast.LENGTH_LONG
        ).show()
    }
}
