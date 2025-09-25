package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.opengl.GLSurfaceView
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Component.AIAssistantCard
import com.privacyshield.android.Component.Screen.Overview.Component.LoadingIndicator
import com.privacyshield.android.Component.Screen.Overview.InfoSection.GpuInfoSection
import com.privacyshield.android.Component.Screen.Overview.Model.CpuInfo
import com.privacyshield.android.Component.Screen.Overview.Model.GpuInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.ExplanationType
import com.privacyshield.android.Component.Screen.Overview.Utility.getDeviceExplanation
import com.privacyshield.android.Component.Screen.Overview.viewModel.GpuInfoRenderer
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveSurfaceColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpuTab() {
    val appSettings = LocalAppSettings.current
    val currentColorScheme = MaterialTheme.colorScheme

    var renderer by remember { mutableStateOf("Loading...") }
    var vendor by remember { mutableStateOf("Loading...") }
    var version by remember { mutableStateOf("Loading...") }
    var extensions by remember { mutableStateOf("Loading...") }

    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showAssistantCard by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Theme colors
    val primaryColor = currentColorScheme.primary
    val backgroundColor = resolveBackgroundColor(appSettings, primaryColor)
    val textColor = resolveTextColor(appSettings, currentColorScheme)
    val surfaceColor = resolveSurfaceColor(appSettings, primaryColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column {
            // 🔹 Loading Indicator
            LoadingIndicator(isLoading, showAssistantCard, appSettings, primaryColor)

            // GPU Info Collector
            AndroidView(
                factory = { context ->
                    GLSurfaceView(context).apply {
                        setEGLContextClientVersion(2)
                        setRenderer(GpuInfoRenderer { r, v, ver, ext ->
                            renderer = r
                            vendor = v
                            version = ver
                            extensions = ext
                        })
                        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                    }
                },
                modifier = Modifier.size(1.dp)
            )

            // GPU + Assistant UI
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
                                         explanationType = ExplanationType.GPU,
                                         userQuestion = q,
                                        gpuInfo = GpuInfo(
                                            renderer = renderer,
                                            vendor = vendor,
                                            version = version,
                                            extensions = extensions
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

                // GPU Info Section
                item {
                    GpuInfoSection(
                        renderer = renderer,
                        vendor = vendor,
                        version = version,
                        extensions = extensions,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        appSettings = appSettings
                    )
                }
            }
        }
        // FloatingActionButton
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true

                    explanation = getDeviceExplanation(
                        explanationType = ExplanationType.GPU,
                        userQuestion = null, // General explanation ke liye
                        gpuInfo = GpuInfo(
                            renderer = renderer,
                            vendor = vendor,
                            version = version,
                            extensions = extensions
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



suspend fun getGpuExplanation(
    renderer: String,
    vendor: String,
    version: String,
    extensions: String,
    userQuestion: String? = null
): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA"
    )

    // GPU info ko ek string me combine karte hain
    val gpuInfo = """
        Renderer: $renderer
        Vendor: $vendor
        OpenGL Version: $version
        Extensions: $extensions
    """.trimIndent()

    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this GPU in simple language:\n$gpuInfo"
    } else {
        "Here is the GPU info:\n$gpuInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}

