package com.privacyshield.android.Component.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


import androidx.navigation.compose.rememberNavController
import com.privacyshield.android.Component.Screen.SplashScreen

@Composable
fun AppNavGraph(startDestination: String = AppRoute.Splash.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        // Splash
        composable(AppRoute.Splash.route) {
            SplashScreen {
                navController.navigate(AppRoute.Main.route) {
                    popUpTo(AppRoute.Splash.route) { inclusive = true }
                }
            }
        }

        // Main with bottom nav
        composable(AppRoute.Main.route) {
            MainScreen(navController = rememberNavController())
        }
    }
}
