package com.privacyshield.android.Component.Scanner.unusgeApp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.navigation.NavController
import com.privacyshield.android.Component.Screen.UsageStatsScreen.model.hasUsageStatsPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import java.util.concurrent.TimeUnit
@OptIn(ExperimentalMaterial3Api::class)


@Composable
fun UnusedAppsScreen(navController: NavController) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf(30) }
    var isLoading by remember { mutableStateOf(false) }       // 🔹 General loading
    var isRefreshing by remember { mutableStateOf(false) }    // 🔹 User swipe refresh
    var searchQuery by remember { mutableStateOf("") }

    val filterOptions = listOf(7, 15, 30, 60, 90, 180)
    val ignoredApps = remember { mutableStateListOf<String>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // load ignored apps once
    LaunchedEffect(Unit) {
        ignoredApps.clear()
        ignoredApps.addAll(IgnoreManager.getIgnoredApps(context))
    }

    // reload when filter changes
    LaunchedEffect(selectedFilter) {
        isLoading = true
        loadApps(context, selectedFilter, ignoredApps) {
            apps = it
            isLoading = false
        }
    }

    val refreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            UnusedAppsHeader(appCount = apps.size)
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                placeholder = { Text("Search apps", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBB86FC),
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Text("Show apps unused for:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow {
                items(filterOptions) { days ->
                    FilterChip(
                        days = days,
                        isSelected = selectedFilter == days,
                        onSelected = { selectedFilter = days }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SwipeRefresh (sirf user refresh ke liye)
            SwipeRefresh(
                state = refreshState,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        loadApps(context, selectedFilter, ignoredApps) {
                            apps = it
                            isRefreshing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (apps.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (hasUsageStatsPermission(context))
                                "No unused apps found"
                            else
                                "Please enable Usage Access permission",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        if (!hasUsageStatsPermission(context)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                            ) {
                                Text("Enable Permission")
                            }
                        }
                    }
                } else {
                    val filteredApps = if (searchQuery.isBlank()) {
                        apps
                    } else {
                        apps.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }

                    LazyColumn {
                        items(filteredApps, key = { it.packageName }) { app ->
                            UnusedAppItem(
                                app = app,
                                filterDays = selectedFilter,
                                ignoredApps = ignoredApps,
                                onDeleteClick = { appToDelete ->
                                    uninstallApp(context, appToDelete)
                                    ignoredApps.add(appToDelete.packageName)
                                    apps = apps.filterNot { it.packageName == appToDelete.packageName }
                                },
                                onAppInfoClick = { appInfo ->
                                    openAppSettings(context, appInfo.packageName)
                                },
                                onIgnoreClick = { appToIgnore ->
                                    IgnoreManager.addIgnoredApp(context, appToIgnore.packageName)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Ignored ${appToIgnore.name}")
                                    }
                                },
                                onRemoveFromList = { pkg ->
                                    apps = apps.filterNot { it.packageName == pkg }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { navController.navigate("ignoredApps") },
            contentColor = Color.Transparent,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(70.dp)
                .shadow(12.dp, CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFBB86FC), Color(0xFF6200EE))
                    ),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "Ignored Apps",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // 🔽 Loading Overlay CircularProgressIndicator (sirf background loads ke liye)
        if (isLoading && !isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)

            }
        }
    }
}

// helper to load apps
suspend fun loadApps(
    context: Context,
    days: Int,
    ignoredApps: List<String>,
    onLoaded: (List<AppInfo>) -> Unit
) {
    withContext(Dispatchers.IO) {
        val allApps = getUnusedApps(context, days, ignoredApps.toSet())
        withContext(Dispatchers.Main) {
            onLoaded(allApps)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UnusedAppItem(
    app: AppInfo,
    filterDays: Int,
    ignoredApps: MutableList<String>,
    onDeleteClick: (AppInfo) -> Unit = {},
    onAppInfoClick: (AppInfo) -> Unit = {},
    onIgnoreClick: (AppInfo) -> Unit = {},
    onShareClick: (AppInfo) -> Unit = {},
    onRemoveFromList: (String) -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val daysUnused = calculateDaysUnused(app.lastUsed)
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val bitmapState = remember(app.packageName) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(app.packageName) {
        bitmapState.value = createBitmapFromDrawable(context, app.icon)
    }

    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                if (!ignoredApps.contains(app.packageName)) {
                    ignoredApps.add(app.packageName)
                    onIgnoreClick(app)
                    onRemoveFromList(app.packageName)
                }
                true
            } else false
        }
    )

    // Reset immediately to remove yellow background
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == DismissValue.DismissedToStart) {
            dismissState.snapTo(DismissValue.Default)
        }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        dismissThresholds = { FractionalThreshold(0.5f) },
        background = {
            if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFA000), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ignore", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Block, contentDescription = "Ignore", tint = Color.White)
                    }
                }
            }
        },
        dismissContent = {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppInfoClick(app) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // App icon
                    if (bitmapState.value != null) {
                        Image(
                            bitmap = bitmapState.value!!.asImageBitmap(),
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                app.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            app.name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Last used: ${if (app.lastUsed > 0) sdf.format(Date(app.lastUsed)) else "Never"}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        daysUnused > 90 -> Color(0xFFB71C1C) // red
                                        daysUnused > 30 -> Color(0xFFFFA000) // amber
                                        else -> Color(0xFF2E7D32) // green
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Unused for $daysUnused days",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // More button
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF2A2A2A))
                        ) {
                            DropdownMenuItem(
                                text = { Text("App Info", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onAppInfoClick(app)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share", color = Color(0xFF03DAC5)) },
                                onClick = {
                                    showMenu = false
                                    onShareClick(app)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(app)
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun FilterChip(days: Int, isSelected: Boolean, onSelected: () -> Unit) {
    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(listOf(Color(0xFFBB86FC), Color(0xFF6200EE)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF2A2A2A), Color(0xFF1E1E1E)))
    }

    val textColor = if (isSelected) Color.White else Color(0xFFE0E0E0)

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(4.dp)
            .clickable { onSelected() }
            .shadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            ),
        color = Color.Transparent // Gradient will be drawn manually
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (days) {
                    7 -> "1 Week"
                    15 -> "2 Weeks"
                    30 -> "1 Month"
                    60 -> "2 Months"
                    90 -> "3 Months"
                    180 -> "6 Months"
                    else -> "$days Days"
                },
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// Helper function to calculate days unused
fun calculateDaysUnused(lastUsed: Long): Int {
    if (lastUsed == 0L) return 0
    val diff = System.currentTimeMillis() - lastUsed
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}

fun openAppSettings(context: Context, packageName: String?) {
    if (packageName == null) return
    try {
        // Try to open App Permissions directly (Android 10+)
        val intent = Intent("android.intent.action.MANAGE_APP_PERMISSIONS").apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to App Info page
        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
    }
}


// Share function (optional)
private fun shareAppInfo(context: Context, app: AppInfo) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "App Info: ${app.name}")
        putExtra(Intent.EXTRA_TEXT, "Check out this app: ${app.name}\nPackage: ${app.packageName}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share App Info"))
}

fun uninstallApp(context: Context, app: AppInfo) {
    try {
        val packageUri = Uri.parse("package:${app.packageName}")
        val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
        context.startActivity(uninstallIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Cannot uninstall ${app.name}",
            Toast.LENGTH_SHORT
        ).show()
    }
}



fun createBitmapFromDrawable(context: Context, drawable: Drawable): Bitmap? {
    return try {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } catch (e: Exception) {
        Log.e("BitmapError", "Error creating bitmap from drawable", e)
        null
    }
}

