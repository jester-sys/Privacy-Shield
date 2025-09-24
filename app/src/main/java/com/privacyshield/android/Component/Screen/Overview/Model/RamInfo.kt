package com.privacyshield.android.Component.Screen.Overview.Model

data class RamInfo(
    val totalRam: String,
    val availableRam: String,
    val freeRam: String,
    val cachedRam: String,
    val swapUsed: String,
    val swapTotal: String
)