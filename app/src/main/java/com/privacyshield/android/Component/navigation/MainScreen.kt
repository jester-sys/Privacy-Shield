package com.privacyshield.android.Component.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.privacyshield.android.Component.MemoryManager.MemoryManagerScreen
import com.privacyshield.android.Component.Scanner.CONTACTS.ContactCleanerScreen
import com.privacyshield.android.Component.Scanner.FileDetailScreen
import com.privacyshield.android.Component.Scanner.FileScanScreen
import com.privacyshield.android.Component.Scanner.ScannerScreen
import com.privacyshield.android.Component.Scanner.Whatsapp.CleanWhatsAppMediaScreen
import com.privacyshield.android.Component.Scanner.Whatsapp.WhatsAppCleanerViewModel
import com.privacyshield.android.Component.Scanner.unusgeApp.IgnoredAppsScreen
import com.privacyshield.android.Component.Scanner.unusgeApp.UnusedAppsScreen
import com.privacyshield.android.Component.Screen.Deatils.DetailsScreen
import com.privacyshield.android.Component.Screen.Deatils.utility.AppMoreMenu
import com.privacyshield.android.Component.Screen.Home.Action.AppDataUsageCard
import com.privacyshield.android.Component.Screen.Home.Action.ManagePermissions
import com.privacyshield.android.Component.Screen.Home.Action.StorageUsageDialog
import com.privacyshield.android.Component.Screen.Home.Action.manageOpenByDefault
import com.privacyshield.android.Component.Screen.Home.Action.openApp
import com.privacyshield.android.Component.Screen.Home.Action.shareApp
import com.privacyshield.android.Component.Screen.Home.Action.showBatteryUsage
import com.privacyshield.android.Component.Screen.Home.Action.showStorageUsage
import com.privacyshield.android.Component.Screen.Home.Action.uninstallApp
import com.privacyshield.android.Component.Screen.Home.HomeScreen
import com.privacyshield.android.Component.Screen.Model.StorageUsage
import com.privacyshield.android.Component.Screen.Overview.OverviewScreen
import com.privacyshield.android.Component.Screen.Permission.PermissionDetailsScreen
import com.privacyshield.android.Component.Screen.UsageStatsScreen.AppUsageDetailsScreen
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.ui.theme.BluePrimary
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, activity: Activity) {
    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Scanner,
        BottomNavItem.Overview,
        BottomNavItem.MemoryManager
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar =
        currentRoute?.startsWith("permission_details") == false &&
                currentRoute != "details" && currentRoute != "UsageDetail" && currentRoute != "full_file_screen"
                && currentRoute!= "ignoredApps" && currentRoute!= "clean_whatsapp_media" && currentRoute != "unused_apps_screen"

    var showAppMenu by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppDetail?>(null) }
    val context = LocalContext.current


    // Dialog states
    var showDataUsageDialog by remember { mutableStateOf(false) }
    var showBatteryUsageDialog by remember { mutableStateOf(false) }
    var showStorageUsageDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var storageUsage by remember { mutableStateOf<StorageUsage?>(null) }
    // ✅ Sort option state yahin maintain karenge
    var sortOption by remember { mutableStateOf("Name") }
    var expanded by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(120.dp) } // initial screen ke hisaab se
    var gridIcon by remember { mutableStateOf(
        if (120.dp == 120.dp) Icons.Default.GridView else Icons.Default.ViewAgenda
    )}


    // Inside NavHost composable("details") { ... }
    val app = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<AppDetail>("selectedApp")



// Assign app to MainScreen state for bottom sheet
    LaunchedEffect(app) {
        if (app != null) selectedApp = app
    }

    if (showDataUsageDialog) {
        app?.let {
            AppDataUsageCard(context, it, onDismiss = {
                showDataUsageDialog = false
            })
        } // Composable dialog
    }
    if (showBatteryUsageDialog) {
        if (app != null) {
            showBatteryUsage(context, app)
        }


    }
    if (showStorageUsageDialog) {
        app?.let { nonNullApp ->
            LaunchedEffect(nonNullApp) {
                storageUsage = showStorageUsage(context, nonNullApp)
            }

            storageUsage?.let { usage ->
                StorageUsageDialog(
                    context = context,
                    usage = usage,
                    app = nonNullApp,
                    onDismiss = {
                        showStorageUsageDialog = false
                        storageUsage = null
                    }
                )
            }
        }
    }

    if (showPermissionsDialog) {
        ManagePermissions(
            context = context,
            app = app!!,
            showDialog = showPermissionsDialog,
            onDismiss = { showPermissionsDialog = false }
        )
    }



    Scaffold(

        topBar = {
                when {
                    currentRoute == "details" -> {
                        TopAppBar(
                            title = { Text("App Details", color = Color.White) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {


                                IconButton(onClick = {
                                    // App ko navController ke savedStateHandle me bhej do
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "selectedApp",
                                        app
                                    )
                                    navController.navigate("UsageDetail")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Insights",
                                        tint = Color.White
                                    )

                                }

                                IconButton(onClick = {
                                    try {
                                        val intent =
                                            Intent("android.intent.action.MANAGE_APP_PERMISSIONS").apply {
                                                data =
                                                    Uri.fromParts("package", app?.packageName, null)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        val fallbackIntent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${app?.packageName}")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        context.startActivity(fallbackIntent)
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Settings",
                                        tint = Color.White
                                    )
                                }
                                selectedApp?.let { app ->
                                    AppMoreMenu(
                                        app = app,
                                        onAction = { clickedApp, action ->
                                            when (action) {
                                                "data_usage" -> showDataUsageDialog = true
                                                "battery_usage" -> showBatteryUsageDialog = true
                                                "storage_usage" -> showStorageUsageDialog = true
                                                "permissions" -> showPermissionsDialog = true
                                                "open_by_default" -> manageOpenByDefault(context, clickedApp)
                                                "open" -> openApp(context, clickedApp)
                                                "uninstall" -> uninstallApp(activity, clickedApp)
                                                "share" -> shareApp(context, clickedApp)
                                            }
                                        }
                                    )
                                }

                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(
                                    0xFF1E1E1E
                                )
                            )
                        )
                    }


                    currentRoute == "full_file_screen" -> {
                        val type = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<String>("type") ?: "unknown"

                        val title = when (type) {
                            "image" -> "Images"
                            "video" -> "Videos"
                            "document" -> "Documents"
                            "sticker" -> "Stickers"
                            "audio" -> "Voice Notes"
                            "gif" -> "GIFs"
                            "status" -> "Status"
                            else -> "Files"
                        }
                        TopAppBar(
                            title = { Text(title, color = Color.White) },
                            navigationIcon = {


                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    // Toggle logic
                                    if (gridColumns == 120.dp) {
                                        gridColumns = 150.dp
                                        gridIcon = Icons.Default.ViewAgenda
                                    } else {
                                        gridColumns = 120.dp // screen ke hisaab se reset
                                        gridIcon = if (120.dp == 120.dp) Icons.Default.GridView else Icons.Default.ViewAgenda
                                    }
                                }) {
                                    Icon(imageVector = gridIcon, contentDescription = "Toggle Grid")
                                }

                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort",
                                        tint = Color.White
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .shadow(4.dp, RoundedCornerShape(16.dp)) // thoda shadow premium look ke liye
                                        .background(
                                            color = Color(0xFF3A3A3A), // 👈 theme se background
                                            shape = RoundedCornerShape(16.dp)         // 👈 round corners
                                        )
                                ){
                                    DropdownMenuItem(
                                        text = { Text("Name") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.SortByAlpha,
                                                contentDescription = null,
                                                tint = Color(0xFF2196F3) // 👈 blue tint for style
                                            )
                                        },
                                        onClick = {
                                            expanded = false
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("sortOption", "Name")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Date") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50) // 👈 green tint
                                            )
                                        },
                                        onClick = {
                                            expanded = false
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("sortOption", "Date")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Size") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = Color(0xFFFF9800) // 👈 orange tint
                                            )
                                        },
                                        onClick = {
                                            expanded = false
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("sortOption", "Size")
                                        }
                                    )
                                }


                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF1E1E1E)
                            )
                        )
                    }

                    currentRoute?.startsWith("permission_details") == true -> {
                        TopAppBar(
                            title = { Text("Permission Details", color = Color.White) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(
                                    0xFF1E1E1E
                                )
                            )
                        )
                    }

                    currentRoute == "overview" -> {
                        TopAppBar(
                            title = { Text("Device Info", color = Color.White) },
                            actions = {
                                IconButton(onClick = {
                                    navController.navigate("settings")
                                }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White
                                    )
                                }

                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(
                                    0xFF1E1E1E
                                )
                            )
                        )
                    }

                    currentRoute?.startsWith("UsageDetail") == true -> {
                        TopAppBar(
                            title = { Text("${app?.appName} Insights", color = Color.White) },
                            actions = {
                                IconButton(onClick = {
                                    val intent =
                                        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data =
                                        Uri.parse("package:${app?.packageName}") // yahan app ka package name
                                    context.startActivity(intent)

                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "settings",
                                        tint = Color.White
                                    )

                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(
                                    0xFF1E1E1E
                                )
                            )
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
            composable("unused_apps_screen"){
                UnusedAppsScreen(navController)
            }



            composable("full_file_screen") {
                val files = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<List<File>>("files") ?: emptyList()

                val type = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>("type") ?: "unknown"

                // ✅ Observe sortOption as StateFlow
                val sortOption by navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("sortOption", "Name")
                    ?.collectAsState() ?: remember { mutableStateOf("Name") }

                // ✅ Sorted list will recompute whenever sortOption or files change
                val sortedFiles = remember(sortOption, files) {
                    when (sortOption) {
                        "Name" -> files.sortedBy { it.name.lowercase() }
                        "Date" -> files.sortedByDescending { it.lastModified() }
                        "Size" -> files.sortedByDescending { it.length() }
                        else -> files
                    }
                }

                val title = when (type.lowercase()) {
                    "image" -> "Images"
                    "video" -> "Videos"
                    "document" -> "Documents"
                    "sticker" -> "Stickers"
                    "audio" -> "Voice Notes"
                    "gif" -> "GIFs"
                    "status" -> "Status"
                    else -> "Files"
                }



                FileDetailScreen(
                    title = title,
                    files = sortedFiles,
                    gridColumns = gridColumns,
                    type = type,
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }


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



            composable("file_scan") {
                FileScanScreen()
            }


            // Overview
            composable(BottomNavItem.Overview.route.route) { OverviewScreen() }
            composable(BottomNavItem.Scanner.route.route) { ScannerScreen(navController) }


            composable(BottomNavItem.MemoryManager.route.route) { MemoryManagerScreen() }

            // Permission Tab
          //  composable(BottomNavItem.Permission.route.route) { PermissionScreen() }

            // App Details

            composable("clean_whatsapp_media") {

                    CleanWhatsAppMediaScreen(navController)
            }

            composable("contact_cleaner_screen") {
                ContactCleanerScreen()
            }

            composable("ignoredApps") {
                IgnoredAppsScreen(navController)
            }

            composable("UsageDetail") {
                val app = navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("selectedApp")
                if(app!=null){
                AppUsageDetailsScreen(
                    context = context,
                    app = app,
                    navController = navController
                )
                }
            }
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
