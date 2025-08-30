package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.hardware.Sensor
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyshield.android.Component.Screen.Overview.Model.SensorInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.SensorsViewModel


@Composable
fun SensorsTab(viewModel: SensorsViewModel = hiltViewModel()) {
    val sensors by viewModel.sensors.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                "Available Sensors (${sensors.size})",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        itemsIndexed(sensors) { index, sensor ->
            SensorBox(sensor = sensor, index = index)
        }
    }
}

@Composable
fun SensorBox(sensor: SensorInfo, index: Int) {
// ✅ Solid Color list (without MaterialTheme)
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFFF44336), // Red
        Color(0xFF9C27B0)  // Purple
    )


    val bgColor = colors[index % colors.size]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor.copy(alpha = 0.2f))
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
