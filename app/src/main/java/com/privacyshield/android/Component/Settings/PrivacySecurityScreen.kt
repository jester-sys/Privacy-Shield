package com.privacyshield.android.Component.Settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.privacyshield.android.ui.theme.GreenPrimary
import com.privacyshield.android.ui.theme.HighRisk

@Composable
fun PrivacySecurityScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Privacy & Security",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = GreenPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        status?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    SettingItem("System & Updates", it.systemUpdates)
                    Divider()
                    SettingItem("Device Unlock (PIN/Pattern/Fingerprint)", it.deviceUnlock)
                    Divider()
                    SettingItem(
                        title = "App Security (Play Protect)",
                        enabled = it.playProtect,
                        onClick = { navController.navigate("app_security") } // ✅ navigate on click
                    )
                    Divider()
                    SettingItem("Camera Usage", it.cameraUsage)
                    Divider()
                    SettingItem("Mic Usage", it.micUsage)
                    Divider()
                    SettingItem("Location Usage", it.locationUsage)
                    Divider()
                    SettingItem("Approximate Usage Insights", it.usageInsights)
                }
            }
        }
    }
}

// ---------------------- ITEM COMPONENTS ----------------------
@Composable
fun SettingItem(
    title: String,
    enabled: Boolean,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        modifier = Modifier
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        headlineContent = { Text(text = title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = {
            Text(
                text = if (enabled) "Enabled" else "Disabled",
                color = if (enabled) GreenPrimary else HighRisk,
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (enabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = title,
                tint = if (enabled) GreenPrimary else HighRisk
            )
        }
    )
}
