package com.waqikids.launcher.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Pairing : Screen("pairing")
    data object Setup : Screen("setup")
    data object Launcher : Screen("launcher")
    data object Locked : Screen("locked")
}
