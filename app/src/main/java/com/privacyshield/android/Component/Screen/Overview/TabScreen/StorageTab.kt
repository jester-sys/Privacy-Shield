package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.annotation.SuppressLint
import android.os.Environment
import android.os.StatFs
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
import androidx.compose.material3.TextButton
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
import com.privacyshield.android.Component.Screen.Overview.Utility.StorageViewModel
import com.privacyshield.android.R
import kotlinx.coroutines.launch

@SuppressLint("SuspiciousIndentation")
@Composable
fun StorageTab(viewModel: StorageViewModel = hiltViewModel()) {
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

    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var showAssistantCard by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E)) // Dark background
        ) {
            Column {

                if (isLoading && !showAssistantCard) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color(0xFF7B1FA2),
                        trackColor = Color.DarkGray
                    )
                }


                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // 🔹 AI Storage Assistant Card
                    if (showAssistantCard) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFF7B1FA2).copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                    Text(
                                        "AI  Storage Assistant",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF7B1FA2)
                                    )



                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(4.dp)
                                        )
                                    }

                                    // 🔹 Show AI explanation
                                    explanation?.let {
                                        Text(
                                            it,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }

                                    // 🔹 Question + Ask AI
                                    if (explanation != null) {
                                        OutlinedTextField(
                                            value = question,
                                            onValueChange = { question = it },
                                            label = { Text("Ask about Storage", color = Color.White) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = Color.White,
                                                unfocusedBorderColor = Color(0xFFEEEEEE),
                                                cursorColor = Color.White
                                            )
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        if (question.isNotBlank()) {
                                                            isLoading = true
                                                            answer = getStorageExplanation(
                                                                internalTotal,
                                                                internalAvail,
                                                                internalUsedPct.toString(),
                                                                externalTotal.toString(),
                                                                externalAvail.toString(),
                                                                externalUsedPct.toString(),


                                                            )
                                                            isLoading = false
                                                            question = ""
                                                        }
                                                    }
                                                },
                                                enabled = question.isNotBlank(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                            ) {
                                                Text("Ask AI", color = Color(0xFF7B1FA2))
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    question = ""
                                                    explanation = null
                                                    answer = null
                                                    showAssistantCard = false
                                                },
                                                border = BorderStroke(1.dp, Color.White)
                                            ) {
                                                Text("Clear Chat", color = Color.White)
                                            }
                                        }

                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(top = 4.dp)
                                            )
                                        }

                                        answer?.let {
                                            Text("Answer: $it", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                    }


                    // ---------- Internal Storage ----------
                    item {
                        SectionHeader("Internal Storage", Color(0xFFE0E0E0))
                    }

                    // Total
                    item {
                        TintedStatBox(
                            title = "Total",
                            value = internalTotal,
                            progress = 1f,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    // Available
                    item {
                        val totalBytes = parseFirstNumber(internalTotal)
                        val availBytes = parseFirstNumber(internalAvail)
                        val progress = if (totalBytes > 0f) (availBytes / totalBytes) else 0f

                        TintedStatBox(
                            title = "Available",
                            value = internalAvail,
                            progress = progress,
                            color = Color(0xFF2196F3)
                        )
                    }

                    // Used %
                    item {
                        val usedColor = usageColor(internalUsedPct)
                        TintedStatBox(
                            title = "Used",
                            value = "${internalUsedPct.toInt()}%",
                            progress = (internalUsedPct / 100f).coerceIn(0f, 1f),
                            color = usedColor
                        )
                    }

                    // Low Storage Alert (free < 10%)
                    item {
                        val total = parseFirstNumber(internalTotal)
                        val avail = parseFirstNumber(internalAvail)
                        val freePct = if (total > 0) (avail / total) * 100f else 0f
                        val low = freePct < 10f

                        AlertTintBox(
                            title = "Low Storage (Internal)",
                            value = if (low) "Yes — free ${"%.1f".format(freePct)}%" else "No — free ${
                                "%.1f".format(
                                    freePct
                                )
                            }%",
                            isAlert = low
                        )
                    }

                    // ---------- External Storage (if present) ----------
                    if (externalTotal != null && externalAvail != null && externalUsedPct != null) {
                        item { SectionHeader("External Storage", Color(0xFFE0E0E0)) }

                        item {
                            TintedStatBox(
                                title = "Total",
                                value = externalTotal!!,
                                progress = 1f,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        item {
                            val total = parseFirstNumber(externalTotal!!)
                            val avail = parseFirstNumber(externalAvail!!)
                            val progress = if (total > 0f) (avail / total) else 0f
                            TintedStatBox(
                                title = "Available",
                                value = externalAvail!!,
                                progress = progress,
                                color = Color(0xFF2196F3)
                            )
                        }
                        item {
                            val usedPct = externalUsedPct!!.coerceIn(0f, 100f)
                            TintedStatBox(
                                title = "Used",
                                value = "${usedPct.toInt()}%",
                                progress = usedPct / 100f,
                                color = usageColor(usedPct)
                            )
                        }
                    }

                    // ---------- App Storage (this app) ----------
                    item { SectionHeader("App Storage (This App)", Color(0xFFE0E0E0)) }

                    item {
                        TintedStatBox(
                            title = "Code (APK)",
                            value = appCode ?: "N/A",
                            progress = 1f,
                            color = Color(0xFF9C27B0)
                        )
                    }
                    item {
                        TintedStatBox(
                            title = "App Data",
                            value = appData ?: "N/A",
                            progress = 1f,
                            color = Color(0xFF7E57C2)
                        )
                    }
                    item {
                        TintedStatBox(
                            title = "Cache",
                            value = appCache ?: "N/A",
                            progress = 1f,
                            color = Color(0xFFBA68C8)
                        )
                    }
                    if (appNote != null) {
                        item {
                            InfoHintBox(
                                text = "Note: ${appNote}",
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                            isLoading = true
                            explanation = getStorageExplanation(
                                internalTotal,
                                internalAvail,
                                externalTotal.toString(),
                                internalUsedPct.toString(),
                                externalAvail.toString(),
                                externalUsedPct.toString(),



                            )
                            isLoading = false
                        showAssistantCard = true
                        }

                },
                containerColor = Color(0xFF7B1FA2),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ai_icon),
                    contentDescription = "AI Info"
                )
            }
        }
    }


// ---------- Reusable UI Pieces ----------

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = color
    )
}

@Composable
fun TintedStatBox(
    title: String,
    value: String,
    progress: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A2A2A))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold

                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = color
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(alpha = 0.18f))
            ) {
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    color = color,
                    trackColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
fun AlertTintBox(title: String, value: String, isAlert: Boolean) {
    val tint = if (isAlert) Color(0xFFFF0000) else Color(0xFF4CAF50)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.18f))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = tint
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun InfoHintBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Text(text, color = Color.White)
    }
}

// ---------- Small utils for UI layer ----------

private fun parseFirstNumber(humanized: String): Float {
    // expects like "12.34 GB" -> converts to bytes in float
    val parts = humanized.split(" ")
    if (parts.isEmpty()) return 0f
    val num = parts[0].toFloatOrNull() ?: return 0f
    val unit = parts.getOrNull(1)?.uppercase() ?: return num
    val factor = when {
        unit.startsWith("TB") -> 1024f * 1024f * 1024f * 1024f
        unit.startsWith("GB") -> 1024f * 1024f * 1024f
        unit.startsWith("MB") -> 1024f * 1024f
        unit.startsWith("KB") -> 1024f
        else -> 1f
    }
    return num * factor
}

private fun usageColor(pct: Float): Color = when {
    pct < 50f -> Color(0xFF4CAF50) // Green
    pct < 80f -> Color(0xFFFFC107) // Amber
    else -> Color(0xFFF44336)      // Red
}

suspend fun getStorageExplanation(
    totalStorage: String,
    usedStorage: String,
    freeStorage: String,
    systemStorage: String,
    appStorage: String,
    cacheStorage: String,
    userQuestion: String? = null,
): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA" // apna API key daalna
    )

    // ✅ Build Storage info string
    val storageInfo = buildString {
        appendLine("Total Storage: $totalStorage")
        appendLine("Used Storage: $usedStorage")
        appendLine("Free Storage: $freeStorage")
        appendLine("System Storage: $systemStorage")
        appendLine("App Storage: $appStorage")
        appendLine("Cache Storage: $cacheStorage")

    }

    // ✅ Build prompt
    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this Storage information in very simple terms:\n$storageInfo"
    } else {
        "Here is the Storage information:\n$storageInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}

