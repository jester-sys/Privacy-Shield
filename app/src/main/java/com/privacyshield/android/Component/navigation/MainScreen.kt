package com.privacyshield.android.Component.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
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
import com.privacyshield.android.Component.Settings.AppSecurity.AppSecurityScreen
import com.privacyshield.android.Component.Settings.DownloadScreen
import com.privacyshield.android.Component.Settings.PrivacySecurityScreen
import com.privacyshield.android.Component.Settings.SettingsScreen
import com.privacyshield.android.Component.Settings.Trash.TrashScreen
import com.privacyshield.android.Component.Settings.theme.Appearance
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import com.privacyshield.android.ViewModel.MainViewModel
import com.privacyshield.android.ui.theme.GreenPrimary
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, activity: Activity,mainViewModel: MainViewModel) {
    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Scanner,
        BottomNavItem.Overview,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val detections by mainViewModel.detections.collectAsState()


    val shouldShowBottomBar =
        currentRoute?.startsWith("permission_details") == false &&
                currentRoute != "details" && currentRoute != "UsageDetail" && currentRoute != "full_file_screen" &&
                currentRoute != "ignoredApps" && currentRoute != "clean_whatsapp_media" && currentRoute != "unused_apps_screen" &&
                currentRoute != "file_scan" // Added file_scan to hide bottom bar

    val shouldShowScaffold = currentRoute != "file_scan" // Hide entire scaffold for file_scan

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
    val colorScheme = MaterialTheme.colorScheme
    val appSettings = LocalAppSettings.current
    val primaryColor = colorScheme.primary
    val backgroundColor = resolveBackgroundColor(appSettings, primaryColor)
    val textColor = resolveTextColor(appSettings, colorScheme)
    var gridColumns by remember { mutableStateOf(120.dp) } // initial screen ke hisaab se
    var gridIcon by remember {
        mutableStateOf(
            if (120.dp == 120.dp) Icons.Default.GridView else Icons.Default.ViewAgenda
        )
    }

    // Inside NavHost composable("details") { ... }
    val app = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<AppDetail>("selectedApp")

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

    if (shouldShowScaffold) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            topBar = {
                when {
                    currentRoute == "details" -> {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            title = {
                                Text("App Details", color = MaterialTheme.colorScheme.onPrimary)
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "selectedApp",
                                        app
                                    )
                                    navController.navigate("UsageDetail")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Insights",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                IconButton(onClick = {
                                    try {
                                        val intent =
                                            Intent("android.intent.action.MANAGE_APP_PERMISSIONS").apply {
                                                data = Uri.fromParts("package", app?.packageName, null)
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
                                        tint = MaterialTheme.colorScheme.onPrimary
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
                            }
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
                            title = {
                                Text(title, color = MaterialTheme.colorScheme.onPrimary)
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    if (gridColumns == 120.dp) {
                                        gridColumns = 150.dp
                                        gridIcon = Icons.Default.ViewAgenda
                                    } else {
                                        gridColumns = 120.dp
                                        gridIcon = if (120.dp == 120.dp) Icons.Default.GridView else Icons.Default.ViewAgenda
                                    }
                                }) {
                                    Icon(
                                        imageVector = gridIcon,
                                        contentDescription = "Toggle Grid",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .shadow(4.dp, RoundedCornerShape(16.dp))
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Name", color = MaterialTheme.colorScheme.onSurface)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.SortByAlpha,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
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
                                        text = {
                                            Text("Date", color = MaterialTheme.colorScheme.onSurface)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
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
                                        text = {
                                            Text("Size", color = MaterialTheme.colorScheme.onSurface)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
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
                            }
                        )
                    }

                    currentRoute?.startsWith("permission_details") == true -> {
                        TopAppBar(
                            title = {
                                Text("Permission Details", color = MaterialTheme.colorScheme.onPrimary)
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        )
                    }

                    currentRoute == "overview" -> {
                        TopAppBar(
                             colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = backgroundColor,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            title = {
                                Text("Device Info", color = textColor)
                            },

                        )
                    }

                    currentRoute?.startsWith("UsageDetail") == true -> {
                        TopAppBar(
                            title = {
                                Text("${app?.appName} Insights", color = MaterialTheme.colorScheme.onPrimary)
                            },
                            actions = {
                                IconButton(onClick = {
                                    val intent =
                                        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = Uri.parse("package:${app?.packageName}")
                                    context.startActivity(intent)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "settings",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        )
                    }
                }
            },
            bottomBar = {
                if (shouldShowBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 6.dp,
                    ) {
                        bottomItems.forEach { item ->
                            NavigationBarItem(

                                selected = currentRoute == item.route.route,
                                onClick = {
                                    navController.navigate(item.route.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                label = {
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentRoute == item.route.route) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = item.icon),
                                        contentDescription = item.title,
                                        tint = if (currentRoute == item.route.route) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                    indicatorColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
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
                    .background(MaterialTheme.colorScheme.background),
            ) {
                composable("unused_apps_screen") {
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
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "selectedApp",
                                app
                            )
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "allApps",
                                allApps
                            )
                            navController.navigate("details")
                        }
                    )
                }


                composable("download_screen") {
                    DownloadScreen()
                }



                composable("file_scan") {
                    FileScanScreen()
                }


                // Overview
                composable(BottomNavItem.Overview.route.route) { OverviewScreen() }
                composable(BottomNavItem.Scanner.route.route) { ScannerScreen(navController) }
                composable(BottomNavItem.Settings.route.route) {SettingsScreen(navController)  }


            //    composable(BottomNavItem.MemoryManager.route.route) { MemoryManagerScreen() }

                // Permission Tab
                //  composable(BottomNavItem.Permission.route.route) { PermissionScreen() }

                // App Details

                composable("trash_clean") {

                   TrashScreen()
                }

                composable("privacy_security") {

                PrivacySecurityScreen(navController)
                }
                composable("app_security") {

                   AppSecurityScreen(navController)
                }






                composable("clean_whatsapp_media") {

                    CleanWhatsAppMediaScreen(navController)
                }

                composable("contact_cleaner_screen") {
                    ContactCleanerScreen()
                }

                composable("ignoredApps") {
                    IgnoredAppsScreen(navController)
                }

                composable("Appearance") {
                    Appearance(navController)
                }

                composable("UsageDetail") {
                    val app =
                        navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("selectedApp")
                    if (app != null) {
                        AppUsageDetailsScreen(
                            context = context,
                            app = app,
                            navController = navController
                        )
                    }
                }
                composable("details") {
                    val app =
                        navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("selectedApp")
                    val allApps =
                        navController.previousBackStackEntry?.savedStateHandle?.get<List<AppDetail>>(
                            "allApps"
                        ) ?: emptyList()

                    if (app != null) {
                        DetailsScreen(
                            app = app,
                            navController = navController,
                            onPermissionClick = { permission ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "allApps",
                                    allApps
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "currentApp",
                                    app
                                ) // ✅ pure AppDetail bhej
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
                    val allApps =
                        navController.previousBackStackEntry?.savedStateHandle?.get<List<AppDetail>>(
                            "allApps"
                        ) ?: emptyList()
                    val currentApp =
                        navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("currentApp")
                    val clickedApp =
                        navController.previousBackStackEntry?.savedStateHandle?.get<AppDetail>("selectedApp")

                    if (permission != null) {
                        PermissionDetailsScreen(
                            permission = permission,
                            allApps = allApps,
                            clickedApp = clickedApp,
                            currentApp = currentApp?.packageName,
                            onAppClick = { app ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "selectedApp",
                                    app
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "allApps",
                                    allApps
                                )
                                navController.navigate("details")
                            }
                        )
                    }
                }

            }


        }

    } else {
        // For file_scan screen, don't use Scaffold at all
        FileScanScreen()
    }
}

