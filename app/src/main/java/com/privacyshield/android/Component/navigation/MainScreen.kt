package com.privacyshield.android.Component.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.privacyshield.android.Component.Screen.HomeScreen
import com.privacyshield.android.Component.Screen.OverviewScreen
import com.privacyshield.android.Component.Screen.PermissionScreen
import com.privacyshield.android.ui.theme.BackgroundLight
import com.privacyshield.android.ui.theme.BluePrimary
import com.privacyshield.android.ui.theme.BlueSecondary
import com.privacyshield.android.ui.theme.HighRisk
import com.privacyshield.android.ui.theme.TextPrimaryLight
import com.privacyshield.android.ui.theme.TextSecondaryLight

@Composable
fun MainScreen(navController: NavHostController) {
    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Overview,
        BottomNavItem.Permission
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor =  BackgroundLight
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route.route, // 👈 fix here
                        onClick = {
                            navController.navigate(item.route.route) { // 👈 fix here
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.title) },
                        icon = {   Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title
                        ) },
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
            composable(BottomNavItem.Home.route.route) { HomeScreen() }
            composable(BottomNavItem.Overview.route.route) { OverviewScreen() }
            composable(BottomNavItem.Permission.route.route) { PermissionScreen() }
        }
    }
}