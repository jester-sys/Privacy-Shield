package com.privacyshield.android.Component.Screen.Overview.TabScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.privacyshield.android.Component.Screen.Overview.Model.CpuCoreInfo
import java.io.File

@Composable
fun CpuTab() {
    val cpuInfoText = File("/proc/cpuinfo").readText()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "CPU Information",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF1976D2) // Blue Highlight
            )
        }

        // Split CPU info line by line and show
        val lines = cpuInfoText.split("\n").filter { it.isNotBlank() }
        items(lines.size) { i ->
            val parts = lines[i].split(":")
            if (parts.size == 2) {
                InfoCard(title = parts[0].trim(), value = parts[1].trim(), color = Color(0xFF64B5F6))
            }
        }
    }
}
@Composable
fun InfoCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = color)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        }
    }
}


fun getCpuCoresInfo(): List<CpuCoreInfo> {
    val cores = Runtime.getRuntime().availableProcessors()
    val list = mutableListOf<CpuCoreInfo>()

    for (i in 0 until cores) {
        val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq/"
        val minFreq = readCpuFile(basePath + "cpuinfo_min_freq")
        val maxFreq = readCpuFile(basePath + "cpuinfo_max_freq")
        val curFreq = readCpuFile(basePath + "scaling_cur_freq")

        list.add(
            CpuCoreInfo(
                coreId = i,
                minFreq = formatFreq(minFreq),
                maxFreq = formatFreq(maxFreq),
                curFreq = formatFreq(curFreq)
            )
        )
    }
    return list
}

fun formatFreq(freq: String): String {
    return if (freq == "N/A") "N/A"
    else try {
        val mhz = freq.toLong() / 1000
        "$mhz MHz"
    } catch (e: Exception) {
        "N/A"
    }
}