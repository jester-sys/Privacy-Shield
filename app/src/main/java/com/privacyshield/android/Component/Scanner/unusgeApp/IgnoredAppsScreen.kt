package com.privacyshield.android.Component.Scanner.unusgeApp

import android.annotation.SuppressLint
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IgnoredAppsScreen(
    navController: NavController
) {
    val context = LocalContext.current

    var ignoredApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load ignored apps
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val allIgnored = IgnoreManager.getIgnoredApps(context)
                .mapNotNull { pkg -> getAppInfo(context, pkg) }
            ignoredApps = allIgnored
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ignored Apps", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF1E1E1E)
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                ignoredApps.isEmpty() -> {
                    Text(
                        text = "No ignored apps",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ignoredApps, key = { it.packageName }) { app ->
                            val dismissState = rememberDismissState(
                                confirmStateChange = {
                                    if (it == DismissValue.DismissedToStart) {
                                        IgnoreManager.removeIgnoredApp(context, app.packageName)
                                        ignoredApps = ignoredApps.filterNot { ignoredApp ->
                                            ignoredApp.packageName == app.packageName
                                        }
                                    }
                                    true
                                }
                            )

                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.EndToStart),
                                background = {
                                    if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Red, RoundedCornerShape(16.dp))
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Unignore", color = Color.White, fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.width(8.dp))
                                                Icon(
                                                    Icons.Default.Undo,
                                                    contentDescription = "Unignore",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                },
                                dismissContent = {
                                    IgnoredAppCard(app = app)
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun IgnoredAppCard(app: AppInfo) {
    val context = LocalContext.current
    val bitmapState = remember(app.packageName) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(app.packageName) {
        bitmapState.value = createBitmapFromDrawable(context, app.icon)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon
            if (bitmapState.value != null) {
                Image(
                    bitmap = bitmapState.value!!.asImageBitmap(),
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(app.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(app.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(app.packageName, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
fun getAppInfo(context: Context, packageName: String): AppInfo? {
    return try {
        val pm = context.packageManager
        val app = pm.getApplicationInfo(packageName, 0)
        val name = pm.getApplicationLabel(app).toString()
        val icon = pm.getApplicationIcon(app)
        val installTime = pm.getPackageInfo(packageName, 0).firstInstallTime

        AppInfo(
            name = name,
            packageName = packageName,
            icon = icon,
            lastUsed = 0L, // agar usage stats nahi chaiye to 0 rakho
            installTime = installTime
        )
    } catch (e: Exception) {
        null
    }
}

@SuppressLint("ServiceCast")
fun getLastUsedTime(context: Context, packageName: String): Long {
    val usageStatsManager =
        context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - (1000L * 60 * 60 * 24 * 365) // last 1 year

    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY, startTime, endTime
    )

    return stats
        ?.find { it.packageName == packageName }
        ?.lastTimeUsed ?: 0L
}
