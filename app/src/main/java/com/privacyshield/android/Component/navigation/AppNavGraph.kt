package com.privacyshield.android.Component.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.privacyshield.android.Component.Screen.Deatils.DetailsScreen
import com.privacyshield.android.Component.Screen.SplashScreen
import com.privacyshield.android.Model.AppDetail

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
        composable(
            route = "${AppRoute.Details.route}/{app}",
            arguments = listOf(navArgument("app") { type = NavType.ParcelableType(AppDetail::class.java) })
        ) { backStackEntry ->
            val appDetail = backStackEntry.arguments?.getParcelable<AppDetail>("app")
            appDetail?.let {
                DetailsScreen(it)
            }
        }

    }
}
