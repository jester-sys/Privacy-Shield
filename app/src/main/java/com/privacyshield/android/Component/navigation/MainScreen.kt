package com.privacyshield.android.Component.navigation

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

    // ✅ Hide bottom bar when route is "details"
    val shouldShowBottomBar = currentRoute !in listOf("details")
    Scaffold(
        topBar = {
            if (currentRoute == "details") {
                TopAppBar(
                    title = {
                        Text(
                            "App Details",
                            color = Color.White // Title ka color white
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White // Back arrow white
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1E1E1E),
                    tonalElevation = 6.dp ,
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
                            label = {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.title
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BluePrimary,               // active icon → blue
                                unselectedIconColor = Color(0xFF9E9E9E),       // inactive icon → light grey
                                selectedTextColor = BluePrimary,               // active text → blue
                                unselectedTextColor = Color(0xFF9E9E9E),       // inactive text → light grey
                                indicatorColor = BluePrimary.copy(alpha = 0.2f) // selection indicator (subtle blue bg)
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
