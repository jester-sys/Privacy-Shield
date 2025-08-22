package com.privacyshield.android.Component.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Main : AppRoute("main")


    data object Home : AppRoute("home")
    data object Overview : AppRoute("overview")
    data object Permission : AppRoute("permission")
}
