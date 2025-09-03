package com.privacyshield.android.Component.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.privacyshield.android.R

sealed class BottomNavItem(val route: AppRoute, val title: String, val icon: Int) {

    data object Home : BottomNavItem(AppRoute.   Home, "Home", R.drawable.ic_app_icon)
    data object Scanner : BottomNavItem(AppRoute.Scanner, "Scanner", R.drawable.ic_broom_icon)
    data object MemoryManager : BottomNavItem(AppRoute.MemoryManager, "Memory Manger", R.drawable.ic_memory_icon)
    data object Overview : BottomNavItem(AppRoute.Overview, "Overview", R.drawable.ic_overview_icon)
}
