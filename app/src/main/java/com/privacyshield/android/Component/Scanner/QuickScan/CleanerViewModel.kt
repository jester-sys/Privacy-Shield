package com.privacyshield.android.Component.Scanner.QuickScan

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyshield.android.Component.Scanner.QuickScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.LinkedList
import javax.inject.Inject

// ---------------------- ViewModel ----------------------
@HiltViewModel
class CleanerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _scanResult = MutableStateFlow<QuickScanResult?>(null)
    val scanResult: StateFlow<QuickScanResult?> = _scanResult

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress
    private var hasScanned = false

    private val _scanningState = MutableStateFlow(ScanningState.IDLE)
    val scanningState: StateFlow<ScanningState> = _scanningState



    fun quickScan() {
        if (hasScanned) return  // prevent multiple scans
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
