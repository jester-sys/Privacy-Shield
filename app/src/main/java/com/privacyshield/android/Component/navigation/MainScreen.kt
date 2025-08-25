package com.privacyshield.android.Component.navigation

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.privacyshield.android.Component.Screen.Deatils.DetailsScreen
import com.privacyshield.android.Component.Screen.Home.HomeScreen
import com.privacyshield.android.Component.Screen.OverviewScreen
import com.privacyshield.android.Component.Screen.PermissionScreen
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.ui.theme.BackgroundLight
import com.privacyshield.android.ui.theme.BluePrimary
import com.privacyshield.android.ui.theme.SurfaceDark

@Composable
fun MainScreen(navController: NavHostController,activity: Activity) {
    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Overview,
        BottomNavItem.Permission
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF000000)) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route.route,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.title) },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = BluePrimary,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = BluePrimary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Home.route.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ✅ HomeScreen with navigation to details
            composable(BottomNavItem.Home.route.route) {
                HomeScreen(
                    activity = activity,
                    onAppClick = { app ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedApp", app)
                        navController.navigate("details")
                    }
                )
            }
            composable(BottomNavItem.Overview.route.route) { OverviewScreen() }
            composable(BottomNavItem.Permission.route.route) { PermissionScreen() }

            // ✅ DetailsScreen destination
            composable("details") {
                val app =
                    navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("selectedApp")
                if (app != null) {
                    DetailsScreen(app)
                }
            }
        }
    }
}
