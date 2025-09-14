package com.privacyshield.android.Component.Screen.Home.Action

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Screen.Model.StorageUsage
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.ui.theme.GreenPrimary


@Composable
fun StorageUsageDialog(
    context: Context,
    usage: StorageUsage,
    app: AppDetail,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF050505)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AppIcon(app.packageName)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            usage.appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Define usage items with fixed colors
                val usageItems = listOf(
                    Triple("App", usage.appBytes, Color(0xFF2196F3)),        // Blue
                    Triple("Data", usage.dataBytes, Color(0xFF90CAF9)),      // Light Blue
                    Triple("Cache", usage.cacheBytes, Color(0xFFFF9800))     // Orange
                )

                usageItems.forEach { (label, value, color) ->
                    StorageUsageItem(label, value, usage.totalBytes, color)
                }
                Spacer(Modifier.height(8.dp))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Spacer(Modifier.height(16.dp))

                Text(
                    "Total Storage Used: ${  
                        Formatter.formatFileSize(LocalContext.current, usage.totalBytes) 
                    }",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Spacer(Modifier.height(16.dp))


                Button(
                    onClick = {    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${usage.packageName}")
                    }
                        context.startActivity(intent)},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Manage", color = Color.White)
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Close", color = Color.White)
                }


            }
        }
    }
}

@Composable
fun StorageUsageItem(
    label: String,
    value: Long,
    total: Long,
    color: Color
) {
    val context = LocalContext.current
    val percent = if (total > 0) (value.toFloat() / total.toFloat()) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium,  color = Color.White)
            Text(
                Formatter.formatFileSize(context, value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        LinearProgressIndicator(
            progress = { percent },
            color = color,   // 👈 label-specific color
            trackColor = Color.DarkGray.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    }
}

