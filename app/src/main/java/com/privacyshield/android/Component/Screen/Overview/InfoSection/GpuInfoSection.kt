package com.privacyshield.android.Component.Screen.Overview.InfoSection


import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SmartToy

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privacyshield.android.Component.Screen.Overview.Component.InfoCard
import com.privacyshield.android.Component.Screen.Overview.Component.SectionHeader
import com.privacyshield.android.Component.Settings.theme.AppSettings
@Composable
fun GpuInfoSection(
    renderer: String,
    vendor: String,
    version: String,
    extensions: String,
    primaryColor: Color,
    textColor: Color,
    appSettings: AppSettings
) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Graphics Processor",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(top = 8.dp)
        )

        InfoCard("Renderer", renderer, primaryColor, Icons.Default.Memory, appSettings.highContrast, appSettings.darkTheme, textColor)
        InfoCard("Vendor", vendor, primaryColor, Icons.Default.DeveloperMode, appSettings.highContrast, appSettings.darkTheme, textColor)
        InfoCard("OpenGL Version", version, primaryColor, Icons.Default.Info, appSettings.highContrast, appSettings.darkTheme, textColor)
        InfoCard("Extensions", extensions.take(150) + "...", primaryColor, Icons.Default.List, appSettings.highContrast, appSettings.darkTheme, textColor)
    }
}
