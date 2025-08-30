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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
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
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Model.CpuCoreInfo
import com.privacyshield.android.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CpuTab() {
    var coresInfo by remember { mutableStateOf(emptyList<CpuCoreInfo>()) }
    var cpuOverview by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var showAssistantCard by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // 🔄 Auto-refresh loop
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
            .background(Color(0xFF1E1E1E)) // Dark background
    ) {
        Column {

            // 🔹 Top Loading Indicator (sirf FAB click ke baad dikhega)
            if (isLoading && !showAssistantCard) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFF0288D1),
                    trackColor = Color.DarkGray
                )
            }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .background(Color(0xFF1E1E1E)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🔹 AI CPU Assistant Card
            if (showAssistantCard) {
            item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0288D1).copy(
                                alpha = 0.12f
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "AI CPU Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(4.dp)
                                )
                            }
                            explanation?.let {
                                Text(it, color = Color.White)
                            }

                            if (explanation != null) {
                                OutlinedTextField(
                                    value = question,
                                    onValueChange = { question = it },
                                    label = { Text("Ask about CPU", color = Color.White) },
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
                                                    answer = getCpuExplanation(question)
                                                    isLoading = false
                                                    question = "" // clear input after ask
                                                }
                                            }
                                        },
                                        enabled = question.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text("Ask AI", color = Color(0xFF0288D1))
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

            // 🔹 CPU Cores Header
            item {
                Text(
                    "CPU Cores",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFE0E0E0)
                )
            }

            // 🔹 Each Core Card
            items(coresInfo.size) { i ->
                val core = coresInfo[i]

                val progressColor = when {
                    core.curPercent < 0.4f -> Color(0xFF4CAF50) // Green
                    core.curPercent < 0.75f -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336) // Red
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)) // round shape
                        .background(color = progressColor.copy(alpha = 0.12f)) // background color
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Core ${core.coreId}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = progressColor
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Min: ${core.minFreq}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                "Max: ${core.maxFreq}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Current: ${core.curFreq}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = progressColor
                        )

                        Spacer(Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { core.curPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)), // fully rounded bar
                            color = progressColor,
                            trackColor = Color.DarkGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }


            // 🔹 CPU Overview Header
            item {
                Text(
                    "CPU Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFE0E0E0),

                )
            }


            // 🔹 Overview InfoCards
            cpuOverview.forEach { (k, v) ->
                item {
                    InfoCard(title = k, value = v, color = Color(0xFF0288D1))
                }
            }


        }

    }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getCpuExplanation("Give me a CPU overview") // 🔹 default overview question
                    isLoading = false
                    showAssistantCard = true
                }
            },
            containerColor = Color(0xFF0288D1),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_icon), // <-- drawable icon
                contentDescription = "AI Info"
            )
        }

    }
}



@Composable
fun InfoCard(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color =Color(0xFF2A2A2A)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 🔹 Title (always on top)
            Text(
                text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                fontWeight = FontWeight.SemiBold

            )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Row with Icon + Value
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 12.dp)
                    )
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = color
                    )
                )
            }
        }
    }
}


fun getCpuOverview(): Map<String, String> {
    val info = mutableMapOf<String, String>()


    info["CPU Model"] = Build.HARDWARE ?: "Unknown"
    info["CPU ABI"] = Build.SUPPORTED_ABIS.joinToString()
    info["CPU Cores"] = Runtime.getRuntime().availableProcessors().toString()
    info["CPU Arch"] = System.getProperty("os.arch") ?: "N/A"


    try {
        val cpuinfo = File("/proc/cpuinfo").readText()
        val lines = cpuinfo.split("\n")
        lines.forEach {
            val parts = it.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()

                when {
                    key.contains("Hardware", true) -> info["Hardware"] = value
                    key.contains("Model name", true) -> info["Model Name"] = value
                    key.contains("Processor", true) -> info["Processor"] = value
                    key.contains("vendor_id", true) -> info["Vendor ID"] = value
                    key.contains("BogoMIPS", true) -> info["BogoMIPS"] = value
                    key.contains("Features", true) -> info["Features"] = value
                    key.contains("cpu family", true) -> info["CPU Family"] = value
                    key.contains("cache size", true) -> info["Cache Size"] = value
                }
            }
        }
    } catch (_: Exception) { }

    // CPU Governor (first core)
    val governor = readCpuFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
    if (governor != "N/A") info["Governor"] = governor

    // Thermal Info (ek example: thermal_zone0)
    val thermal = readCpuFile("/sys/class/thermal/thermal_zone0/temp")
    if (thermal != "N/A") {
        try {
            val celsius = thermal.toFloat() / 1000f
            info["CPU Temperature"] = "%.1f °C".format(celsius)
        } catch (_: Exception) { }
    }

    return info
}


fun getCpuCoresInfo(): List<CpuCoreInfo> {
    val cores = Runtime.getRuntime().availableProcessors()
    val list = mutableListOf<CpuCoreInfo>()

    for (i in 0 until cores) {
        val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq/"
        val minFreq = readCpuFile(basePath + "cpuinfo_min_freq")
        val maxFreq = readCpuFile(basePath + "cpuinfo_max_freq")
        val curFreq = readCpuFile(basePath + "scaling_cur_freq")

        val min = minFreq.toLongOrNull() ?: 0L
        val max = maxFreq.toLongOrNull() ?: 0L
        val cur = curFreq.toLongOrNull() ?: 0L

        val percent = if (max > 0) cur.toFloat() / max else 0f

        list.add(
            CpuCoreInfo(
                coreId = i,
                minFreq = formatFreq(minFreq),
                maxFreq = formatFreq(maxFreq),
                curFreq = formatFreq(curFreq),
                curPercent = percent.coerceIn(0f, 1f)
            )
        )
    }
    return list
}

fun formatFreq(freq: String): String {
    return if (freq == "N/A") "N/A"
    else try {
        val mhz = freq.toLong() / 1000
        if (mhz >= 1000) "${mhz / 1000.0} GHz" else "$mhz MHz"
    } catch (e: Exception) {
        "N/A"
    }
}

fun readCpuFile(path: String): String {
    return try {
        val file = File(path)
        if (file.exists()) {
            file.readText().trim()
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
}
suspend fun getCpuExplanation(userQuestion: String? = null): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA" // apna API key daalna
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
            lines.forEach { line ->
                if (line.contains("processor") || line.contains("Hardware") || line.contains("model name") || line.contains("cpu MHz")) {
                    appendLine(line.trim())
                }
            }
        } catch (e: Exception) {
            appendLine("\n(Core details not available: ${e.localizedMessage})")
        }
    }

    // ✅ Prompt for AI
    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this CPU information in very simple terms:\n$cpuInfo"
    } else {
        "Here is the CPU information:\n$cpuInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}
