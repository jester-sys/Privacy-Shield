package com.privacyshield.android.Component.Screen.Overview.Model

// ✅ Data Model
data class RamData(
    val totalRam: Long = 0,
    val availableRam: Long = 0,
    val usedRam: Long = 0,
    val isLowMemory: Boolean = false
)