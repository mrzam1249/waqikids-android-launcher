package com.waqikids.launcher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.waqikids.launcher.ui.launcher.LauncherScreen
import com.waqikids.launcher.ui.onboarding.OnboardingScreen
import com.waqikids.launcher.ui.pairing.PairingScreen
import com.waqikids.launcher.ui.parentmode.ParentModeScreen
import com.waqikids.launcher.ui.setup.SetupWizardScreen
import com.waqikids.launcher.ui.splash.SplashScreen

@Composable
fun WaqiNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLauncher = {
                    navController.navigate(Screen.Launcher.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToPairing = {
                    navController.navigate(Screen.Pairing.route)
                }
            )
        }
        
        composable(Screen.Pairing.route) {
            PairingScreen(
                onPairingSuccess = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Pairing.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Setup.route) {
            SetupWizardScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Launcher.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Launcher.route) {
            LauncherScreen(
                onNavigateToParentMode = {
                    navController.navigate(Screen.ParentMode.route)
                }
            )
        }
        
        composable(Screen.ParentMode.route) {
            ParentModeScreen(
                onDismiss = {
                    navController.popBackStack()
                },
                onUnlocked = {
                    // Stay on parent mode screen but show unlocked content
                    // The screen handles this internally
                }
            )
        }
    }
}
