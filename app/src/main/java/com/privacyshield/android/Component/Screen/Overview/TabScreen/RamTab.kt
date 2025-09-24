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
import com.privacyshield.android.Component.Screen.Overview.viewModel.RamViewModel
import com.privacyshield.android.R
import kotlinx.coroutines.launch

@Composable
fun RamTab(viewModel: RamViewModel = hiltViewModel()) {
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


    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // Dark background
    ) {
        Column {

            // 🔹 Top Loading Indicator (sirf FAB click ke baad dikhega)
            if (isLoading && !showAssistantCard) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFFF57C00),
                    trackColor = Color.DarkGray
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 🔹 AI CPU Assistant Card
                if (showAssistantCard) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFF57C00).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                Text(
                                    "AI RAM Assistant",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF57C00)
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
                                        label = { Text("Ask about RAM", color = Color.White) },
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
                                                        answer = getRamExplanation(
                                                            totalRam,
                                                            availableRam,
                                                            freeRam,
                                                            cachedRam,
                                                            swapUsed,
                                                            swapTotal
                                                        )
                                                        isLoading = false
                                                        question = ""
                                                    }
                                                }
                                            },
                                            enabled = question.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                        ) {
                                            Text("Ask AI", color = Color(0xFFF57C00))
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


                item {
                    Text(
                        text = "RAM Monitor",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFE0E0E0)
                    )
                }


                item { CardInfo("Total RAM", totalRam, 1f, Color(0xFF4CAF50)) }

                // ✅ Available RAM
                item {
                    CardInfo(
                        "Available RAM",
                        availableRam,
                        progress = if (totalRam.isNotBlank() && availableRam.isNotBlank()) {
                            availableRam.replace(" MB", "").toFloat() /
                                    totalRam.replace(" MB", "").toFloat()
                        } else 0f,
                        progressColor = Color(0xFF2196F3)
                    )
                }

                // ✅ Used RAM
                item {
                    CardInfo(
                        "Used RAM",
                        "${usedRamPercent.toInt()}%",
                        progress = usedRamPercent / 100f,
                        progressColor = if (usedRamPercent > 80) Color.Red else Color(0xFFFFC107)
                    )
                }

                item {
                    val total = totalRam.replace(" MB", "").toFloatOrNull() ?: 0f
                    val free = freeRam.replace(" MB", "").toFloatOrNull() ?: 0f
                    val cached = cachedRam.replace(" MB", "").toFloatOrNull() ?: 0f

                    val progress = if (total > 0f) (free + cached) / total else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A2A2A))
                            .padding(12.dp) // Inner padding
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Free vs Cached RAM",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Free: $freeRam",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50) // Green
                                )
                            )
                            Text(
                                text = "Cached: $cachedRam",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFC107) // Amber
                                )
                            )
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
                                    color = Color(0xFF00BCD4),
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


                item {
                    // Convert strings to Float safely
                    val used = swapUsed.replace(" MB", "").toFloatOrNull() ?: 0f
                    val total = swapTotal.replace(" MB", "").toFloatOrNull() ?: 0f

                    val progress = if (total > 0f) used / total else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A2A2A)) // Tint background
                            .padding(12.dp) // Inner padding
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Swap Usage",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Used: $swapUsed",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFBA68C8) // Lighter purple for used
                                )
                            )
                            Text(
                                text = "Total: $swapTotal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE1BEE7) // Even lighter purple for total
                                )
                            )
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
                                    color = Color(0xFFBA68C8),
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


//        // ✅ Per-app / process memory usage
//        items(processMemory.entries.toList()) { (appName, usage) ->
//            CardInfo(
//                "$appName",
//                usage,
//                progress = if (totalRam.isNotBlank()) {
//                    usage.replace(" MB", "").toFloat() /
//                            totalRam.replace(" MB", "").toFloat()
//                } else 0f,
//                progressColor = Color(0xFFFF9800)
//            )
//        }

                // ✅ Low memory warning
                item {
                    LowMemoryCard(isLowMemory)
                }


            }
        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                        isLoading = true
                        explanation = getRamExplanation(
                            totalRam,
                            availableRam,
                            freeRam,
                            cachedRam,
                            swapUsed,
                            swapTotal
                        )
                        isLoading = false
                        showAssistantCard = true

                }
            },
            containerColor = Color(0xFFF57C00),
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

@Composable
fun CardInfo(
    title: String,
    value: String,
    progress: Float,
    progressColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A2A2A))

    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // spacing between elements
        ) {
            // 📌 Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )

            // 📌 Value
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = progressColor
            )

            // 📌 Progress Bar Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(progressColor.copy(alpha = 0.15f))
            ) {
                // 📌 Progress Bar Foreground
                LinearProgressIndicator(
                    progress = progress,
                    color = progressColor,
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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isLowMemory) Color.Red else Color(0xFFFFFFFF)
            )
            Text(
                text = if (isLowMemory) "Yes" else "No",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


suspend fun getRamExplanation(
    totalRam: String,
    availableRam: String,
    freeRam: String,
    cachedRam: String,
    swapUsed: String,
    swapTotal: String,
    userQuestion: String? = null
): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA" // apna API key daalna
    )

    // ✅ Build RAM info string
    val ramInfo = buildString {
        appendLine("Total RAM: $totalRam")
        appendLine("Available RAM: $availableRam")
        appendLine("Free RAM: $freeRam")
        appendLine("Cached RAM: $cachedRam")
        appendLine("Swap Used: $swapUsed")
        appendLine("Swap Total: $swapTotal")
    }

    // ✅ Build prompt
    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this RAM information in very simple terms:\n$ramInfo"
    } else {
        "Here is the RAM information:\n$ramInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}

