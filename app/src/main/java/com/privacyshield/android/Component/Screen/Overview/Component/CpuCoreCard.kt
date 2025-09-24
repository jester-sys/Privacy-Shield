package com.privacyshield.android.Component.Screen.Overview.Component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Component.Screen.Overview.Model.CpuCoreInfo
import com.privacyshield.android.Component.Settings.theme.AppSettings

@Composable
fun CpuCoreCard(core: CpuCoreInfo, appSettings: AppSettings, primaryColor: Color, textColor: Color) {
    val progressColor = when {
        core.curPercent < 0.4f -> Color(0xFF4CAF50)
        core.curPercent < 0.75f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val coreBackground = if (appSettings.highContrast) {
        if (appSettings.darkTheme) Color(0xFF111111) else Color(0xFFEEEEEE)
    } else progressColor.copy(alpha = 0.12f)

    val coreTextColor = if (appSettings.highContrast) {
        if (appSettings.darkTheme) Color.White else Color.Black
    } else textColor

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Core ${core.coreId}", color = progressColor, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Min: ${core.minFreq}", color = coreTextColor.copy(alpha = 0.7f))
                Text("Max: ${core.maxFreq}", color = coreTextColor.copy(alpha = 0.7f))
            }
            Spacer(Modifier.height(6.dp))
            Text("Current: ${core.curFreq}", color = progressColor)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = core.curPercent,
                color = progressColor,
                trackColor = Color.DarkGray.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}
