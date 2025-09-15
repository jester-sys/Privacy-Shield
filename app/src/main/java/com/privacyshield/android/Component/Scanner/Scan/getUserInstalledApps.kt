package com.privacyshield.android.Component.Scanner.Scan

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Environment

data class AppInfo(val name: String, val icon: Drawable)

fun getUserInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { app -> (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map {
            AppInfo(
                name = it.loadLabel(pm).toString(),
                icon = it.loadIcon(pm)
            )
        }
}

data class FileInfo(val name: String, val path: String)

fun getUserFiles(): List<FileInfo> {
    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    return downloads.listFiles()?.filter { it.isFile }?.map { FileInfo(it.name, it.absolutePath) } ?: emptyList()
}
