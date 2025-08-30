package com.privacyshield.android.Component.Screen.Overview.Model

data class SensorInfo(
    val name: String,
    val type: Int,
    val vendor: String,
    val version: Int,
    val resolution: Float,
    val power: Float,
    val maxRange: Float,
    val minDelay: Int
)
