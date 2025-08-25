package com.privacyshield.android.Component.Screen.Home.Action

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.net.Uri
import android.os.storage.StorageManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.privacyshield.android.Component.Screen.Model.StorageUsage
import com.privacyshield.android.Model.AppDetail



fun showBatteryUsage(context: Context, app: AppDetail) {
    try {
        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Battery usage not available", Toast.LENGTH_SHORT).show()
    }
}

@SuppressLint("NewApi", "ServiceCast")
fun showStorageUsage(context: Context, app: AppDetail): StorageUsage? {
    return try {
        if (!hasUsageStatsPermission(context)) {
            requestUsageStatsPermission(context)
            return null
        }

        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

        val stats = storageStatsManager.queryStatsForPackage(
            StorageManager.UUID_DEFAULT,
            app.packageName,
            android.os.Process.myUserHandle()
        )

        StorageUsage(
            appName = app.appName,
            packageName = app.packageName,
            appBytes = stats.appBytes,
            dataBytes = stats.dataBytes,
            cacheBytes = stats.cacheBytes,
            totalBytes = stats.appBytes + stats.dataBytes + stats.cacheBytes
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


@Composable
fun ManagePermissions(
    context: Context,
    app: AppDetail,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        PermissionDialog(
            app = app,
            onDismiss = { onDismiss() },
            onNavigate = {
                onDismiss()
                try {
                    val intent = Intent("android.intent.action.MANAGE_APP_PERMISSIONS").apply {
                        data = Uri.fromParts("package", app.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    val fallbackIntent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${app.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    context.startActivity(fallbackIntent)
                }
            }
        )
    }
}

fun manageOpenByDefault(context: Context, app: AppDetail) {
    try {
        val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()

        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
    }
}


fun openApp(context: Context, app: AppDetail) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        Toast.makeText(context, "Cannot open ${app.appName}", Toast.LENGTH_SHORT).show()
    }
}

fun uninstallApp(activity: Activity, app: AppDetail) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(activity, "Cannot open app settings for ${app.appName}", Toast.LENGTH_SHORT).show()
    }
}

fun shareApp(context: Context, app: AppDetail) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out this app: ${app.appName}")
    context.startActivity(android.content.Intent.createChooser(intent, "Share ${app.appName} via"))
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun requestUsageStatsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

