package com.privacyshield.android.Component.Screen.Model

import android.graphics.drawable.Drawable

data class StorageUsage(
    val appName: String,
    val packageName: String,
    val appBytes: Long,
    val dataBytes: Long,
    val cacheBytes: Long,
    val totalBytes: Long
)
