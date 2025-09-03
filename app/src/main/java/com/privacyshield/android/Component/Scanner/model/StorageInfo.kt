package com.privacyshield.android.Component.Scanner.model

data class StorageInfo(
    val freeBytes: Long,
    val usedBytes: Long,
    val totalBytes: Long,
    val freePercent: Int,
    val usedPercent: Int
)
