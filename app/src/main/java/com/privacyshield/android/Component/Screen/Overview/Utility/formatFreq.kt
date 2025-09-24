package com.privacyshield.android.Component.Screen.Overview.Utility

fun formatFreq(freq: String): String {
    return if (freq == "N/A") "N/A"
    else try {
        val mhz = freq.toLong() / 1000
        if (mhz >= 1000) "${mhz / 1000.0} GHz" else "$mhz MHz"
    } catch (e: Exception) {
        "N/A"
    }
}