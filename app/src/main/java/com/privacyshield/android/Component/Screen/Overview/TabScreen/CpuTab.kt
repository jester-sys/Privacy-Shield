package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.AssistantCard.AIAssistantCard
import com.privacyshield.android.Component.Screen.Overview.AssistantCard.CpuAssistantCard
import com.privacyshield.android.Component.Screen.Overview.Component.CpuCoreCard
import com.privacyshield.android.Component.Screen.Overview.Component.InfoCard
import com.privacyshield.android.Component.Screen.Overview.Indicator.GpuLoadingIndicator
import com.privacyshield.android.Component.Screen.Overview.InfoSection.getCpuCoresInfo
import com.privacyshield.android.Component.Screen.Overview.InfoSection.getCpuOverview
import com.privacyshield.android.Component.Screen.Overview.Model.CpuCoreInfo
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.R
import com.privacyshield.android.Utils.theme.resolveBackgroundColor
import com.privacyshield.android.Utils.theme.resolveTextColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CpuTab() {
    val appSettings = LocalAppSettings.current
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()

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
            GpuLoadingIndicator(isLoading, showAssistantCard, appSettings, primaryColor)
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
                                    answer = getCpuExplanation(q)
                                    isLoading = false
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
                        "CPU Cores",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor
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
                            icon = Icons.Default.Info,
                            highContrast = appSettings.highContrast,
                            darkTheme = appSettings.darkTheme,
                            textColor = textColor
                        )
                    }
                }
            }
        }
        // FloatingActionButton
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getCpuExplanation("Give me a CPU overview")
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


suspend fun getCpuExplanation(userQuestion: String? = null): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = com.privacyshield.android.BuildConfig.AI_KEY // ✅ Capital 'AI_KEY' use karo
    )

    // ✅ CPU basic info
    val cpuInfo = buildString {
        appendLine("CPU ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
        appendLine("CPU Cores: ${Runtime.getRuntime().availableProcessors()}")
        appendLine("CPU Hardware: ${Build.HARDWARE}")
        appendLine("CPU Manufacturer: ${Build.MANUFACTURER}")
        appendLine("CPU Model: ${Build.MODEL}")

        // ✅ Core-wise info (Linux proc file se)
        try {
            val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
            val reader = process.inputStream.bufferedReader()
            val lines = reader.readLines()
            reader.close()

            appendLine("\n--- Core Details (/proc/cpuinfo) ---")
            lines.take(20).forEach { line -> // Limit to first 20 lines
                if (line.contains("processor") || line.contains("Hardware") ||
                    line.contains("model name") || line.contains("cpu MHz")) {
                    appendLine(line.trim())
                }
            }
        } catch (e: Exception) {
            appendLine("\n(Core details not available: ${e.localizedMessage})")
        }
    }

    // ✅ Better prompt for AI
    val prompt = """
        You are a technical expert explaining CPU information to a non-technical user.
        
        CPU Information:
        $cpuInfo
        
        ${if (userQuestion.isNullOrBlank()) "Explain this CPU information in very simple, easy-to-understand terms." else "Question: $userQuestion\n\nAnswer in simple terms:"}
        
        Keep the response concise and user-friendly.
    """.trimIndent()

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "I couldn't generate an explanation at the moment. Please try again."
    } catch (e: Exception) {
        "Error getting explanation: ${e.localizedMessage}"
    }
}