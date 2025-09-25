package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Component.AIAssistantCard
import com.privacyshield.android.Component.Screen.Overview.Component.CpuCoreCard
import com.privacyshield.android.Component.Screen.Overview.Component.InfoCard
import com.privacyshield.android.Component.Screen.Overview.Component.LoadingIndicator
import com.privacyshield.android.Component.Screen.Overview.Component.SectionHeader
import com.privacyshield.android.Component.Screen.Overview.InfoSection.getCpuCoresInfo
import com.privacyshield.android.Component.Screen.Overview.InfoSection.getCpuOverview
import com.privacyshield.android.Component.Screen.Overview.Model.CpuCoreInfo
import com.privacyshield.android.Component.Screen.Overview.Model.CpuInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.ExplanationType
import com.privacyshield.android.Component.Screen.Overview.Utility.getDeviceExplanation
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Memory as Memory1

@Composable
fun CpuTab() {
    val appSettings = LocalAppSettings.current
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()

    val cpuIcons = mapOf(
        "CPU Model" to Icons.Default.Memory1,
        "CPU ABI" to Icons.Default.Code,
        "CPU Cores" to Icons.Default.ViewModule,
        "CPU Arch" to Icons.Default.DeveloperBoard,
        "Hardware" to Icons.Default.Build,
        "Model Name" to Icons.Default.Badge,
        "Processor" to Icons.Default.Computer,
        "Vendor ID" to Icons.Default.Apartment,
        "BogoMIPS" to Icons.Default.Timeline,
        "Features" to Icons.Default.List,
        "CPU Family" to Icons.Default.GroupWork,
        "Cache Size" to Icons.Default.Storage,
        "Governor" to Icons.Default.Tune,
        "CPU Temperature" to Icons.Default.Thermostat
    )




    var coresInfo by remember { mutableStateOf(emptyList<CpuCoreInfo>()) }
    var cpuOverview by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var showAssistantCard by remember { mutableStateOf(false) }

    val primaryColor = colorScheme.primary
    val backgroundColor = resolveBackgroundColor(appSettings, primaryColor)
    val textColor = resolveTextColor(appSettings, colorScheme)

    // Auto-refresh CPU cores
    LaunchedEffect(Unit) {
        cpuOverview = getCpuOverview()
        while (true) {
            coresInfo = getCpuCoresInfo()
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column {
            LoadingIndicator(isLoading, showAssistantCard, appSettings, primaryColor)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // AI Assistant Card
                if (showAssistantCard) {
                    item {
                        AIAssistantCard(
                            explanation = explanation,
                            question = question,
                            answer = answer,
                            isLoading = isLoading,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            appSettings = appSettings,
                            onAsk = { q ->
                                coroutineScope.launch {
                                    isLoading = true
                                    answer = getDeviceExplanation(
                                        explanationType = ExplanationType.CPU,
                                        userQuestion = q,
                                        cpuInfo = CpuInfo(
                                            abi = Build.SUPPORTED_ABIS.joinToString(),
                                            cores = Runtime.getRuntime().availableProcessors(),
                                            hardware = Build.HARDWARE,
                                            manufacturer = Build.MANUFACTURER,
                                            model = Build.MODEL,
                                            coreDetails = coresInfo.joinToString(" | ") { "Core ${it.coreId}: ${it.curPercent}%" }
                                        )
                                    )
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

                // CPU Cores Section
                item {
                    Text(
                        "Processing Power",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                }
                items(coresInfo) { core ->
                    CpuCoreCard(core, appSettings, primaryColor, textColor)
                }

                // CPU Overview Section
                item {
                    Text(
                        "CPU Overview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor
                    )
                }
                cpuOverview.forEach { (title, value) ->
                    item {
                        InfoCard(
                            title = title,
                            value = value,
                            cardColor = primaryColor,
                            icon = cpuIcons[title] ?: Icons.Default.Info,
                            highContrast = appSettings.highContrast,
                            darkTheme = appSettings.darkTheme,
                            textColor = textColor
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getDeviceExplanation(
                        explanationType = ExplanationType.CPU,
                        userQuestion = null,
                        cpuInfo = CpuInfo(
                            abi = Build.SUPPORTED_ABIS.joinToString(),
                            cores = Runtime.getRuntime().availableProcessors(),
                            hardware = Build.HARDWARE,
                            manufacturer = Build.MANUFACTURER,
                            model = Build.MODEL,
                            coreDetails = coresInfo.joinToString(" | ") { "Core ${it.coreId}: ${it.curPercent}%" }
                        )
                    )
                    isLoading = false
                    showAssistantCard = true
                }
            },
            containerColor = primaryColor,
            contentColor = textColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.SmartToy, contentDescription = "AI Info")
        }
    }

}


