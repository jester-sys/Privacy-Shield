package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.google.ai.client.generativeai.GenerativeModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.privacyshield.android.Component.Screen.Overview.viewModel.HardwareViewModel
import com.privacyshield.android.R
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HardwareTab(viewModel: HardwareViewModel = hiltViewModel()) {
    val manufacturer by viewModel.manufacturer.collectAsState()
    val model by viewModel.model.collectAsState()
    val board by viewModel.board.collectAsState()
    val hardware by viewModel.hardware.collectAsState()
    val device by viewModel.device.collectAsState()
    val product by viewModel.product.collectAsState()
    val brand by viewModel.brand.collectAsState()
    val host by viewModel.host.collectAsState()
    val buildId by viewModel.buildId.collectAsState()
    val fingerprint by viewModel.fingerprint.collectAsState()
    val serial by viewModel.serial.collectAsState()

    val supportedAbis by viewModel.supportedAbis.collectAsState()
    val cpuAbi by viewModel.cpuAbi.collectAsState()
    val cpuAbi2 by viewModel.cpuAbi2.collectAsState()
    val cpuCores by viewModel.cpuCores.collectAsState()
    val maxFreq by viewModel.maxFreq.collectAsState()
    val minFreq by viewModel.minFreq.collectAsState()
    val currentFreq by viewModel.currentFreq.collectAsState()

    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val thermalInfo by viewModel.thermalInfo.collectAsState()



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
                    color = Color(0xFFD32F2F),
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
                                Color(0xFFD32F2F).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            Text(
                                "AI  Android Hardware Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD32F2F)
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
                                                    answer = getAndroidHardwareExplanation(
                                                        manufacturer = manufacturer,
                                                        model = model,
                                                        board = board,
                                                        hardware = hardware,
                                                        product = product,
                                                        brand = brand,
                                                        host = host,
                                                        buildId = buildId,
                                                        fingerprint = fingerprint,
                                                        serial = serial,
                                                        supportedAbis = supportedAbis,
                                                        cpuAbi = cpuAbi,
                                                        cpuAbi2 = cpuAbi2,
                                                        maxFreq = maxFreq,
                                                        minFreq = minFreq,
                                                        currentFreq = currentFreq,
                                                        gpuInfo = gpuInfo,
                                                        thermalInfo = thermalInfo.toString(),
                                                        device = device,
                                                    )

                                                    isLoading = false
                                                    question = ""
                                                }
                                            }
                                        },
                                        enabled = question.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text("Ask AI", color = Color(0xFFD32F2F))
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




        // Device Info
        item { SectionHeader("Device Info", Color.White) }
        item { HardwareStatBox("Manufacturer", manufacturer, Color(0xFFD32F2F)) }
        item { HardwareStatBox("Brand", brand,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Model", model,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Board", board,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Hardware", hardware,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Device", device,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Product", product,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Host", host,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Build ID", buildId,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Fingerprint", fingerprint,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Serial", serial,  Color(0xFFD32F2F)) }

        // CPU Info
        item { SectionHeader("CPU Info", Color.White )}
        item { HardwareStatBox("Supported ABIs", supportedAbis, Color(0xFFD32F2F)) }
        item { HardwareStatBox("CPU ABI", cpuAbi,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("CPU ABI2", cpuAbi2,  Color(0xFFD32F2F)) }
        item { HardwareStatBox("CPU Cores", cpuCores.toString(),  Color(0xFFD32F2F)) }
        item { HardwareStatBox("Max Freq", maxFreq, Color(0xFFD32F2F)) }
        item { HardwareStatBox("Min Freq", minFreq, Color(0xFFD32F2F)) }
        item { HardwareStatBox("Current Freq", currentFreq,  Color(0xFFD32F2F)) }

        // GPU Info
        item { SectionHeader("GPU Info",Color.White) }
        item { HardwareStatBox("Renderer", gpuInfo, Color(0xFFD32F2F)) }

        // Thermal Info
        if (thermalInfo.isNotEmpty()) {
            item { SectionHeader("Thermal Sensors", Color.White) }
            items(thermalInfo.entries.toList()) { entry ->
                HardwareStatBox(entry.key, entry.value,  Color(0xFFD32F2F))
            }
        }

//        // Info hint
//        item {
//            InfoHintBox(
//                "Tip: CPU frequencies and thermal sensors may vary by kernel & device. Some fields might return N/A if not exposed.",
//                color = Color(0xFFD32F2F),
//                value = ""
//            )
//        }
    }

}
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getAndroidHardwareExplanation(
                        manufacturer = manufacturer,
                        model = model,
                        board = board,
                        hardware = hardware,
                        product = product,
                        brand = brand,
                        host = host,
                        buildId = buildId,
                        fingerprint = fingerprint,
                        serial = serial,
                        supportedAbis = supportedAbis,
                        cpuAbi = cpuAbi,
                        cpuAbi2 = cpuAbi2,
                        maxFreq = maxFreq,
                        minFreq = minFreq,
                        currentFreq = currentFreq,
                        gpuInfo = gpuInfo,
                        thermalInfo = thermalInfo.toString(),
                        device = device,
                    )



                    isLoading = false
                    showAssistantCard = true
                }

            },
            containerColor = Color(0xFFD32F2F),
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
fun HardwareStatBox(
    title: String,
    value: String,
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
                style = MaterialTheme.typography.bodySmall.copy(color = color),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp

            )

        }
    }
}


suspend fun getAndroidHardwareExplanation(
    manufacturer: String,
    model: String,
    board: String,
    hardware: String,
    device: String,
    product: String,
    brand: String,
    host: String,
    buildId: String,
    fingerprint: String,
    serial: String,
    supportedAbis: String,
    cpuAbi: String,
    cpuAbi2: String,
    maxFreq: String,
    minFreq: String,
    currentFreq: String,
    gpuInfo: String,
    thermalInfo: String,
    userQuestion: String? = null
): String {
    val generateModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA"
    )

    // ✅ Build hardware info string
    val hardwareInfo = buildString {
        appendLine("Manufacturer: $manufacturer")
        appendLine("Model: $model")
        appendLine("Board: $board")
        appendLine("Hardware: $hardware")
        appendLine("Device: $device")
        appendLine("Product: $product")
        appendLine("Brand: $brand")
        appendLine("Host: $host")
        appendLine("Build ID: $buildId")
        appendLine("Fingerprint: $fingerprint")
        appendLine("Serial Number: $serial")
        appendLine("Supported ABIs: $supportedAbis")
        appendLine("CPU ABI 1 Present: $cpuAbi")
        appendLine("CPU ABI 2 Present: $cpuAbi2")
        appendLine("Max CPU Frequency: $maxFreq")
        appendLine("Min CPU Frequency: $minFreq")
        appendLine("Current CPU Frequency: $currentFreq")
        appendLine("GPU Info: $gpuInfo")
        appendLine("Thermal Info: $thermalInfo")
    }

    // ✅ Build prompt
    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this Android hardware information in very simple terms:\n$hardwareInfo"
    } else {
        "Here is the Android hardware information:\n$hardwareInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = generateModel.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}
