package com.privacyshield.android.Component.Scanner.unusgeApp

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


fun getUnusedApps(
    context: Context,
    minDays: Int = 30,
    ignoredApps: Set<String> = emptySet()
): List<AppInfo> {
    if (!hasUsageStatsPermission(context)) {
        Log.d("UnusedApps", "Usage stats permission not granted")
        return emptyList()
    }

    val pm = context.packageManager
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(minDays.toLong())
    val now = System.currentTimeMillis()

    try {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            now - TimeUnit.DAYS.toMillis(365),
            now
        ) ?: emptyList()

        val statsMap = mutableMapOf<String, UsageStats>()
        for (stat in stats) {
            if (stat.lastTimeUsed > 0) {
                val existing = statsMap[stat.packageName]
                if (existing == null || stat.lastTimeUsed > existing.lastTimeUsed) {
                    statsMap[stat.packageName] = stat
                }
            }
        }

        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val unusedApps = mutableListOf<AppInfo>()

        for (app in allApps) {
            try {
                // skip system apps
                if (shouldSkipApp(app)) continue

                // skip ignored apps
                if (ignoredApps.contains(app.packageName)) continue

                val usageStats = statsMap[app.packageName]
                val lastUsed = usageStats?.lastTimeUsed ?: 0L
                val appName = pm.getApplicationLabel(app).toString()

                if (lastUsed == 0L || lastUsed >= cutoffTime) continue

                val appIcon = try {
                    pm.getApplicationIcon(app.packageName)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)!!
                }

                unusedApps.add(
                    AppInfo(
                        name = appName,
                        packageName = app.packageName,
                        icon = appIcon,
                        lastUsed = lastUsed,
                        installTime = getAppInstallTime(pm, app.packageName)
                    )
                )
            } catch (e: Exception) {
                Log.e("UnusedApps", "Error processing app: ${app.packageName}", e)
            }
        }

        return unusedApps.sortedByDescending { it.lastUsed }
    } catch (e: Exception) {
        Log.e("UnusedApps", "Error fetching usage stats", e)
        return emptyList()
    }
}

// Better app filtering logic
private fun shouldSkipApp(appInfo: ApplicationInfo): Boolean {
    // Skip system apps
    if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
        // Allow updated system apps (like updated Google apps)
        if ((appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return false
        }
        return true
    }

    // Skip certain package patterns
    val packageName = appInfo.packageName
    if (packageName.startsWith("com.android.") ||
        packageName.startsWith("com.google.android.") ||
        packageName.startsWith("com.qualcomm.") ||
        packageName.startsWith("com.sec.") ||
        packageName.startsWith("com.samsung.")) {
        return true
    }

    return false
}

// Usage stats permission check
private fun hasUsageStatsPermission(context: Context): Boolean {
    return try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        val hasPermission = mode == AppOpsManager.MODE_ALLOWED
        Log.d("PermissionCheck", "Usage stats permission: $hasPermission")
        hasPermission
    } catch (e: Exception) {
        Log.e("PermissionCheck", "Error checking permission", e)
        false
    }
}

// Install time with better error handling
private fun getAppInstallTime(pm: PackageManager, packageName: String): Long {
    return try {
        val info = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
        info.firstInstallTime
    } catch (e: PackageManager.NameNotFoundException) {
        Log.w("InstallTime", "Package not found: $packageName")
        System.currentTimeMillis()
    } catch (e: Exception) {
        Log.e("InstallTime", "Error getting install time", e)
        System.currentTimeMillis()
    }
}


data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val lastUsed: Long,
    val installTime: Long = 0
)