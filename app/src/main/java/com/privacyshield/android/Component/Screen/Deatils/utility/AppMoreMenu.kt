package com.privacyshield.android.Component.Screen.Deatils.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.privacyshield.android.Model.AppDetail
@Composable
fun AppMoreMenu(
    app: AppDetail,
    onAction: (AppDetail, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Actions",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF121212), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp)
        ) {
            @Composable
            fun menuItem(
                text: String,
                icon: ImageVector,
                action: String
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color =Color.White
                            )
                        )
                    },
                    onClick = {
                        expanded = false
                        onAction(app, action)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )
            }

            menuItem("Data Usage", Icons.Default.DataUsage, "data_usage")
            menuItem("Battery Usage", Icons.Default.BatteryFull, "battery_usage")
            menuItem("Storage Usage", Icons.Default.Storage, "storage_usage")
            menuItem("Permissions", Icons.Default.Lock, "permissions")
            menuItem("Open by Default", Icons.Default.Settings, "open_by_default")
        }
    }
}

