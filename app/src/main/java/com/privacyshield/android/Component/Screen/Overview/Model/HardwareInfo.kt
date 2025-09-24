package com.privacyshield.android.Component.Screen.Overview.Model



data class HardwareInfo(
    val manufacturer: String,
    val model: String,
    val board: String,
    val hardware: String,
    val device: String,
    val product: String,
    val brand: String,
    val host: String,
    val buildId: String,
    val fingerprint: String,
    val serial: String,
    val supportedAbis: String,
    val cpuAbi: String,
    val cpuAbi2: String,
    val maxFreq: String,
    val minFreq: String,
    val currentFreq: String,
    val gpuInfo: String,
    val thermalInfo: String
)