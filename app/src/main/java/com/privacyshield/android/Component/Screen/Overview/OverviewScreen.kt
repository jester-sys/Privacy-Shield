package com.privacyshield.android.Component.Screen.Overview

import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.GLES20
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.privacyshield.android.Component.Screen.Overview.TabScreen.CpuTab
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen() {
    val tabs = listOf("CPU", "GPU", "RAM", "Storage", "OS", "Hardware", "Sensors")

    // Colors for each tab
    val tabColors = listOf(
        Color(0xFF1976D2), // CPU -> Blue
        Color(0xFF388E3C), // GPU -> Green
        Color(0xFFF57C00), // RAM -> Orange
        Color(0xFF7B1FA2), // Storage -> Purple
        Color(0xFF00796B), // OS -> Teal
        Color(0xFFD32F2F), // Hardware -> Red
        Color(0xFF455A64)  // Sensors -> Gray
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    // Current tab color
    val currentColor = tabColors[pagerState.currentPage]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Info") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // ✅ ScrollableTabRow for scrollable tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = currentColor,
                contentColor = Color.White,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title, color = if (pagerState.currentPage == index) Color.White else Color.White.copy(0.7f)) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> CpuTab()
                    1 -> GpuTab()
                    2 -> RamTab()
                    3 -> StorageTab()
                    4 -> OsTab()
                    5 -> HardwareTab()
                    6 -> SensorsTab()
                }
            }
        }
    }
}



@Composable
fun GpuTab() {
    val glRenderer = GLES20.glGetString(GLES20.GL_RENDERER)
    val glVendor = GLES20.glGetString(GLES20.GL_VENDOR)
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("GPU Info", style = MaterialTheme.typography.titleMedium) }
        item { Text("Renderer: $glRenderer") }
        item { Text("Vendor: $glVendor") }
    }
}

@Composable
fun RamTab() {
    val activityManager = LocalContext.current.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo().apply { activityManager.getMemoryInfo(this) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("RAM Info", style = MaterialTheme.typography.titleMedium) }
        item { Text("Total: ${memInfo.totalMem / 1024 / 1024} MB") }
        item { Text("Available: ${memInfo.availMem / 1024 / 1024} MB") }
    }
}

@Composable
fun StorageTab() {
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.blockSizeLong * stat.blockCountLong
    val avail = stat.blockSizeLong * stat.availableBlocksLong
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Storage Info", style = MaterialTheme.typography.titleMedium) }
        item { Text("Total: ${total / 1024 / 1024} MB") }
        item { Text("Available: ${avail / 1024 / 1024} MB") }
    }
}

@Composable
fun OsTab() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("OS Info", style = MaterialTheme.typography.titleMedium) }
        item { Text("Android Version: ${Build.VERSION.RELEASE}") }
        item { Text("SDK: ${Build.VERSION.SDK_INT}") }
        item { Text("Device: ${Build.MODEL}") }
        item { Text("Manufacturer: ${Build.MANUFACTURER}") }
    }
}

@Composable
fun HardwareTab() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Hardware Info", style = MaterialTheme.typography.titleMedium) }
        item { Text("Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}") }
        item { Text("Board: ${Build.BOARD}") }
        item { Text("Hardware: ${Build.HARDWARE}") }
    }
}

@Composable
fun SensorsTab() {
    val sensorManager = LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Sensors", style = MaterialTheme.typography.titleMedium) }
        items(sensors) { sensor ->
            Text("${sensor.name} (${sensor.type})")
        }
    }
}
