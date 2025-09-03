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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.privacyshield.android.Component.Screen.Overview.TabScreen.GpuTab
import com.privacyshield.android.Component.Screen.Overview.TabScreen.HardwareTab
import com.privacyshield.android.Component.Screen.Overview.TabScreen.OsTab
import com.privacyshield.android.Component.Screen.Overview.TabScreen.RamTab
import com.privacyshield.android.Component.Screen.Overview.TabScreen.SensorsTab
import com.privacyshield.android.Component.Screen.Overview.TabScreen.StorageTab
import kotlinx.coroutines.launch
import java.io.File
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverviewScreen() {
    val tabs = listOf("CPU", "GPU", "RAM", "Storage", "OS", "Hardware", "Sensors")


    val tabColors = listOf(
        Color(0xFF1976D2), // CPU -> Blue
        Color(0xFF388E3C), // GPU -> Green
        Color(0xFFF57C00), // RAM -> Orange
        Color(0xFF7B1FA2), // Storage -> Purple
        Color(0xFF00BCD4), // OS -> Teal
        Color(0xFFD32F2F), // Hardware -> Red
        Color(0xFF1DE9B6)  // Sensors -> Gray
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color(0xFF121212),
            contentColor = Color.White,
            edgePadding = 8.dp,
            // ✅ Custom indicator
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 3.dp,
                    color = tabColors[pagerState.currentPage]  // selected tab ka color
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            title,
                            color = if (pagerState.currentPage == index)
                                tabColors[index] // ✅ active tab -> apna color
                            else
                                Color.White.copy(0.7f) // inactive -> white
                        )
                    }
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












