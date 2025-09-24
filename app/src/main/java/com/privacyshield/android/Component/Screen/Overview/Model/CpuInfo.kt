package com.privacyshield.android.Component.Screen.Overview.Model

data class CpuInfo(
    val abi: String,
    val cores: Int,
    val hardware: String,
    val manufacturer: String,
    val model: String,
    val coreDetails: String = ""
)