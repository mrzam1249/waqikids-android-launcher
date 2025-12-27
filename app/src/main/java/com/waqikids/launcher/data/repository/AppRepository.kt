package com.waqikids.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.domain.model.AppInfo
import com.waqikids.launcher.domain.model.AppSyncInfo
import com.waqikids.launcher.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    private val packageManager: PackageManager = context.packageManager
    
    /**
     * Get all launchable apps installed on device
     */
    suspend fun getAllInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        
        resolveInfos.mapNotNull { resolveInfo ->
            try {
                val packageName = resolveInfo.activityInfo.packageName
                
                // Skip our own app
                if (packageName == context.packageName) return@mapNotNull null
                
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                
                AppInfo(
                    packageName = packageName,
                    name = resolveInfo.loadLabel(packageManager).toString(),
                    icon = resolveInfo.loadIcon(packageManager),
                    isSystemApp = isSystemApp,
                    isAllowed = true
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.name.lowercase() }
    }
    
    /**
     * Get only allowed apps (filtered by parent settings)
     */
    fun getAllowedApps(): Flow<List<AppInfo>> = combine(
        flowOf(Unit),
        preferencesManager.allowedPackages
    ) { _, allowedPackages ->
        val allApps = getAllInstalledApps()
        
        if (allowedPackages.isEmpty()) {
            // If no packages configured, show default safe apps
            allApps.filter { 
                it.packageName in Constants.DEFAULT_ALLOWED_PACKAGES &&
                it.packageName !in Constants.SYSTEM_HIDDEN_PACKAGES
            }
        } else {
            allApps.filter { 
                it.packageName in allowedPackages &&
                it.packageName !in Constants.BLOCKED_PACKAGES
            }
        }
    }
    
    /**
     * Get apps for syncing to backend (with base64 icons)
     */
    suspend fun getAppsForSync(): List<AppSyncInfo> = withContext(Dispatchers.IO) {
        getAllInstalledApps().map { app ->
            AppSyncInfo(
                packageName = app.packageName,
                name = app.name,
                iconBase64 = drawableToBase64(app.icon),
                isSystemApp = app.isSystemApp
            )
        }
    }
    
    /**
     * Launch an app by package name
     */
    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if a package is installed
     */
    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Convert drawable to base64 string for API sync
     */
    private fun drawableToBase64(drawable: Drawable): String? {
        return try {
            val bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth.coerceAtLeast(1)
                val height = drawable.intrinsicHeight.coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            
            // Scale down for efficiency
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 72, 72, true)
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
