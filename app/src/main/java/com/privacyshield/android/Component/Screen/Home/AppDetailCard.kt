package com.privacyshield.android.Component.Screen.Home


import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Component.Screen.Home.Action.manageOpenByDefault
import com.privacyshield.android.Component.Screen.Home.Action.openApp
import com.privacyshield.android.Component.Screen.Home.Action.shareApp
import com.privacyshield.android.Component.Screen.Home.Action.showBatteryUsage
import com.privacyshield.android.Component.Screen.Home.Action.AppDataUsageCard
import com.privacyshield.android.Component.Screen.Home.Action.ManagePermissions
import com.privacyshield.android.Component.Screen.Home.Action.StorageUsageDialog
import com.privacyshield.android.Component.Screen.Home.Action.showStorageUsage
import com.privacyshield.android.Component.Screen.Home.Action.uninstallApp
import com.privacyshield.android.Component.Screen.Home.utility.AppActionPopupMenu
import com.privacyshield.android.Component.Screen.Model.StorageUsage
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.Model.AppPermission
import com.privacyshield.android.R
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import com.privacyshield.android.Component.Screen.Home.utility.getPermissionDetails
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale


@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDetailCard(
    app: AppDetail,
    onViewDetails: (AppDetail) -> Unit,
    onAction: (AppDetail, String) -> Unit,
    activity: Activity
) {

    var showMenu by remember { mutableStateOf(false) }

    // Dialog states
    var showDataUsageDialog by remember { mutableStateOf(false) }
    var showBatteryUsageDialog by remember { mutableStateOf(false) }
    var showStorageUsageDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }



    var storageUsage by remember { mutableStateOf<StorageUsage?>(null) }




    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ){


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                Color(0xFF1E1E1E),
                RoundedCornerShape(12.dp)
            )
            .animateContentSize()
            .padding(12.dp)

    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {

            AppIcon(
                packageName = app.packageName,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewDetails(app) }
                    .combinedClickable(
                        onClick = { onViewDetails(app) },
                        onLongClick = { showMenu = true }
                    )
            ) {
                Text(
                    text = app.appName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )


                Text(
                    text = app.packageName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Version
                Text(
                    text = "Version: ${app.versionName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Install Time
                Text(
                    text = "Install Time: ${formatTime(app.firstInstallTime)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Update Time
                Text(
                    text = "Update Time: ${formatTime(app.lastUpdateTime)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 🔹 Source info row
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


    }

            Spacer(modifier = Modifier.height(8.dp))
            ImportantPermissionsRow(app)
            Spacer(modifier = Modifier.height(8.dp))



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
                    "uninstall" -> uninstallApp(activity, clickedApp)
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
//        storageUsage = showStorageUsage(context,app) // 🔹 call function
        if (showStorageUsageDialog) {

            LaunchedEffect(Unit) {
                storageUsage = showStorageUsage(context, app)
            }
            storageUsage?.let {
                StorageUsageDialog(
                    context = context,
                    usage = it,
                    app = app,
                    onDismiss = {
                        showStorageUsageDialog = false
                        storageUsage = null
                    }
                )
            }
        }

    }
    if (showPermissionsDialog) {
        ManagePermissions(
            context = context,
            app = app,
            showDialog = showPermissionsDialog,
            onDismiss = { showPermissionsDialog = false }
        )
    }
    }






@Composable
fun ImportantPermissionsRow(app: AppDetail) {
    val context = LocalContext.current
    var selectedPermissionInfo by remember { mutableStateOf(AnnotatedString("")) }
    var showDialog by remember { mutableStateOf(false) }

    val permissionsWithIcon = app.permissions.filter { perm ->
        val icon = getPermissionIcon(perm)
        icon != Icons.Default.Info
    }

    if (permissionsWithIcon.isNotEmpty()) {

        LazyRow(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(permissionsWithIcon) { perm ->
                val color = getPermissionColor(perm)

                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            selectedPermissionInfo = getPermissionDetails(perm)

                            showDialog = true
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = getPermissionIcon(perm),
                            contentDescription = perm.name,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth().
                    padding(16.dp)
            ) {
                Box(modifier = Modifier.background(Color(0xFF121212)).padding(16.dp)) {
                Column(modifier = Modifier.background(Color(0xFF121212))) {
                    Text(
                        text = "Permission Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Show your AnnotatedString here
                    Text(
                        text = selectedPermissionInfo, // should be AnnotatedString
                        style = TextStyle(fontSize = 16.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Close")
                        }
                    }
                }
                    }
            }
        }
    }

}

enum class PermissionLevel { DANGEROUS, NORMAL, SAFE }


fun getPermissionLevel(perm: AppPermission): PermissionLevel {
    return when (perm.name) {
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> PermissionLevel.DANGEROUS

        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE -> PermissionLevel.NORMAL

        else -> PermissionLevel.SAFE
    }
}


fun  getPermissionIcon(perm: AppPermission): ImageVector {
    return when (perm.name) {
        // Camera & media
        android.Manifest.permission.CAMERA -> Icons.Default.Camera
        android.Manifest.permission.RECORD_AUDIO -> Icons.Default.Mic
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE -> Icons.Default.Storage
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO -> Icons.Default.Photo

        // Location
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION -> Icons.Default.Place

        // Contacts & SMS & Call
        android.Manifest.permission.READ_CONTACTS -> Icons.Default.Contacts
        android.Manifest.permission.WRITE_CONTACTS -> Icons.Default.Contacts
        android.Manifest.permission.READ_SMS -> Icons.Default.Message
        android.Manifest.permission.SEND_SMS -> Icons.Default.Send
        android.Manifest.permission.READ_CALL_LOG -> Icons.Default.Call
        android.Manifest.permission.WRITE_CALL_LOG -> Icons.Default.Call

        // Network & Internet
        android.Manifest.permission.INTERNET -> Icons.Default.Wifi
        android.Manifest.permission.ACCESS_NETWORK_STATE -> Icons.Default.NetworkCell

        // Calendar
        android.Manifest.permission.READ_CALENDAR -> Icons.Default.CalendarToday
        android.Manifest.permission.WRITE_CALENDAR -> Icons.Default.CalendarToday

        // Sensors
        android.Manifest.permission.BODY_SENSORS -> Icons.Default.Favorite

        // Vibration / Haptics
        android.Manifest.permission.VIBRATE -> Icons.Default.Vibration

        // Others
        else -> Icons.Default.Info
    }
}





fun getPermissionColor(perm: AppPermission): Color {
    return when (getPermissionLevel(perm)) {
        PermissionLevel.DANGEROUS -> Color(0xFFD32F2F)
        PermissionLevel.NORMAL -> Color(0xFFF57C00)
        PermissionLevel.SAFE -> Color(0xFF388E3C)
    }
}
fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

//Text(text = app.appName, fontWeight = FontWeight.Bold)
//Text(text = "Package: ${app.packageName}", fontSize = 12.sp)
//Text(text = "Version: ${app.versionName} (${app.versionCode})", fontSize = 12.sp)
//Text(text = "Min SDK: ${app.minSdk}, Target SDK: ${app.targetSdk}", fontSize = 12.sp)
//Text(text = "Installed: ${Date(app.firstInstallTime)}", fontSize = 10.sp)
//Text(text = "Updated: ${Date(app.lastUpdateTime)}", fontSize = 10.sp)
//Text(text = "Source: ${app.sourceDir}", fontSize = 10.sp)
//Text(text = if (app.isSystemApp) "System App" else "User App", fontSize = 12.sp)