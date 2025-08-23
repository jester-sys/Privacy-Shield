package com.privacyshield.android.Component.Screen.Home


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Model.AppDetail
import java.util.Date


@Composable
fun AppDetailCard(
    app: AppDetail,
    onViewDetails: (AppDetail) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                Color.Black.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .animateContentSize()
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // 👇 Ye pura Row click hoga except arrow
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewDetails(app) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = app.icon.toPainter(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = app.appName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Version: ${app.versionName}", fontSize = 12.sp)
                    // 🔹 Source info with icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (sourceIcon, sourceText, sourceColor) = when {
                            app.isSystemApp -> Triple(
                                Icons.Default.Settings,
                                "System App",
                                Color.Red
                            )
                            app.isFromPlayStore -> Triple(
                                Icons.Default.ShoppingCart,
                                "Play Store",
                                Color(0xFF0F9D58) // green
                            )
                            app.isSideloaded -> Triple(
                                Icons.Default.Download,
                                "Sideloaded",
                                Color(0xFFFFA000) // orange
                            )
                            else -> Triple(
                                Icons.Default.Download,
                                "Unknown Source",
                                Color.Gray
                            )
                        }

                        Icon(
                            imageVector = sourceIcon,
                            contentDescription = sourceText,
                            tint = sourceColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sourceText,
                            color = sourceColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }
            }


            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Permissions:", fontWeight =  FontWeight.Bold, fontSize = 13.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                app.permissions.forEach { perm ->
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = getPermissionColor(perm.toString())
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = perm.toString(),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            Divider()
        }
    }
}
    @Composable
    fun getPermissionColor(permission: String): Color {
        return when {
            permission.contains("CAMERA") ||
                    permission.contains("LOCATION") ||
                    permission.contains("READ_CONTACTS") ||
                    permission.contains("RECORD_AUDIO") ||
                    permission.contains("READ_SMS") ||
                    permission.contains("WRITE_EXTERNAL_STORAGE") -> Color(0xFFD32F2F) // light red


            permission.contains("INTERNET") ||
                    permission.contains("VIBRATE") -> Color(0xFFF57C00) // light green


            else -> Color(0xFF388E3C)
        }
    }



//Text(text = app.appName, fontWeight = FontWeight.Bold)
//Text(text = "Package: ${app.packageName}", fontSize = 12.sp)
//Text(text = "Version: ${app.versionName} (${app.versionCode})", fontSize = 12.sp)
//Text(text = "Min SDK: ${app.minSdk}, Target SDK: ${app.targetSdk}", fontSize = 12.sp)
//Text(text = "Installed: ${Date(app.firstInstallTime)}", fontSize = 10.sp)
//Text(text = "Updated: ${Date(app.lastUpdateTime)}", fontSize = 10.sp)
//Text(text = "Source: ${app.sourceDir}", fontSize = 10.sp)
//Text(text = if (app.isSystemApp) "System App" else "User App", fontSize = 12.sp)