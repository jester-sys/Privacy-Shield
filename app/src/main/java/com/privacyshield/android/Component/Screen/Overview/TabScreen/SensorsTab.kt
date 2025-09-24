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
import com.google.ai.client.generativeai.GenerativeModel
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.privacyshield.android.Component.Screen.Overview.Component.SectionHeader
import com.privacyshield.android.Component.Screen.Overview.Model.SensorInfo
import com.privacyshield.android.Component.Screen.Overview.viewModel.SensorsViewModel
import com.privacyshield.android.R
import kotlinx.coroutines.launch


@Composable
fun SensorsTab(viewModel: SensorsViewModel = hiltViewModel()) {
    val sensors by viewModel.sensors.collectAsState()


    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var showAssistantCard by remember { mutableStateOf(false) }

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
                    color = Color(0xFF1DE9B6),
                    trackColor = Color.DarkGray
                )
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {



                    // 🔹 AI Storage Assistant Card
                    if (showAssistantCard) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFF1DE9B6).copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                    Text(
                                        "AI  Android Sensors Assistant",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1DE9B6)
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
                                            label = {
                                                Text(
                                                    "Ask about Android Hardware",
                                                    color = Color.White
                                                )
                                            },
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
                                                            answer = getSensorsExplanation(listOf(sensors))
                                                            isLoading = false
                                                            question = ""
                                                        }
                                                    }
                                                },
                                                enabled = question.isNotBlank(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                            ) {
                                                Text("Ask AI", color = Color(0xFF1DE9B6))
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
                    SectionHeader("Available Sensors (${sensors.size})", Color.White)
                }

                itemsIndexed(sensors) { index, sensor ->
                    SensorBox(sensor = sensor, index = index)
                }
            }

        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getSensorsExplanation(listOf(sensors))


                    isLoading = false
                    showAssistantCard = true
                }

            },
            containerColor = Color(0xFF1DE9B6),
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
        fun SensorBox(sensor: SensorInfo, index: Int) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2A2A2A))
                    .padding(16.dp)
            ) {
                Column {

                    Text(
                        text = sensor.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ Sensor Details
                    SensorInfoRow(label = "Type", value = sensor.type.toString())
                    SensorInfoRow(label = "Vendor", value = sensor.vendor)
                    SensorInfoRow(label = "Version", value = sensor.version.toString())
                    SensorInfoRow(label = "Resolution", value = "${sensor.resolution}")
                    SensorInfoRow(label = "Power", value = "${sensor.power} mA")
                    SensorInfoRow(label = "Max Range", value = "${sensor.maxRange}")
                    SensorInfoRow(label = "Min Delay", value = "${sensor.minDelay} µs")

                }

            }

        }


        @Composable
        private fun SensorInfoRow(label: String, value: String) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )
            }
        }


suspend fun getSensorsExplanation(
    sensors: List<List<SensorInfo>>,
    userQuestion: String? = null
): String {
    val generateModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA"
    )

    // ✅ Build sensors info string
    val sensorsInfo = buildString {
        if (sensors.isEmpty()) {
            appendLine("No sensor data available.")
        } else {
            sensors.forEachIndexed { groupIndex, sensorGroup ->
                appendLine("Sensor Group ${groupIndex + 1}:")
                sensorGroup.forEach { sensor ->
                    appendLine("  Name: ${sensor.name}")
                    appendLine("  Type: ${sensor.type}")
                    appendLine("  Vendor: ${sensor.vendor}")
                    appendLine("  Max Range: ${sensor.maxRange}")
                    appendLine("  Resolution: ${sensor.resolution}")
                    appendLine("  Power: ${sensor.power} mA")
                    appendLine()
                }
            }
        }
    }

    // ✅ Build prompt
    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this Android sensors information in very simple terms:\n$sensorsInfo"
    } else {
        "Here is the Android sensors information:\n$sensorsInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = generateModel.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}
