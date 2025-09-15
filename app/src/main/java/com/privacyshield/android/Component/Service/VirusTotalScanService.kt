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
    @Inject lateinit var virusTotalManager: VirusTotalManager // Cleaner logic moved here

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
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(cancelReceiver, IntentFilter("CANCEL_VT_SCAN"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(cancelReceiver)
    }

    @SuppressLint("MissingSuperCall")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val paths = intent?.getStringArrayExtra("files") ?: emptyArray()
        val files = paths.map { File(it) }.toSet()
        val mode = intent?.getStringExtra("mode") ?: "SCAN"

        updateNotification(0, if (mode=="SCAN") "Scanning..." else "Downloading...")

        if (files.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (mode=="SCAN") startScan(files)
                else virusTotalManager.scanFilesWithVirusTotal(files)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun startScan(files: Set<File>) {
        files.forEachIndexed { index, file ->
            if (isCancelled) return
            virusTotalRepo.scanFile(file)
            val progress = ((index+1)*100/files.size)
            updateNotification(progress,"Scanning files...")
        }
    }

    private fun updateNotification(progress: Int, text: String) {
        val cancelIntent = Intent("CANCEL_VT_SCAN")
        val cancelPending = PendingIntent.getBroadcast(this,0,cancelIntent,PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VirusTotal Scan")
            .setContentText("$text $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100,progress,false)
            .addAction(android.R.drawable.ic_delete,"Cancel",cancelPending)
            .setOngoing(true)

        startForeground(notificationId,builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,"VirusTotal Scans",NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
