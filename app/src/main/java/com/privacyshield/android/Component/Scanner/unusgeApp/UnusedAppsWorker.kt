package com.privacyshield.android.Component.Scanner.unusgeApp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.privacyshield.android.MainActivity
import java.util.concurrent.TimeUnit

class UnusedAppsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("UnusedAppsWorker", "🚀 Worker started")

        val unusedApps = getUnusedApps(applicationContext, minDays = 3)

        Log.d("UnusedAppsWorker", "📱 Total unused apps found: ${unusedApps.size}")
        unusedApps.forEach {
            Log.d(
                "UnusedAppsWorker",
                "Unused App -> Name: ${it.name}, Package: ${it.packageName}, LastUsed: ${it.lastUsed}"
            )
        }

        if (unusedApps.isNotEmpty()) {
            val appNames = unusedApps.take(5).joinToString { it.name } // max 5 apps show
            Log.d("UnusedAppsWorker", "🔔 Showing notification for apps: $appNames")
            showUnusedAppsNotification(appNames, unusedApps.size)
        } else {
            Log.d("UnusedAppsWorker", "⚠️ No unused apps found")
        }

        return Result.success()
    }

    private fun showUnusedAppsNotification(appNames: String, total: Int) {
        val channelId = "unused_apps_channel"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ HIGH importance channel for background notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Unused Apps",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows notifications for unused apps"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("UnusedAppsWorker", "📢 Notification channel created: $channelId")
        }

        // ✅ PendingIntent for when user taps notification
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_fragment", "unused_apps") // Direct open karega unused apps fragment
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Build notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📱 Unused Apps Detected")
            .setContentText("$total apps not used in 3 days")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$total apps unused for 3+ days: $appNames\n\nTap to see all unused apps"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(applicationContext, android.R.color.holo_red_dark))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()

        // ✅ Show notification (unique ID so it replaces previous ones)
        notificationManager.notify(1001, notification)
        Log.d("UnusedAppsWorker", "✅ Background notification sent with $total apps")
    }
}

// ✅ Schedule the worker (Har 2 din, 4 hours flexibility)
fun scheduleUnusedAppsWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<UnusedAppsWorker>(
        2, TimeUnit.DAYS,    // Har 2 din
        4, TimeUnit.HOURS    // Battery optimization ke liye flexibility
    )
        .setInitialDelay(30, TimeUnit.MINUTES) // App open hone ke 30 min baad first run
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "unused_apps_work",
        ExistingPeriodicWorkPolicy.UPDATE,  // Better than REPLACE
        workRequest
    )

    Log.d("WorkerSchedule", "✅ UnusedAppsWorker scheduled: Every 2 days with 4 hours flexibility")
}