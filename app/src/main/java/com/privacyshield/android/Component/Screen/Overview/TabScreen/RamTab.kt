package com.privacyshield.android.Component.Screen.Overview.TabScreen

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
import androidx.compose.material.icons.filled.SmartToy
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
import com.privacyshield.android.Component.Screen.Overview.Component.AIAssistantCard
import com.privacyshield.android.Component.Screen.Overview.Component.InfoCard
import com.privacyshield.android.Component.Screen.Overview.Component.LoadingIndicator
import com.privacyshield.android.Component.Screen.Overview.Component.SectionHeader
import com.privacyshield.android.Component.Screen.Overview.InfoSection.FreeCachedRamCard
import com.privacyshield.android.Component.Screen.Overview.InfoSection.RamContentSection
import com.privacyshield.android.Component.Screen.Overview.InfoSection.SwapUsageCard
import com.privacyshield.android.Component.Screen.Overview.Model.CpuInfo
import com.privacyshield.android.Component.Screen.Overview.Model.RamInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.ExplanationType
import com.privacyshield.android.Component.Screen.Overview.Utility.getDeviceExplanation
import com.privacyshield.android.Component.Screen.Overview.viewModel.RamViewModel
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.R
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveSurfaceColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import compose.icons.TablerIcons
import compose.icons.tablericons.Activity
import compose.icons.tablericons.Database
import compose.icons.tablericons.DeviceDesktop
import kotlinx.coroutines.launch

@Composable
fun RamTab(viewModel: RamViewModel = hiltViewModel()) {

    val appSettings = LocalAppSettings.current
    val currentColorScheme = MaterialTheme.colorScheme

    val totalRam by viewModel.totalRam.collectAsState()
    val availableRam by viewModel.availableRam.collectAsState()
    val usedRamPercent by viewModel.usedRamPercent.collectAsState()
    val isLowMemory by viewModel.isLowMemory.collectAsState()
    val freeRam by viewModel.freeRam.collectAsState()
    val cachedRam by viewModel.cachedRam.collectAsState()
    val swapUsed by viewModel.swapUsed.collectAsState()
    val swapTotal by viewModel.swapTotal.collectAsState()
    val processMemory by viewModel.processMemory.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var showAssistantCard by remember { mutableStateOf(false) }
    // Theme colors
    val primaryColor = currentColorScheme.primary
    val backgroundColor = resolveBackgroundColor(appSettings, primaryColor)
    val textColor = resolveTextColor(appSettings, currentColorScheme)
    val surfaceColor = resolveSurfaceColor(appSettings, primaryColor)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        Column(
        ) {
            // 🔹 Loading Indicator
            LoadingIndicator(isLoading, showAssistantCard, appSettings, primaryColor)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showAssistantCard) {
                    item {
                        AIAssistantCard(
                            explanation = explanation,
                            question = question,
                            answer = answer,
                            isLoading = isLoading,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            appSettings = appSettings,
                            onAsk = { q ->
                                coroutineScope.launch {
                                    answer = getDeviceExplanation(
                                        explanationType = ExplanationType.RAM,
                                        userQuestion = q,
                                        ramInfo = RamInfo(
                                            totalRam = totalRam,
                                            availableRam = availableRam,
                                            freeRam = freeRam,
                                            cachedRam = cachedRam,
                                            swapUsed = swapUsed,
                                            swapTotal = swapTotal
                                        )
                                    )
                                    isLoading = true

                                    isLoading = false
                                    question = ""
                                }
                            },
                            onClear = {
                                question = ""
                                explanation = null
                                answer = null
                                showAssistantCard = false
                            },
                            onQuestionChange = { question = it }
                        )
                    }
                }
                item {
                    //   SectionHeader("Ram Overview", textColor)

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {



                SectionHeader("Device Memory", textColor)
//


                        InfoCard(
                            title = "Total RAM",
                            value = totalRam,
                            cardColor = Color(0xFF4CAF50),
                            textColor = textColor,
                            icon = TablerIcons.DeviceDesktop
                        )



                        InfoCard(
                            title = "Available RAM",
                            value = availableRam,
                            cardColor = Color(0xFF2196F3),
                            textColor = textColor,
                            icon = TablerIcons.Activity 
                        )



                        InfoCard(
                            title = "Used RAM",
                            value = "${usedRamPercent.toInt()}%",
                            cardColor = if (usedRamPercent > 80) Color.Red else Color(0xFFFFC107),
                            textColor = textColor,
                            icon = TablerIcons.Database
                        )



                        FreeCachedRamCard(freeRam, cachedRam, totalRam, textColor)



                        SwapUsageCard(swapUsed, swapTotal, textColor)



                        LowMemoryCard(isLowMemory)

                    }
                }
            }
        }


        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getDeviceExplanation(
                        explanationType = ExplanationType.RAM,
                        userQuestion = null,
                        ramInfo = RamInfo(
                            totalRam = totalRam,
                            availableRam = availableRam,
                            freeRam = freeRam,
                            cachedRam = cachedRam,
                            swapUsed = swapUsed,
                            swapTotal = swapTotal
                        )
                    )
                    isLoading = false
                    showAssistantCard = true
                }
            },
            containerColor = primaryColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.SmartToy, contentDescription = "AI Info")
        }

    }
}


@Composable
fun LowMemoryCard(isLowMemory: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isLowMemory)
                    Color(0xFFFF0000).copy(alpha = 0.2f) // 🔴 Red
                else
                    Color(0xFF4CAF50).copy(alpha = 0.2f), // 🟢 Green
                shape = RoundedCornerShape(16.dp)
            )

    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "System Low Memory",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold),
                color = if (isLowMemory) Color.Red else Color.Unspecified
            )
            Text(
                text = if (isLowMemory) "Yes" else "No",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


