package com.privacyshield.android.Component.MemoryManager.usecases

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import java.io.File

fun getStorageInfo(): Triple<Long, Long, Long> {
    val stat = StatFs(Environment.getDataDirectory().path)
    val blockSize = stat.blockSizeLong
    val total = stat.blockCountLong * blockSize
    val free = stat.availableBlocksLong * blockSize
    val used = total - free
    return Triple(total, used, free)
}
fun getInstalledAppsSize(context: Context): String {
    val pm = context.packageManager
    var totalSize = 0L
    val packages = pm.getInstalledPackages(0)

    for (pkg in packages) {
        try {
            val appInfo = pm.getApplicationInfo(pkg.packageName, 0)
            val file = File(appInfo.sourceDir)
            totalSize += file.length()
        } catch (_: Exception) {}
    }
    return formatSize(totalSize)
}
fun getMediaSize(context: Context, uri: Uri): Long {
    var totalSize = 0L
    val projection = arrayOf(MediaStore.MediaColumns.SIZE)

    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
        while (cursor.moveToNext()) {
            totalSize += cursor.getLong(sizeColumn)
        }
    }
    return totalSize
}

fun getImagesSize(context: Context): String {
    val sizeInBytes = getMediaSize(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    return formatSize(sizeInBytes)
}


fun getMusicSize(context: Context): String {
    val sizeInBytes = getMediaSize(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
    return formatSize(sizeInBytes)
}


fun getVideosSize(context: Context): String =
    formatSize(getMediaSize(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI))

fun getDocumentsSize(context: Context): String =
    formatSize(getMediaSize(context, MediaStore.Files.getContentUri("external")))



fun getDownloadsSize(): String {
    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    var totalSize = 0L
    downloads?.listFiles()?.forEach { file ->
        totalSize += if (file.isFile) file.length() else getFolderSize(file)
    }
    return formatSize(totalSize)
}

fun getFolderSize(dir: File): Long {
    var size: Long = 0
    dir.listFiles()?.forEach { file ->
        size += if (file.isFile) file.length() else getFolderSize(file)
    }
    return size
}
fun getSystemSize(): String {
    val system = File("/system")
    return formatSize(getFolderSize(system))
}
