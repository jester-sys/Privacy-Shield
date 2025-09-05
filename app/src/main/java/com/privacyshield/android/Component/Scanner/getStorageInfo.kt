package com.privacyshield.android.Component.Scanner

import android.os.Environment
import android.os.StatFs
import com.privacyshield.android.Component.Scanner.model.StorageInfo

// Optional: get total storage info for device
fun getStorageInfo(): StorageInfo {
    val stat = StatFs(Environment.getExternalStorageDirectory().path)
    val total = stat.totalBytes
    val free = stat.availableBytes
    val used = total - free
    val freePercent = ((free * 100) / total).toInt()
    val usedPercent = ((used * 100) / total).toInt()
    return StorageInfo(free, used, total, freePercent, usedPercent)
}

fun formatSize(bytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes >= gb -> String.format("%.1f GB", bytes.toFloat() / gb)
        bytes >= mb -> String.format("%.1f MB", bytes.toFloat() / mb)
        bytes >= kb -> String.format("%.1f KB", bytes.toFloat() / kb)
        else -> "$bytes B"
    }
}
