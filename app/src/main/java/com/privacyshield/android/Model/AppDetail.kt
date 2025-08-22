package com.privacyshield.android.Model

import android.graphics.drawable.Drawable

data class AppDetail(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val sourceDir: String,
    val isSystemApp: Boolean
)

