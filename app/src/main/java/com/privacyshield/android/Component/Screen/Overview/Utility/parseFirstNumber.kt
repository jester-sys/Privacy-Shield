package com.privacyshield.android.Component.Screen.Overview.Utility

import androidx.compose.ui.graphics.Color

// ---------- Small utils for UI layer ----------

fun parseFirstNumber(humanized: String): Float {
    // expects like "12.34 GB" -> converts to bytes in float
    val parts = humanized.split(" ")
    if (parts.isEmpty()) return 0f
    val num = parts[0].toFloatOrNull() ?: return 0f
    val unit = parts.getOrNull(1)?.uppercase() ?: return num
    val factor = when {
        unit.startsWith("TB") -> 1024f * 1024f * 1024f * 1024f
        unit.startsWith("GB") -> 1024f * 1024f * 1024f
        unit.startsWith("MB") -> 1024f * 1024f
        unit.startsWith("KB") -> 1024f
        else -> 1f
    }
    return num * factor
}

 fun usageColor(pct: Float): Color = when {
    pct < 50f -> Color(0xFF4CAF50) // Green
    pct < 80f -> Color(0xFFFFC107) // Amber
    else -> Color(0xFFF44336)      // Red
}