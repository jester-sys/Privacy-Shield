package com.privacyshield.android.Component.Screen.UsageStatsScreen

import android.annotation.SuppressLint
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context

@SuppressLint("WrongConstant")
fun getAppUsageStats(context: Context, packageName: String): UsageStats? {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000L * 60 * 60 * 24 // last 24 hours

    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        endTime
    )

    return stats.find { it.packageName == packageName }
}
