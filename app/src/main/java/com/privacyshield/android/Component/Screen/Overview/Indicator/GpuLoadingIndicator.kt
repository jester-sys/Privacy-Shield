package com.privacyshield.android.Component.Screen.Overview.Indicator

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.privacyshield.android.Component.Settings.theme.AppSettings

@Composable
fun GpuLoadingIndicator(
    isLoading: Boolean,
    showAssistantCard: Boolean,
    appSettings: AppSettings,
    primaryColor: Color
) {
    if (isLoading && !showAssistantCard) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = primaryColor,
            trackColor = if (appSettings.highContrast) {
                if (appSettings.darkTheme) Color(0xFF333333) else Color(0xFFCCCCCC)
            } else {
                primaryColor.copy(alpha = 0.2f)
            }
        )
    }
 }
