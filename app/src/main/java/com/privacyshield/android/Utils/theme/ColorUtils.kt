package com.privacyshield.android.Utils.theme


import androidx.compose.ui.graphics.Color
import com.privacyshield.android.Component.Settings.theme.AppSettings

/**
 * Resolve background color based on user settings and primary color.
 */
fun resolveBackgroundColor(appSettings: AppSettings, primaryColor: Color): Color {
    return if (appSettings.highContrast) {
        if (appSettings.darkTheme) Color.Black else Color.White
    } else {
        if (appSettings.darkTheme) primaryColor.copy(alpha = 0.05f)
        else primaryColor.copy(alpha = 0.03f)
    }
}

/**
 * Resolve text color based on user settings and theme.
 */
fun resolveTextColor(appSettings: AppSettings, colorScheme: androidx.compose.material3.ColorScheme): Color {
    return if (appSettings.highContrast) {
        if (appSettings.darkTheme) Color.White else Color.Black
    } else {
        colorScheme.onSurface
    }
}

/**
 * Resolve card / surface color based on user settings and primary color.
 */
fun resolveSurfaceColor(appSettings: AppSettings, primaryColor: Color): Color {
    return if (appSettings.highContrast) {
        if (appSettings.darkTheme) Color(0xFF111111) else Color(0xFFEEEEEE)
    } else {
        if (appSettings.darkTheme) primaryColor.copy(alpha = 0.08f)
        else primaryColor.copy(alpha = 0.05f)
    }
}
