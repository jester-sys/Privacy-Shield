package com.privacyshield.android.Component.Screen.Deatils.utility

fun formatSourceDir(path: String, maxSegments: Int = 3): String {
    val segments = path.split("/")
    return if (segments.size <= maxSegments) {
        path
    } else {
        "…/" + segments.takeLast(maxSegments).joinToString("/")
    }
}
