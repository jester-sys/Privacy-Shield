package com.privacyshield.android.Component.Screen.Home


import android.content.Context
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Component.Screen.Home.Action.manageOpenByDefault
import com.privacyshield.android.Component.Screen.Home.Action.managePermissions
import com.privacyshield.android.Component.Screen.Home.Action.openApp
import com.privacyshield.android.Component.Screen.Home.Action.shareApp
import com.privacyshield.android.Component.Screen.Home.Action.showBatteryUsage
import com.privacyshield.android.Component.Screen.Home.Action.AppDataUsageCard
import com.privacyshield.android.Component.Screen.Home.Action.showStorageUsage
import com.privacyshield.android.Component.Screen.Home.Action.uninstallApp
import com.privacyshield.android.Component.Screen.Home.utility.AppActionPopupMenu
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.R


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDetailCard(
    app: AppDetail,
    onViewDetails: (AppDetail) -> Unit,
    onAction: (AppDetail, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Dialog states
    var showDataUsageDialog by remember { mutableStateOf(false) }
    var showBatteryUsageDialog by remember { mutableStateOf(false) }
    var showStorageUsageDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

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

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewDetails(app) }
                    .combinedClickable(
                        onClick = { onViewDetails(app) },
                        onLongClick = { showMenu = true }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = app.icon.toPainter(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Version: ${app.versionName}",
                        fontSize = 12.sp
                    )

                    // 🔹 Source info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when {
                            app.isSystemApp -> {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "System App",
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("System App", color = Color(0xFFFFA000), fontSize = 12.sp)
                            }

                            app.isFromPlayStore -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_playstore_icon),
                                    contentDescription = "Play Store",
                                    tint = Color(0xFF0F9D58),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Store", color = Color(0xFF0F9D58), fontSize = 12.sp)
                            }

                            app.isSideloaded -> {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Sideloaded",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sideloaded", color = Color.Red, fontSize = 12.sp)
                            }

                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Unknown Source",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unknown Source", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
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
            Text(text = "Permissions:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

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
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
        }
        AppActionPopupMenu(
            showSheet = showMenu,
            onDismiss = { showMenu = false },
            app = app,
            onAction = { clickedApp, action ->
                when (action) {
                    "data_usage" -> showDataUsageDialog = true
                    "battery_usage" -> showBatteryUsageDialog = true
                    "storage_usage" -> showStorageUsageDialog = true
                    "permissions" -> showPermissionsDialog = true
                    "open_by_default" -> manageOpenByDefault(context, clickedApp)
                    "open" -> openApp(context, clickedApp)
                    "uninstall" -> uninstallApp(context, clickedApp)
                    "share" -> shareApp(context, clickedApp)
                }
            }
        )

    }
    if (showDataUsageDialog) {
        AppDataUsageCard(context, app,onDismiss = {
            showDataUsageDialog = false
        }) // Composable dialog
    }
    if (showBatteryUsageDialog) {
        showBatteryUsage(context, app)
    }
    if (showStorageUsageDialog) {
        showStorageUsage(context, app)
    }
    if (showPermissionsDialog) {
        managePermissions(context, app)
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