package com.privacyshield.android.Component.Screen.Home.Action

import android.annotation.SuppressLint
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.storage.StorageManager
import android.widget.Toast
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


fun managePermissions(context: Context, app: AppDetail) {
    // TODO: Add permission management logic
    Toast.makeText(context, "${app.appName} permission management not implemented yet", Toast.LENGTH_SHORT).show()
}

fun manageOpenByDefault(context: Context, app: AppDetail) {
    // TODO: Add open by default management logic
    Toast.makeText(context, "${app.appName} open by default not implemented yet", Toast.LENGTH_SHORT).show()
}

fun openApp(context: Context, app: AppDetail) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        Toast.makeText(context, "Cannot open ${app.appName}", Toast.LENGTH_SHORT).show()
    }
}

fun uninstallApp(context: Context, app: AppDetail) {
    val intent = android.content.Intent(android.content.Intent.ACTION_DELETE)
    intent.data = android.net.Uri.parse("package:${app.packageName}")
    context.startActivity(intent)
}

fun shareApp(context: Context, app: AppDetail) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out this app: ${app.appName}")
    context.startActivity(android.content.Intent.createChooser(intent, "Share ${app.appName} via"))
}

