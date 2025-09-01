package com.privacyshield.android.Component.Screen.UsageStatsScreen.model

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayUsage(
    val dayName: String,
    val date: String,
    val totalTime: Long,
    val openCount: Int,
    val lastOpened: Long?
)

data class MyEvent(
    val timeStamp: Long,
    val packageName: String,
    val className: String?,
    val eventType: Int
)

/**
 * Get usage data between any two dates (startDate–endDate)
 */
fun getUsageBetweenDates(
    context: Context,
    packageName: String,
    startDate: Long,
    endDate: Long
): List<DayUsage> {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val events = usageStatsManager.queryEvents(startDate, endDate)

    val dayWise = mutableMapOf<String, MutableList<MyEvent>>()
    val event = UsageEvents.Event()

    while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.packageName == packageName &&
            (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)
        ) {
            val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.timeStamp))
            val copiedEvent = MyEvent(
                event.timeStamp,
                event.packageName,
                event.className,
                event.eventType
            )
            dayWise.getOrPut(dayKey) { mutableListOf() }.add(copiedEvent)
        }
    }

    // Iterate from startDate → endDate day by day
    val result = mutableListOf<DayUsage>()
    val cal = Calendar.getInstance()
    cal.timeInMillis = startDate

    while (cal.timeInMillis <= endDate) {
        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)

        val evts = dayWise[dayKey] ?: emptyList()
        val openCount = evts.count { it.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND }
        val lastOpened = evts.lastOrNull { it.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND }?.timeStamp

        val dayStart = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dayEnd = cal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val dailyUsage = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            dayStart,
            dayEnd
        ).firstOrNull { it.packageName == packageName }

        result.add(
            DayUsage(
                dayName,
                dayKey,
                dailyUsage?.totalTimeInForeground ?: 0,
                openCount,
                lastOpened
            )
        )

        cal.add(Calendar.DAY_OF_YEAR, 1)
    }

    return result
}


fun formatLastOpened(time: Long?): String {
    return if (time != null) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(time))
    } else "Never"
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
