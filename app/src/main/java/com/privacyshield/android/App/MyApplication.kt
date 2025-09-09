package com.privacyshield.android.App

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import com.privacyshield.android.Component.Scanner.unusgeApp.scheduleUnusedAppsWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set

        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Worker ko schedule karo
        if (hasUsageAccess(this)) {
            scheduleUnusedAppsWorker(this)
        }
    }

    private fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
