package com.privacyshield.android.Component.Screen.Home.Action

import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.privacyshield.android.Component.Screen.Model.StorageUsage

@Composable
fun StorageUsageDialog(
    usage: StorageUsage,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
//                Image(
//                    painter = rememberDrawablePainter(usage.icon),
//                    contentDescription = usage.appName,
//                    modifier = Modifier.size(40.dp)
//                )
                Spacer(Modifier.width(8.dp))
                Text(usage.appName, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column {
                Text("Total: ${Formatter.formatFileSize(LocalContext.current, usage.totalBytes)}")

                Spacer(Modifier.height(8.dp))

                StorageProgress("App", usage.appBytes, usage.totalBytes)
                StorageProgress("Data", usage.dataBytes, usage.totalBytes)
                StorageProgress("Cache", usage.cacheBytes, usage.totalBytes)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        }
    )
}


@Composable
fun StorageProgress(label: String, value: Long, total: Long) {
    val context = LocalContext.current
    val percent = if (total > 0) (value.toFloat() / total.toFloat()) else 0f

    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            "$label: ${Formatter.formatFileSize(context, value)}",
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}
