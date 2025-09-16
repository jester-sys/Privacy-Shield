package com.privacyshield.android.Component.Service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.privacyshield.android.Component.Scanner.QuickScan.CleanerViewModel
import com.privacyshield.android.Component.VirusTotal.VirusTotalManager
import com.privacyshield.android.Component.VirusTotal.VirusTotalRepository
import com.privacyshield.android.Component.VirusTotal.VirusTotalResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class VirusTotalScanService : LifecycleService() {

    @Inject lateinit var virusTotalRepo: VirusTotalRepository
    @Inject lateinit var virusTotalManager: VirusTotalManager

    private val notificationId = 1001
    private val channelId = "vt_scan_channel"
    private var isCancelled = false

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "CANCEL_VT_SCAN") {
                isCancelled = true
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter("CANCEL_VT_SCAN")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cancelReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(cancelReceiver, filter, RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(cancelReceiver)
        } catch (e: Exception) {
            // Receiver was not registered, ignore
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val paths = intent?.getStringArrayExtra("files") ?: emptyArray()
        val mode = intent?.getStringExtra("mode") ?: "SCAN"

        val files = paths.map { File(it) }.filter { it.exists() && it.isFile }.toSet()

        if (files.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        updateNotification(0, if (mode == "SCAN") "Starting scan..." else "Starting download...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (mode == "SCAN") {
                    startScan(files)
                } else {
                    virusTotalManager.scanFilesWithVirusTotal(files)
                }

                // Send completion broadcast
                val completeIntent = Intent("VT_SCAN_COMPLETE").apply {
                    putExtra("files", paths)
                }
                LocalBroadcastManager.getInstance(this@VirusTotalScanService).sendBroadcast(completeIntent)

            } catch (e: Exception) {
                Log.e("VirusTotalScanService", "Error during scan: ${e.message}")

                // Send error broadcast
                val errorIntent = Intent("VT_SCAN_ERROR").apply {
                    putExtra("error", e.message ?: "Unknown error")
                }
                LocalBroadcastManager.getInstance(this@VirusTotalScanService).sendBroadcast(errorIntent)

                // Show error in notification
                updateNotificationWithError(e.message ?: "Network error")
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun startScan(files: Set<File>) {
        val results = mutableListOf<VirusTotalResult>()

        for ((index, file) in files.withIndex()) {
            if (isCancelled) return

            // Calculate accurate progress percentage
            val progress = ((index) * 100f / files.size).toInt()

            // Update notification with current file name
            updateNotification(
                progress = progress,
                text = "Scanning ${file.name}..."
            )

            // Send progress update using LocalBroadcastManager
            val progressIntent = Intent("VT_SCAN_PROGRESS").apply {
                putExtra("progress", progress)
                putExtra("currentFile", index + 1)
                putExtra("totalFiles", files.size)
                putExtra("fileName", file.name)
            }
            LocalBroadcastManager.getInstance(this@VirusTotalScanService).sendBroadcast(progressIntent)

            // Scan the file with retry logic
            try {
                val result = virusTotalRepo.scanFileWithRetry(file, maxRetries = 3)
                results.add(result)

                // Update progress after successful scan
                val newProgress = ((index + 1) * 100f / files.size).toInt()
                updateNotification(newProgress, "Scanned ${file.name}")

            } catch (e: Exception) {
                Log.e("VirusTotalScanService", "Error scanning file ${file.name}: ${e.message}")

                // Send error for this specific file
                val errorIntent = Intent("VT_FILE_SCAN_ERROR").apply {
                    putExtra("fileName", file.name)
                    putExtra("error", e.message ?: "Unknown error")
                }
                LocalBroadcastManager.getInstance(this@VirusTotalScanService).sendBroadcast(errorIntent)

                // Continue with next file instead of stopping
                continue
            }
        }

        // Save results after all files are scanned
        if (results.isNotEmpty() && !isCancelled) {
            virusTotalManager.saveResultsToDownloads(results)
        }
    }

    private fun updateNotification(progress: Int, text: String) {
        val cancelIntent = Intent("CANCEL_VT_SCAN")
        val cancelPending = PendingIntent.getBroadcast(
            this, 0, cancelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VirusTotal Scan")
            .setContentText("$text $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .addAction(android.R.drawable.ic_delete, "Cancel", cancelPending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)

        startForeground(notificationId, builder.build())
    }

    private fun updateNotificationWithError(error: String) {
        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VirusTotal Scan Failed")
            .setContentText("Error: $error")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setOngoing(false)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId + 1, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "VirusTotal Scans",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of VirusTotal scans"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}