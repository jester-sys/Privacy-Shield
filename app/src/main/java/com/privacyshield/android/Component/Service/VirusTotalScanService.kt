package com.privacyshield.android.Component.Service

import android.annotation.SuppressLint
import android.app.Notification
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
import kotlinx.coroutines.CoroutineScope
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
    private val results = mutableListOf<VirusTotalResult>()

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "CANCEL_VT_SCAN") {
                isCancelled = true
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                // Send cancellation broadcast
                val cancelIntent = Intent("VT_SCAN_CANCELLED")
                sendBroadcast(cancelIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(cancelReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val paths = intent?.getStringArrayExtra("files") ?: emptyArray()
        val scanMode = intent?.getStringExtra("scanMode") ?: "SINGLE" // ✅ Scan mode get karo
        val files = paths.map { File(it) }.filter { it.exists() && it.isFile && it.length() <= 32 * 1024 * 1024 }

        if (files.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Register cancel receiver
        val filter = IntentFilter("CANCEL_VT_SCAN")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(cancelReceiver, filter, RECEIVER_NOT_EXPORTED)
        }

        // Create notification and start foreground service
        val notification = createNotification(0, "Starting scan...")
        startForeground(notificationId, notification)

        // Start scanning in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ✅ YAHAN DECIDE KARO KAUNSA FUNCTION CALL KARNA HAI
                if (scanMode == "BATCH") {
                    // ✅ scanFilesWithVirusTotal CALL KARO
                    virusTotalManager.scanFilesWithVirusTotal(files.toSet()) { progress, status ->
                        // Progress update bhejo
                        val progressIntent = Intent("VT_SCAN_PROGRESS").apply {
                            putExtra("progress", progress)
                            putExtra("currentFile", progress) // Approximate current file
                            putExtra("totalFiles", 100) // Total as percentage
                            putExtra("fileName", status)
                            putExtra("scanMode", scanMode)
                        }
                        sendBroadcast(progressIntent)
                    }
                } else {
                    // ✅ NORMAL scanFiles FUNCTION CALL KARO
                    scanFiles(files)
                }

                if (!isCancelled && results.isNotEmpty()) {
                    // Save results
                    virusTotalManager.saveResultsToDownloads(results)

                    // Send completion broadcast
                    val completeIntent = Intent("VT_SCAN_COMPLETE").apply {
                        putExtra("files", paths)
                        putExtra("scanMode", scanMode) // ✅ Scan mode bhi bhejo
                    }
                    sendBroadcast(completeIntent)
                }
            } catch (e: Exception) {
                if (!isCancelled) {
                    Log.e("VirusTotalScanService", "Error: ${e.message}")
                    val errorIntent = Intent("VT_SCAN_ERROR").apply {
                        putExtra("error", e.message ?: "Unknown error")
                        putExtra("scanMode", scanMode) // ✅ Scan mode bhi bhejo
                    }
                    sendBroadcast(errorIntent)
                }
            } finally {
                if (!isCancelled) {
                    // Send scan finished signal (even if error)
                    val finishedIntent = Intent("VT_SCAN_FINISHED").apply {
                        putExtra("scanMode", scanMode) // ✅ Scan mode bhi bhejo
                    }
                    sendBroadcast(finishedIntent)
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }
    private suspend fun scanFiles(files: List<File>) {
        for ((index, file) in files.withIndex()) {
            if (isCancelled) break

            // Calculate progress
            val progress = (index * 100 / files.size)

            // Update notification
            updateNotification(progress, "Scanning ${file.name}")

            // Send progress update
            val progressIntent = Intent("VT_SCAN_PROGRESS").apply {
                putExtra("progress", progress)
                putExtra("currentFile", index + 1)
                putExtra("totalFiles", files.size)
                putExtra("fileName", file.name)
            }
            sendBroadcast(progressIntent)

            try {
                // Scan the file
                val result = virusTotalRepo.scanFile(file)
                results.add(result)

                // Update progress after successful scan
                val newProgress = ((index + 1) * 100 / files.size)
                updateNotification(newProgress, "Scanned ${file.name}")

                // Send progress update
                val newProgressIntent = Intent("VT_SCAN_PROGRESS").apply {
                    putExtra("progress", newProgress)
                    putExtra("currentFile", index + 1)
                    putExtra("totalFiles", files.size)
                    putExtra("fileName", file.name)
                }
                sendBroadcast(newProgressIntent)
            } catch (e: Exception) {
                Log.e("VirusTotalScanService", "Error scanning ${file.name}: ${e.message}")
                // Send error for this file but continue with others
                val errorIntent = Intent("VT_FILE_SCAN_ERROR").apply {
                    putExtra("fileName", file.name)
                    putExtra("error", e.message ?: "Unknown error")
                }
                sendBroadcast(errorIntent)
            }
        }
    }

    private fun createNotification(progress: Int, text: String): Notification {
        val cancelIntent = Intent("CANCEL_VT_SCAN")
        val cancelPending = PendingIntent.getBroadcast(
            this, 0, cancelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("VirusTotal Scan")
            .setContentText("$text - $progress%")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setProgress(100, progress, false)
            .addAction(android.R.drawable.ic_delete, "Cancel", cancelPending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(progress: Int, text: String) {
        val notification = createNotification(progress, text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
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