package com.privacyshield.android.Repo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.privacyshield.android.Model.AppDetail
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getInstalledApps():List<AppDetail>{
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        return installedApps.mapNotNull { appsInfo ->
            try {
                val packageInfo = pm.getPackageInfo(appsInfo.packageName,0)
                AppDetail(
                    appName = pm.getApplicationLabel(appsInfo).toString(),
                    packageName = appsInfo.packageName,
                   icon = pm.getApplicationIcon(appsInfo),
                    versionName = packageInfo.versionName ?: "",
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
                    else packageInfo.versionCode.toLong(),
                    minSdk = appsInfo.minSdkVersion,
                    targetSdk = appsInfo.targetSdkVersion,
                    compileSdk = Build.VERSION.SDK_INT,
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime,
                    sourceDir = appsInfo.sourceDir,
                    isSystemApp = (appsInfo.flags and ApplicationInfo.FLAG_SYSTEM) !=0

                )
            }catch (e:Exception){null}

        }

    }

}