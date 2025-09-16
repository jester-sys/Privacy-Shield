package com.privacyshield.android.Component.Scanner.QuickScan

import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.privacyshield.android.Component.Scanner.QuickScanResult
import com.privacyshield.android.Component.Service.VirusTotalScanService
import com.privacyshield.android.Component.VirusTotal.VirusTotalManager
import com.privacyshield.android.Component.VirusTotal.VirusTotalRepository
import com.privacyshield.android.Component.VirusTotal.VirusTotalResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import javax.inject.Inject

// ---------------------- ViewModel ----------------------
@HiltViewModel
class CleanerViewModel @Inject constructor(
    private val virusTotalRepo: VirusTotalRepository,
    private val virusTotalManager: VirusTotalManager,
    @ApplicationContext private val context: Context
) : ViewModel() {


    private var hasScanned = false



    private var scanJob: Job? = null

    // State flows
    private val _scanResult = MutableStateFlow<QuickScanResult?>(null)
    val scanResult: StateFlow<QuickScanResult?> = _scanResult

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress

    private val _totalFiles = MutableStateFlow(0)
    val totalFiles: StateFlow<Int> = _totalFiles

    private val _scannedFiles = MutableStateFlow(0)
    val scannedFiles: StateFlow<Int> = _scannedFiles

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _scanningState = MutableStateFlow(ScanningState.IDLE)
    val scanningState: StateFlow<ScanningState> = _scanningState

    private val _vtScanProgress = MutableStateFlow(0)
    val vtScanProgress: StateFlow<Int> = _vtScanProgress

    private val _vtScanDialog = MutableStateFlow(false)
    val vtScanDialog: StateFlow<Boolean> = _vtScanDialog

    private val _scanCompleted = MutableStateFlow(false)
    val scanCompleted: StateFlow<Boolean> = _scanCompleted

    private val _currentScanFile = MutableStateFlow<String?>(null)
    val currentScanFile: StateFlow<String?> = _currentScanFile

    // Add this new state for dialog visibility
    private val _showScanDialog = MutableStateFlow(false)
    val showScanDialog: StateFlow<Boolean> = _showScanDialog

    fun setShowScanDialog(show: Boolean) {
        _showScanDialog.value = show
    }
    fun updateCurrentScanFile(fileName: String) {
        _currentScanFile.value = fileName
    }


    fun startScan(files: List<File>, context: Context) {
        _totalFiles.value = files.size
        _scannedFiles.value = 0
        _scanCompleted.value = false
        _isScanning.value = true
        _showScanDialog.value = true
        _vtScanProgress.value = 0

        // Start background scan
        startBackgroundScan(files, context)
    }

    fun startBackgroundScan(files: List<File>, context: Context) {
        val serviceIntent = Intent(context, VirusTotalScanService::class.java).apply {
            putExtra("files", files.map { it.absolutePath }.toTypedArray())
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun updateVTProgress(progress: Int) {
        _vtScanProgress.value = progress
    }

    fun updateScanProgress(current: Int, total: Int) {
        _scannedFiles.value = current
        _totalFiles.value = total
    }

    fun markScanComplete() {
        _scanCompleted.value = true
        _isScanning.value = false
    }

    fun cancelScan() {
        _isScanning.value = false
        _showScanDialog.value = false
        // Broadcast to service to stop
        val intent = Intent("CANCEL_VT_SCAN")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun scanFilesWithVirusTotal(files: Set<File>) {
        viewModelScope.launch {
            _vtScanDialog.value = true
            val results = mutableListOf<VirusTotalResult>()

            files.forEachIndexed { index, file ->
                val result = virusTotalRepo.scanFile(file)
                results.add(result)
                _vtScanProgress.value = ((index + 1) * 100 / files.size)
            }

            val downloads =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val scanFolder = File(downloads, "VT_Scan_Results").apply { mkdirs() }

            results.forEach { r ->
                val fileOut = File(scanFolder, "${r.fileName}_scan.html")

                val htmlContent = """ 
                <html> 
                <head>
                    <title>Scan Report - ${r.fileName}</title>
                    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                    <style>
                        body {
                            background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
                            color: #ffffff;
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            padding: 24px;
                        }
                        h2 {
                            color: #3DDC84;
                            text-shadow: 1px 1px 4px rgba(0,0,0,0.6);
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 16px;
                            box-shadow: 0 4px 12px rgba(0,0,0,0.5);
                            border-radius: 8px;
                            overflow: hidden;
                        }
                        th, td {
                            padding: 10px;
                            text-align: left;
                        }
                        th {
                            background-color: #1E1E1E;
                            font-size: 14px;
                        }
                        td {
                            background-color: #2A2A2A;
                            font-size: 13px;
                        }
                        .malicious { color: #FF4C4C; font-weight: bold; }
                        .harmless { color: #4CAF50; font-weight: bold; }
                        .suspicious { color: #FFC107; font-weight: bold; }
                        .undetected { color: #9E9E9E; font-weight: bold; }
                        .timeout { color: #FF00FF; font-weight: bold; }
                        canvas {
                            margin-top: 24px;
                            max-width: 300px;
                            max-height: 300px;
                            display: block;
                            margin-left: auto;
                            margin-right: auto;
                        }
                        p {
                            font-size: 14px;
                            color: #CCCCCC;
                        }
                    </style>
                </head>
                <body>
                    <h2>Scan Report: ${r.fileName}</h2>
                    <p>Scan Date: ${
                    SimpleDateFormat(
                        "dd-MM-yyyy HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                }</p>
                    
                    <table>
                        <tr><th>Result Type</th><th>Count</th></tr>
                        <tr><td class="malicious">Malicious</td><td>${r.malicious}</td></tr>
                        <tr><td class="harmless">Harmless</td><td>${r.harmless}</td></tr>
                        <tr><td class="suspicious">Suspicious</td><td>${r.suspicious}</td></tr>
                        <tr><td class="undetected">Undetected</td><td>${r.undetected}</td></tr>
                        <tr><td class="timeout">Timeout</td><td>${r.timeout}</td></tr>
                    </table>

                    <canvas id="vtChart"></canvas>

                    <script>
                        const ctx = document.getElementById('vtChart').getContext('2d');
                        new Chart(ctx, {
                            type: 'pie',
                            data: {
                                labels: ['Malicious', 'Harmless', 'Suspicious', 'Undetected', 'Timeout'],
                                datasets: [{
                                    data: [${r.malicious}, ${r.harmless}, ${r.suspicious}, ${r.undetected}, ${r.timeout}],
                                    backgroundColor: [
                                        '#FF4C4C',
                                        '#4CAF50',
                                        '#FFC107',
                                        '#9E9E9E',
                                        '#FF00FF'
                                    ],
                                    borderWidth: 2,
                                    borderColor: '#121212'
                                }]
                            },
                            options: {
                                responsive: true,
                                plugins: {
                                    legend: { position: 'bottom', labels: { color: '#ffffff', font: { size: 12 } } },
                                    tooltip: { bodyFont: { size: 12 } }
                                },
                                layout: {
                                    padding: 16
                                }
                            }
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

                fileOut.writeText(htmlContent)
            }

            _vtScanDialog.value = false
            Toast.makeText(
                context,
                "Scan Completed! Check DownloadScreen for results",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun cancelVirusTotalScan() {
        val intent = Intent("CANCEL_VT_SCAN")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        _vtScanDialog.value = false
    }


    fun quickScan() {
        if (hasScanned) return
        hasScanned = true

        _scanningState.value = ScanningState.SCANNING
        _scanProgress.value = 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cache = withContext(Dispatchers.IO) { scanCacheFiles(context).also { _scanProgress.value = 10 } }
                val junk = withContext(Dispatchers.IO) { scanJunkFiles(context).also { _scanProgress.value = 20 } }
                val large = withContext(Dispatchers.IO) { scanLargeFiles(context, 100 * 1024 * 1024).also { _scanProgress.value = 40 } }
                val duplicates = withContext(Dispatchers.IO) { scanDuplicateMedia(context).also { _scanProgress.value = 50 } }
                val apks = withContext(Dispatchers.IO) { scanApkFiles(context).also { _scanProgress.value = 60 } }
                val emptyFolders = withContext(Dispatchers.IO) { scanEmptyFolders(context).also { _scanProgress.value = 65 } }
                val downloads = withContext(Dispatchers.IO) { scanDownloadFiles(context).also { _scanProgress.value = 70 } }
                val whatsappMedia = withContext(Dispatchers.IO) { scanWhatsAppTelegramMedia(context).also { _scanProgress.value = 80 } }
                val screenshots = withContext(Dispatchers.IO) { scanScreenshots(context).also { _scanProgress.value = 90 } }
                val photos = withContext(Dispatchers.IO) { scanPhotos(context).also { _scanProgress.value = 95 } }
                val videos = withContext(Dispatchers.IO) { scanVideos(context).also { _scanProgress.value = 100 } }

                _scanResult.value = QuickScanResult(
                    cacheSize = cache,
                    junkSize = junk,
                    largeFiles = large,
                    duplicateFiles = duplicates,
                    apkFiles = apks,
                    emptyFolders = emptyFolders,
                    downloadFiles = downloads,
                    screenshotFiles = screenshots,
                    videoFiles = videos,
                    photoFiles = photos
                )
                _scanningState.value = ScanningState.COMPLETED

            } catch (e: Exception) {
                _scanningState.value = ScanningState.ERROR
                e.printStackTrace()
            }
        }
    }

    fun clearResults() {
        _scanResult.value = null
        _scanProgress.value = 0
        _scanningState.value = ScanningState.IDLE
        hasScanned = false
    }

    // 🔹 Ye function ViewModel ke andar hi hona chahiye
    fun updateAfterDelete(deletedFiles: Set<File>, category: String?) {
        _scanResult.update { result ->
            result?.let {
                when (category) {
                    "Large Files" -> it.copy(largeFiles = it.largeFiles - deletedFiles)
                    "Duplicate Files" -> it.copy(duplicateFiles = it.duplicateFiles - deletedFiles)
                    "APK Files" -> it.copy(apkFiles = it.apkFiles - deletedFiles)
                    "Empty Folders" -> it.copy(emptyFolders = it.emptyFolders - deletedFiles)
                    "Downloads" -> it.copy(downloadFiles = it.downloadFiles - deletedFiles)
                    "Screenshots" -> it.copy(screenshotFiles = it.screenshotFiles - deletedFiles)
                    "Videos" -> it.copy(videoFiles = it.videoFiles - deletedFiles)
                    "Photos" -> it.copy(photoFiles = it.photoFiles - deletedFiles)
                    "Cache" -> it.copy(cacheSize = 0L)
                    "Junk" -> it.copy(junkSize = 0L)
                    else -> it
                }
            }
        }
    }
}

enum class ScanningState { IDLE, SCANNING, COMPLETED, ERROR }



// ---------------- Scan Functions ----------------

fun scanCacheFiles(context: Context): Long {
    var total = 0L
    listOfNotNull(context.cacheDir, context.externalCacheDir).forEach { dir ->
        total += getFolderSize(dir)
    }
    return total
}

fun scanJunkFiles(context: Context): Long {
    var total = 0L
    val dirs = listOf(
        context.cacheDir,
        context.externalCacheDir,
        File(context.filesDir, "temp"),
        File(Environment.getExternalStorageDirectory(), "Android/data"),
        File(Environment.getExternalStorageDirectory(), "Android/obb")
    )
    dirs.forEach { dir ->
        if (dir!!.exists()) dir.listFiles()?.forEach { file ->
            total += if (file.isFile) file.length() else 0L
        }
    }
    return total
}

fun scanLargeFiles(context: Context, minSize: Long): List<File> {
    val largeFiles = mutableListOf<File>()
    val storageDirs = listOfNotNull(
        Environment.getExternalStorageDirectory(),
        context.getExternalFilesDir(null)?.parentFile
    )
    storageDirs.forEach { storageDir ->
        storageDir.walkTopDown().forEach { file ->
            if (file.isFile && file.length() >= minSize) largeFiles.add(file)
        }
    }
    return largeFiles
}

fun scanDuplicateMedia(context: Context): List<File> {
    val duplicates = mutableListOf<File>()
    val seenSizes = mutableMapOf<Long, MutableList<File>>()

    context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE),
        "${MediaStore.Files.FileColumns.SIZE} > 0",
        null,
        null
    )?.use { cursor ->
        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        while (cursor.moveToNext()) {
            val path = cursor.getString(pathIndex)
            val size = cursor.getLong(sizeIndex)
            val file = File(path)
            if (file.exists() && file.isFile && file.canRead()) {
                seenSizes.getOrPut(size) { mutableListOf() }.add(file)
            }
        }
    }
    seenSizes.values.forEach { files ->
        if (files.size > 1) duplicates.addAll(files)
    }
    return duplicates
}

fun scanApkFiles(context: Context): List<File> {
    val apkFiles = mutableListOf<File>()
    Environment.getExternalStorageDirectory().walkTopDown().forEach {
        if (it.isFile && it.extension.equals("apk", true)) apkFiles.add(it)
    }
    return apkFiles
}

fun scanEmptyFolders(context: Context): List<File> {
    val emptyFolders = mutableListOf<File>()
    Environment.getExternalStorageDirectory().walkTopDown().forEach {
        if (it.isDirectory && (it.listFiles()?.isEmpty() == true)) emptyFolders.add(it)
    }
    return emptyFolders
}

fun scanDownloadFiles(context: Context): List<File> {
    val downloads = mutableListOf<File>()
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles()?.forEach {
        if (it.isFile) downloads.add(it)
    }
    return downloads
}

fun scanScreenshots(context: Context): List<File> {
    val screenshots = mutableListOf<File>()
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Images.Media.DISPLAY_NAME} LIKE '%Screenshot%'",
        null,
        null
    )?.use { cursor ->
        val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        while (cursor.moveToNext()) {
            val file = File(cursor.getString(index))
            if (file.exists()) screenshots.add(file)
        }
    }
    return screenshots
}

fun scanWhatsAppTelegramMedia(context: Context): List<File> {
    val media = mutableListOf<File>()
    val dirs = listOf(
        File(Environment.getExternalStorageDirectory(), "WhatsApp/Media"),
        File(Environment.getExternalStorageDirectory(), "Telegram")
    )
    dirs.forEach { dir ->
        if (dir.exists()) dir.walkTopDown().forEach { if (it.isFile) media.add(it) }
    }
    return media
}

fun scanPhotos(context: Context): List<File> {
    val photos = mutableListOf<File>()
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null
    )?.use { cursor ->
        val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        while (cursor.moveToNext()) {
            val file = File(cursor.getString(index))
            if (file.exists()) photos.add(file)
        }
    }
    return photos
}

fun scanVideos(context: Context): List<File> {
    val videos = mutableListOf<File>()
    context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        arrayOf(MediaStore.Video.Media.DATA),
        null,
        null,
        null
    )?.use { cursor ->
        val index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        while (cursor.moveToNext()) {
            val file = File(cursor.getString(index))
            if (file.exists()) videos.add(file)
        }
    }
    return videos
}

fun getFolderSize(file: File): Long {
    if (!file.exists()) return 0L
    return if (file.isFile) file.length() else file.listFiles()?.sumOf { getFolderSize(it) } ?: 0L
}




