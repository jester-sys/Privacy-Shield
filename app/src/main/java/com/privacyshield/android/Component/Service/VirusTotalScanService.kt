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
import kotlinx.coroutines.ensureActive
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.privacyshield.android.Component.Scanner.QuickScan.CleanerViewModel
import com.privacyshield.android.Component.VirusTotal.VirusTotalManager
import com.privacyshield.android.Component.VirusTotal.VirusTotalRepository
import com.privacyshield.android.Component.VirusTotal.VirusTotalResult
import com.privacyshield.android.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@AndroidEntryPoint
class VirusTotalScanService : LifecycleService() {

    @Inject lateinit var virusTotalRepo: VirusTotalRepository
    @Inject lateinit var virusTotalManager: VirusTotalManager

    private val notificationId = 1001
    private val channelId = "vt_scan_channel"
    private val isCancelled = AtomicBoolean(false)
    private val results = mutableListOf<VirusTotalResult>()
    private var scanJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Create initial notification immediately in onCreate
        val initialNotification = createNotification("Starting scan...", "Preparing files")
        startForeground(notificationId, initialNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(cancelReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }

    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Update notification immediately to show we're processing
        updateNotification("Processing", "Starting scan...")

        val paths = intent?.getStringArrayExtra("files") ?: emptyArray()
        val scanMode = intent?.getStringExtra("scanMode") ?: "SINGLE"
        val source = intent?.getStringExtra("source") ?: "unknown"
        val files = paths.map { File(it) }.filter { it.exists() && it.isFile && it.length() <= 32 * 1024 * 1024 }

        if (files.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Register cancel receiver
        val filter = IntentFilter("CANCEL_VT_SCAN")
        try {
            registerReceiver(cancelReceiver, filter, RECEIVER_EXPORTED)
        } catch (e: Exception) {
            // Ignore if already registered
        }

        // Start scanning in background - SINGLE COROUTINE JOB
        scanJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                if (scanMode == "BATCH") {
                    virusTotalManager.scanFilesWithVirusTotal(
                        files.toSet(),
                        applicationContext,
                        { progress, status ->
                            val progressIntent = Intent("VT_SCAN_PROGRESS").apply {
                                putExtra("progress", progress)
                                putExtra("currentFile", status)
                                putExtra("totalFiles", files.size)
                                putExtra("scanMode", scanMode)
                            }
                            sendBroadcast(progressIntent)
                            updateNotification(status, "Scanning...")
                        },
                        { isCancelled.get() } // Cancellation check
                    )
                } else {
                    scanFiles(files)
                }

                // Save results only if not cancelled
                if (!isCancelled.get() && results.isNotEmpty()) {
                    virusTotalManager.saveResultsToDownloads(results, applicationContext)

                    // Show completion notification
                    val completedNotification = createCompletionNotification()
                    withContext(Dispatchers.Main) {
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.notify(notificationId + 1, completedNotification)
                    }

                    // Send completion broadcast
                    val completeIntent = Intent("VT_SCAN_COMPLETE").apply {
                        putExtra("files", paths)
                        putExtra("scanMode", scanMode)

                    }
                    sendBroadcast(completeIntent)
                }
            } catch (e: CancellationException) {
                Log.d("VirusTotalScanService", "Scan was cancelled")
                // Show cancelled notification
                showCancelledNotification()
            } catch (e: Exception) {
                if (!isCancelled.get()) {
                    Log.e("VirusTotalScanService", "Error: ${e.message}")
                    val errorIntent = Intent("VT_SCAN_ERROR").apply {
                        putExtra("error", e.message ?: "Unknown error")
                        putExtra("scanMode", scanMode)
                    }
                    sendBroadcast(errorIntent)
                } else {
                    // Show cancelled notification if cancelled during error
                    showCancelledNotification()
                }
            } finally {
                // Always send finished signal
                val finishedIntent = Intent("VT_SCAN_FINISHED").apply {
                    putExtra("scanMode", scanMode)
                }
                sendBroadcast(finishedIntent)

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    // Add this new function to show cancelled notification
    private fun showCancelledNotification() {
        // First cancel the original notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        // Then show the cancelled notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Scan Cancelled")
            .setContentText("VirusTotal scan was cancelled by user")
            .setSmallIcon(android.R.drawable.ic_delete) // Changed icon for cancellation
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId + 2, notification)
    }

    // Also update the cancel receiver to show cancelled notification
    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "CANCEL_VT_SCAN") {
                Log.d("VirusTotalScanService", "Cancel broadcast received")
                isCancelled.set(true)
                scanJob?.cancel()

                // Show cancelled notification immediately
                showCancelledNotification()

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                sendBroadcast(Intent("VT_SCAN_CANCELLED"))
            }
        }
    }

    private fun createCompletionNotification(): Notification {
        // Create intent to open download screen
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigateTo", "downloads")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("VirusTotal Scan Completed")
            .setContentText("Tap to view results")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set click action
            .build()
    }

    private suspend fun scanFiles(files: List<File>) {
        for ((index, file) in files.withIndex()) {
            // Check for cancellation
            if (isCancelled.get()) {
                break
            }

            // Update notification with file name and "Scanning..." text
            updateNotification(file.name, "Scanning...")

            // Send progress update
            val progressIntent = Intent("VT_SCAN_PROGRESS").apply {
                putExtra("progress", (index * 100 / files.size))
                putExtra("currentFile", index + 1)
                putExtra("totalFiles", files.size)
                putExtra("fileName", file.name)
            }
            sendBroadcast(progressIntent)

            try {
                // Scan the file
                val result = virusTotalRepo.scanFile(file)
                results.add(result)

                // Update notification after successful scan
                updateNotification(file.name, "Scan completed")

                // Send progress update
                val newProgressIntent = Intent("VT_SCAN_PROGRESS").apply {
                    putExtra("progress", ((index + 1) * 100 / files.size))
                    putExtra("currentFile", index + 1)
                    putExtra("totalFiles", files.size)
                    putExtra("fileName", file.name)
                }
                sendBroadcast(newProgressIntent)
            } catch (e: Exception) {
                Log.e("VirusTotalScanService", "Error scanning ${file.name}: ${e.message}")
                // Update notification with error
                updateNotification(file.name, "Scan failed: ${e.message ?: "Unknown error"}")

                val errorIntent = Intent("VT_FILE_SCAN_ERROR").apply {
                    putExtra("fileName", file.name)
                    putExtra("error", e.message ?: "Unknown error")
                }
                sendBroadcast(errorIntent)
            }
        }
    }

    private fun createNotification(title: String, message: String): Notification {
        // Notification with file name as title and status as content text
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val notification = createNotification(title, text)
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