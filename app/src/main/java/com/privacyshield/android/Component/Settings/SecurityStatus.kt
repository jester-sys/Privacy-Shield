package com.privacyshield.android.Component.Settings

data class SecurityStatus(
    val systemUpdates: Boolean,
    val deviceUnlock: Boolean,
    val playProtect: Boolean,
    val cameraUsage: Boolean,
    val micUsage: Boolean,
    val locationUsage: Boolean,
    val usageInsights: Boolean,
    val deviceFinderAvailable: Boolean
)
data class AppScanResult(
    val packageName: String,
    val appName: String,
    val installer: String?,
    val isSystemApp: Boolean,
    val isDebuggable: Boolean,
    val dangerousPermissions: List<String>,
    val apkSha256: String?,
    val riskScore: Int, // e.g., 0..100
    val riskReasons: List<String>
)

