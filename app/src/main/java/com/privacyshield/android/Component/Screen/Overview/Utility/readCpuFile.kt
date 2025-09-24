package com.privacyshield.android.Component.Screen.Overview.Utility

import java.io.File

fun readCpuFile(path: String): String {
    return try {
        val file = File(path)
        if (file.exists()) file.readText().trim() else "N/A"
    } catch (e: Exception) {
        "N/A"
    }
}