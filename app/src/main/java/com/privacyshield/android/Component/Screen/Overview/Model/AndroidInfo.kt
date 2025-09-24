package com.privacyshield.android.Component.Screen.Overview.Model

data class AndroidInfo(
    val androidVersion: String,
    val sdk: String,
    val securityPatch: String,
    val securityDays: String,
    val kernel: String,
    val bootloader: String,
    val fingerprint: String,
    val buildId: String,
    val selinux: String,
    val uptime: String,
    val timezone: String,
    val locale: String,
    val isRooted: Boolean,
    val hasGms: Boolean
)