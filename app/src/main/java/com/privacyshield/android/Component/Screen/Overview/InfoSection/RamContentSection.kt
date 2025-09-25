package com.privacyshield.android.Component.Screen.Overview.InfoSection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.privacyshield.android.Component.Screen.Overview.Component.AIAssistantCard
import com.privacyshield.android.Component.Screen.Overview.Component.InfoCard
import com.privacyshield.android.Component.Screen.Overview.Component.LoadingIndicator
import com.privacyshield.android.Component.Screen.Overview.Model.RamInfo
import com.privacyshield.android.Component.Screen.Overview.TabScreen.LowMemoryCard
import com.privacyshield.android.Component.Screen.Overview.Utility.ExplanationType
import com.privacyshield.android.Component.Screen.Overview.Utility.getDeviceExplanation
import com.privacyshield.android.Component.Screen.Overview.viewModel.RamViewModel
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.R
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import kotlinx.coroutines.launch

// Remove the nested LazyColumn from RamContentSection and make it a regular Column
@Composable
fun RamContentSection(
    totalRam: String,
    availableRam: String,
    usedRamPercent: Float,
    freeRam: String,
    cachedRam: String,
    swapUsed: String,
    swapTotal: String,
    isLowMemory: Boolean,
    textColor: Color,
    primaryColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "RAM Monitor",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )

        InfoCard(
            title = "Total RAM",
            value = totalRam,
            cardColor = Color(0xFF4CAF50),
            textColor = textColor
        )

        InfoCard(
            title = "Available RAM",
            value = availableRam,
            cardColor = Color(0xFF2196F3),
            textColor = textColor,

        )

        InfoCard(
            title = "Used RAM",
            value = "${usedRamPercent.toInt()}%",
            cardColor = if (usedRamPercent > 80) Color.Red else Color(0xFFFFC107),
            textColor = textColor
        )

        FreeCachedRamCard(
            freeRam,
            cachedRam,
            totalRam,
            textColor
        )

        SwapUsageCard(swapUsed, swapTotal, textColor)

        LowMemoryCard(isLowMemory)
    }
}

@Composable
fun FreeCachedRamCard(freeRam: String, cachedRam: String, totalRam: String, textColor: Color) {
    val total = totalRam.replace(" MB", "").toFloatOrNull() ?: 0f
    val free = freeRam.replace(" MB", "").toFloatOrNull() ?: 0f
    val cached = cachedRam.replace(" MB", "").toFloatOrNull() ?: 0f
    val progress = if (total > 0f) (free + cached) / total else 0f
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Free vs Cached RAM",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF26A69A), // Light Teal

            )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "Free RAM",
                    tint = Color(0xFF4DB6AC),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Free: $freeRam",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF4DB6AC)
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Cached RAM",
                    tint = Color(0xFFFF8A65),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Cached: $cachedRam",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFFFF8A65)
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    color = Color(0xFF009688), // Teal
                    trackColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        }
    }
}
@Composable
fun SwapUsageCard(swapUsed: String, swapTotal: String, textColor: Color) {
    val used = swapUsed.replace(" MB", "").toFloatOrNull() ?: 0f
    val total = swapTotal.replace(" MB", "").toFloatOrNull() ?: 0f
    val progress = if (total > 0f) used / total else 0f
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

                Text(
                    text = "Swap Usage",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF26A69A), // Light Teal

                    )
                )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Upload, // Used swap icon
                    contentDescription = "Used Swap",
                    tint = Color(0xFFFF8A65),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Used: $swapUsed",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF8A65)
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storage, // Total swap icon
                    contentDescription = "Total Swap",
                    tint = Color(0xFF4DB6AC),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Total: $swapTotal",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4DB6AC)
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    color = Color(0xFF009688),
                    trackColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        }
    }
}
