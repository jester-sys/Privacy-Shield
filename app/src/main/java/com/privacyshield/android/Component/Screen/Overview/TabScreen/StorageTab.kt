package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Component.AIAssistantCard
import com.privacyshield.android.Component.Screen.Overview.Component.LoadingIndicator
import com.privacyshield.android.Component.Screen.Overview.InfoSection.AppStorageSection
import com.privacyshield.android.Component.Screen.Overview.InfoSection.InternalStorageSection
import com.privacyshield.android.Component.Screen.Overview.Model.StorageInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.ExplanationType
import com.privacyshield.android.Component.Screen.Overview.Utility.getDeviceExplanation
import com.privacyshield.android.Component.Screen.Overview.viewModel.StorageViewModel
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.R
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveSurfaceColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import kotlinx.coroutines.launch

@SuppressLint("SuspiciousIndentation")
@Composable
fun StorageTab(viewModel: StorageViewModel = hiltViewModel()) {
    val appSettings = LocalAppSettings.current
    val currentColorScheme = MaterialTheme.colorScheme

    // Collect states from ViewModel
    val internalTotal by viewModel.internalTotal.collectAsState()
    val internalAvail by viewModel.internalAvail.collectAsState()
    val internalUsedPct by viewModel.internalUsedPct.collectAsState()

    val externalTotal by viewModel.externalTotal.collectAsState()
    val externalAvail by viewModel.externalAvail.collectAsState()
    val externalUsedPct by viewModel.externalUsedPct.collectAsState()

    val appCode by viewModel.appCode.collectAsState()
    val appData by viewModel.appData.collectAsState()
    val appCache by viewModel.appCache.collectAsState()
    val appNote by viewModel.appStatsNote.collectAsState()

    // Theme colors (same as GPU)
    val primaryColor = currentColorScheme.primary
    val backgroundColor = resolveBackgroundColor(appSettings, primaryColor)
    val textColor = resolveTextColor(appSettings, currentColorScheme)
    val surfaceColor = resolveSurfaceColor(appSettings, primaryColor)

    // UI states
    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var showAssistantCard by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        Column {
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
                                isLoading = true
                                answer = getDeviceExplanation(
                                    explanationType = ExplanationType.STORAGE,
                                    userQuestion = q,
                                    storageInfo = StorageInfo(
                                        totalStorage = internalTotal,
                                        usedStorage = "${internalUsedPct}%",
                                        freeStorage = internalAvail,
                                        systemStorage = externalTotal ?: "N/A",
                                        appStorage = appData ?: "N/A",
                                        cacheStorage = appCache ?: "N/A"
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

            // Internal Storage
            item {
                InternalStorageSection(
                    total = internalTotal,
                    available = internalAvail,
                    usedPct = internalUsedPct,
                    textColor = textColor,
                    primaryColor = primaryColor,
                    surfaceColor = surfaceColor
                )
            }

//            // External Storage (conditional)
//            if (!externalTotal.isNullOrEmpty() && !externalAvail.isNullOrEmpty() && externalUsedPct != null) {
//                item {
//                    ExternalStorageSection(
//                        total = externalTotal!!,
//                        available = externalAvail!!,
//                        usedPct = externalUsedPct!!,
//                        textColor = textColor,
//                        primaryColor = primaryColor,
//                        surfaceColor = surfaceColor
//                    )
//                }
//            }

            // App Storage
            item {
                AppStorageSection(
                    appCode = appCode,
                    appData = appData,
                    appCache = appCache,
                    appNote = appNote,
                    textColor = textColor,
                    primaryColor = primaryColor,
                    surfaceColor = surfaceColor
                )
            }

            // Bottom spacing to avoid FAB overlap
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        }

        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getDeviceExplanation(
                        explanationType = ExplanationType.STORAGE,
                        storageInfo = StorageInfo(
                            totalStorage = internalTotal,
                            usedStorage = "${internalUsedPct}%",
                            freeStorage = internalAvail,
                            systemStorage = externalTotal ?: "N/A",
                            appStorage = appData ?: "N/A",
                            cacheStorage = appCache ?: "N/A"
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








