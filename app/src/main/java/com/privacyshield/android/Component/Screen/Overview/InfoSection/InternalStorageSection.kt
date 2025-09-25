package com.privacyshield.android.Component.Screen.Overview.InfoSection

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Component.AlertTintBox
import com.privacyshield.android.Component.Screen.Overview.Component.SectionHeader
import com.privacyshield.android.Component.Screen.Overview.Component.TintedStatBox
import com.privacyshield.android.Component.Screen.Overview.InfoSection.InfoHintBox
import com.privacyshield.android.Component.Screen.Overview.Utility.parseFirstNumber
import com.privacyshield.android.Component.Screen.Overview.Utility.usageColor
import com.privacyshield.android.Component.Screen.Overview.viewModel.StorageViewModel
import com.privacyshield.android.R
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveSurfaceColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import kotlinx.coroutines.launch

@Composable
fun InternalStorageSection(
    total: String,
    available: String,
    usedPct: Float,
    textColor: Color = Color.Unspecified,
    primaryColor: Color = Color(0xFF4CAF50),
    surfaceColor: Color = Color(0xFFE0E0E0)
) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Internal Storage", textColor)

        TintedStatBox(
            title = "Total",
            value = total,
            titleColor = textColor,
            valueColor = primaryColor,
            backgroundColor = surfaceColor,
            icon = Icons.Default.Storage
        )

        TintedStatBox(
            title = "Available",
            value = available,
            titleColor = textColor,
            valueColor = primaryColor.copy(alpha = 0.8f),
            backgroundColor = surfaceColor,
            icon = Icons.Default.CheckCircle
        )

        TintedStatBox(
            title = "Used",
            value = "${usedPct.toInt()}%",
            titleColor = textColor,
            valueColor = usageColor(usedPct),
            backgroundColor = surfaceColor,
            icon = Icons.Default.Info
        )

        val totalBytes = parseFirstNumber(total)
        val availBytes = parseFirstNumber(available)
        val freePct = if (totalBytes > 0) (availBytes / totalBytes) * 100f else 0f
        val low = freePct < 10f
        AlertTintBox(
            "Low Storage (Internal)",
            if (low) "Yes — free ${"%.1f".format(freePct)}%" else "No — free ${"%.1f".format(freePct)}%",
            isAlert = low
        )
    }
}




@Composable
fun ExternalStorageSection(
    total: String,
    available: String,
    usedPct: Float,
    textColor: Color = Color.Black,
    primaryColor: Color = Color(0xFF4CAF50),
    surfaceColor: Color = Color(0xFFE0E0E0)
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("External Storage", textColor)

        TintedStatBox(
            title = "Total",
            value = total,
            titleColor = textColor,
            valueColor = primaryColor,
            backgroundColor = surfaceColor
        )

        TintedStatBox(
            title = "Available",
            value = available,
            titleColor = textColor,
            valueColor = primaryColor.copy(alpha = 0.8f),
            backgroundColor = surfaceColor
        )

        TintedStatBox(
            title = "Used",
            value = "${usedPct.toInt()}%",
            titleColor = textColor,
            valueColor = usageColor(usedPct),
            backgroundColor = surfaceColor
        )
    }
}
@Composable
fun AppStorageSection(
    appCode: String?,
    appData: String?,
    appCache: String?,
    appNote: String?,
    textColor: Color,
    primaryColor: Color,
    surfaceColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("App Storage (This App)", textColor)

        TintedStatBox(
            title = "Code (APK)",
            value = appCode ?: "N/A",
            titleColor = textColor,
            valueColor = primaryColor,
            backgroundColor = surfaceColor,
            icon = Icons.Default.Android // APK ke liye Android icon
        )

        TintedStatBox(
            title = "App Data",
            value = appData ?: "N/A",
            titleColor = textColor,
            valueColor = primaryColor.copy(alpha = 0.8f),
            backgroundColor = surfaceColor,
            icon = Icons.Default.Folder
        )

        TintedStatBox(
            title = "Cache",
            value = appCache ?: "N/A",
            titleColor = textColor,
            valueColor = primaryColor.copy(alpha = 0.6f),
            backgroundColor = surfaceColor,
            icon = Icons.Default.DeleteSweep
        )

        appNote?.let {
            InfoHintBox("Note: $it", primaryColor)
        }
    }
}
