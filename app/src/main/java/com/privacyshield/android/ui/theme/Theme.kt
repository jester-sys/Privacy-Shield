package com.privacyshield.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDirection
import com.privacyshield.android.R


@Composable
fun PrivacyShieldTheme(
    themeColor: Color,
    isDynamicColor: Boolean,
    theme: Boolean,
    contrastMode: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ dynamic colors
        if (theme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        // Custom colors
        if (theme) {
            darkColorScheme(
                primary = themeColor,
                onPrimary = Color.White,
                primaryContainer = themeColor.copy(alpha = 0.2f),
                secondary = themeColor.copy(alpha = 0.8f),
                tertiary = themeColor.copy(alpha = 0.6f),
            )
        } else {
            lightColorScheme(
                primary = themeColor,
                onPrimary = Color.White,
                primaryContainer = themeColor.copy(alpha = 0.1f),
                secondary = themeColor.copy(alpha = 0.8f),
                tertiary = themeColor.copy(alpha = 0.6f),
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}