package com.privacyshield.android.Component.Screen.Overview.Model

data class StorageInfo(
    val totalStorage: String,
    val usedStorage: String,
    val freeStorage: String,
    val systemStorage: String,
    val appStorage: String,
    val cacheStorage: String
)
