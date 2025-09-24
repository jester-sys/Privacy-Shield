package com.privacyshield.android.Component.Screen.Overview.FloatingActionButton

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.privacyshield.android.Component.Settings.theme.AppSettings

@Composable
fun GpuFloatingActionButton(
    onClick: () -> Unit,
    primaryColor: Color,
    currentColorScheme: ColorScheme,
    appSettings: AppSettings,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = primaryColor,
        contentColor = if (appSettings.highContrast) {
            if (appSettings.darkTheme) Color.Black else Color.White
        } else {
            currentColorScheme.onPrimary
        },
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Default.SmartToy, contentDescription = "AI Info")
    }
}
