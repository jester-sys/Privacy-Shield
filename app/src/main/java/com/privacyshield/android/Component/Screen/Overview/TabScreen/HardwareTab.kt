package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyshield.android.Component.Screen.Overview.Utility.HardwareViewModel
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Header
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📱 Hardware Details", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF512DA8)))
                TextButton(onClick = { coroutineScope.launch { viewModel.refresh() } }) {
                    Text("Refresh", color = Color(0xFF512DA8))
                }
            }
        }

        // Device Info
        item { SectionHeader("Device Info", Color(0xFF512DA8)) }
        item { TintedStatBox("Manufacturer", manufacturer, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Brand", brand, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Model", model, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Board", board, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Hardware", hardware, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Device", device, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Product", product, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Host", host, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Build ID", buildId, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Fingerprint", fingerprint, 1f, Color(0xFF512DA8)) }
        item { TintedStatBox("Serial", serial, 1f, Color(0xFF512DA8)) }

        // CPU Info
        item { SectionHeader("CPU Info", Color(0xFF009688)) }
        item { TintedStatBox("Supported ABIs", supportedAbis, 1f, Color(0xFF009688)) }
        item { TintedStatBox("CPU ABI", cpuAbi, 1f, Color(0xFF009688)) }
        item { TintedStatBox("CPU ABI2", cpuAbi2, 1f, Color(0xFF009688)) }
        item { TintedStatBox("CPU Cores", cpuCores.toString(), 1f, Color(0xFF009688)) }
        item { TintedStatBox("Max Freq", maxFreq, 1f, Color(0xFF009688)) }
        item { TintedStatBox("Min Freq", minFreq, 1f, Color(0xFF009688)) }
        item { TintedStatBox("Current Freq", currentFreq, 1f, Color(0xFF009688)) }

        // GPU Info
        item { SectionHeader("GPU Info", Color(0xFF1976D2)) }
        item { TintedStatBox("Renderer", gpuInfo, 1f, Color(0xFF1976D2)) }

        // Thermal Info
        if (thermalInfo.isNotEmpty()) {
            item { SectionHeader("Thermal Sensors", Color(0xFFFF5722)) }
            items(thermalInfo.entries.toList()) { entry ->
                TintedStatBox(entry.key, entry.value, 1f, Color(0xFFFF5722))
            }
        }

        // Info hint
        item {
            InfoHintBox(
                "Tip: CPU frequencies and thermal sensors may vary by kernel & device. Some fields might return N/A if not exposed.",
                color = Color(0xFF009688)
            )
        }
    }
}
