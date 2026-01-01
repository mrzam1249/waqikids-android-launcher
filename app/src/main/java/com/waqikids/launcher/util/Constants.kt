package com.waqikids.launcher.util

object Constants {
    // API Configuration - Direct connection to backend server
    const val API_BASE_URL = "http://178.156.160.245:8080/api/"
    const val DNS_BASE_DOMAIN = "dns.waqikids.com"
    
    // Timeouts - FCM push handles instant updates, polling is just for fallback
    const val SYNC_INTERVAL_MINUTES = 15L
    const val HEARTBEAT_INTERVAL_MINUTES = 5L
    
    // Package names to always block from launcher
    val BLOCKED_PACKAGES = setOf(
        "com.android.settings",
        "com.android.vending",           // Play Store
        "com.google.android.packageinstaller",
        "com.sec.android.app.launcher",  // Samsung launcher
        "com.google.android.apps.nexuslauncher",
        "com.miui.home",                 // Xiaomi launcher
        "com.huawei.android.launcher"    // Huawei launcher
    )
    
    // System apps that should always be hidden
    val SYSTEM_HIDDEN_PACKAGES = setOf(
        "com.android.settings",
        "com.android.vending",
        "com.google.android.gms",
        "com.google.android.gsf"
    )
    
    // Default allowed packages (browser for DNS filtering)
    val DEFAULT_ALLOWED_PACKAGES = setOf(
        "com.android.chrome",
        "com.google.android.youtube"
    )
}
