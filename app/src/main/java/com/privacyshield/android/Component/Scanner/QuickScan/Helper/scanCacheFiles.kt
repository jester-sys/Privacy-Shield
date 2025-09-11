package com.privacyshield.android.Component.Scanner.QuickScan.Helper

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.File

fun scanCacheFiles(context: Context): Long {
    var total = 0L
    val cacheDir = context.cacheDir
    cacheDir?.listFiles()?.forEach { total += it.length() }
    return total
}

fun scanJunkFiles(context: Context): Long {
    var total = 0L
    val dirs = listOf(
        context.externalCacheDir,
        File(context.filesDir, "temp"),
        File("/storage/emulated/0/Android/data")
    )
    dirs.forEach { dir ->
        dir?.listFiles()?.forEach { file ->
            if (file.isDirectory && file.listFiles()?.isEmpty() == true) {
                total += file.length()
            }
        }
    }
    return total
}

fun scanLargeFiles(context: Context, minSize: Long): List<File> {
    val largeFiles = mutableListOf<File>()
    val storage = Environment.getExternalStorageDirectory()
    storage.walk().forEach { file ->
        if (file.isFile && file.length() >= minSize) {
            largeFiles.add(file)
        }
    }
    return largeFiles
}

fun scanDuplicateMedia(context: Context): Int {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val cursor = context.contentResolver.query(
        uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null
    )
    val map = mutableMapOf<Long, String>()
    var count = 0
    cursor?.use {
        while (it.moveToNext()) {
            val path = it.getString(0)
            val size = File(path).length()
            if (map.containsKey(size)) count++ else map[size] = path
        }
    }
    return count
}
