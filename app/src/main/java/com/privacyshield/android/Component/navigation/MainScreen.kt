package com.privacyshield.android.Component.navigation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.privacyshield.android.Component.Screen.Deatils.DetailsScreen
import com.privacyshield.android.Component.Screen.Home.HomeScreen
import com.privacyshield.android.Component.Screen.OverviewScreen
import com.privacyshield.android.Component.Screen.Permission.PermissionDetailsScreen
import com.privacyshield.android.Component.Screen.PermissionScreen
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.ui.theme.BackgroundLight
import com.privacyshield.android.ui.theme.BluePrimary
import com.privacyshield.android.ui.theme.SurfaceDark


@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, activity: Activity) {
    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Overview,
        BottomNavItem.Permission
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar =
        currentRoute?.startsWith("permission_details") == false &&
                currentRoute != "details"

    Scaffold(
        topBar = {
            when {
                currentRoute == "details" -> {
                    TopAppBar(
                        title = { Text("App Details", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                    )
                }

                currentRoute?.startsWith("permission_details") == true -> {
                    TopAppBar(
                        title = { Text("Permission Details", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                    )
                }
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1E1E1E),
                    tonalElevation = 6.dp,
                    contentColor = Color.White
                ) {
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
                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.title
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BluePrimary,
                                unselectedIconColor = Color(0xFF9E9E9E),
                                selectedTextColor = BluePrimary,
                                unselectedTextColor = Color(0xFF9E9E9E),
                                indicatorColor = BluePrimary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Home.route.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home
            composable(BottomNavItem.Home.route.route) {
                HomeScreen(
                    activity = activity,
                    onAppClick = { app, allApps ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedApp", app)
                        navController.currentBackStackEntry?.savedStateHandle?.set("allApps", allApps)
                        navController.navigate("details")
                    }
                )
            }

            // Overview
            composable(BottomNavItem.Overview.route.route) { OverviewScreen() }

            // Permission Tab
          //  composable(BottomNavItem.Permission.route.route) { PermissionScreen() }

            // App Details
            composable("details") {
                val app = navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("selectedApp")
                val allApps = navController.previousBackStackEntry?.savedStateHandle?.get<List<AppDetail>>("allApps") ?: emptyList()

                if (app != null) {
                    DetailsScreen(
                        app = app,
                        navController = navController,
                        onPermissionClick = { permission ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("allApps", allApps)
                            navController.currentBackStackEntry?.savedStateHandle?.set("currentApp", app) // ✅ pure AppDetail bhej
                            navController.navigate("permission_details/$permission")
                        }
                    )
                }
            }

            // Permission Details
            composable(
                "permission_details/{permission}",
                arguments = listOf(navArgument("permission") { type = NavType.StringType })
            ) { backStackEntry ->
                val permission = backStackEntry.arguments?.getString("permission")
                val allApps = navController.previousBackStackEntry?.savedStateHandle?.get<List<AppDetail>>("allApps") ?: emptyList()
                val currentApp = navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("currentApp")
                val clickedApp = navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("selectedApp")

                if (permission != null) {
                    PermissionDetailsScreen(
                        permission = permission,
                        allApps = allApps,
                        clickedApp = clickedApp,
                        currentApp = currentApp?.packageName,
                        onAppClick = { app ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedApp", app)
                            navController.currentBackStackEntry?.savedStateHandle?.set("allApps", allApps)
                            navController.navigate("details")
                        }
                    )
                }
            }
        }
    }
}
