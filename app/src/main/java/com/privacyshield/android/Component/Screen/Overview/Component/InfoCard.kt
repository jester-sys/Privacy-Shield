package com.privacyshield.android.Component.Screen.Overview.Component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun InfoCard(
    title: String,
    value: String,
    cardColor: Color,
    icon: ImageVector = Icons.Default.Info,
    highContrast: Boolean = false,
    darkTheme: Boolean = false,
    textColor: Color = Color.Unspecified
) {
    val backgroundColor = when {
        highContrast && darkTheme -> Color(0xFF111111)
        highContrast && !darkTheme -> Color(0xFFEEEEEE)
        darkTheme -> cardColor.copy(alpha = 0.1f)
        else -> cardColor.copy(alpha = 0.1f)
    }
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    // Card as container only, no elevation
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
                .background( Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
