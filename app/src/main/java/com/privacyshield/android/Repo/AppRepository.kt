package com.privacyshield.android.Repo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.provider.Settings
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.Model.AppPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getInstalledApps(): List<AppDetail> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        installedApps.mapNotNull { appsInfo ->
            try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(
                        appsInfo.packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(appsInfo.packageName, PackageManager.GET_PERMISSIONS)
                }

                // 👇 Yahan har permission ke liye AppPermission object banayenge
                val permissions = packageInfo.requestedPermissions?.mapNotNull { permName ->
                    try {
                        val permInfo = pm.getPermissionInfo(permName, 0)

                        val granted = context.checkPermission(
                            permInfo.name,
                            appsInfo.uid,
                            appsInfo.uid
                        ) == PackageManager.PERMISSION_GRANTED

                        val isDangerous =
                            permInfo.protectionLevel and PermissionInfo.PROTECTION_DANGEROUS != 0

                        val isDeclared = packageInfo.requestedPermissions?.contains(permInfo.name) == true
                        AppPermission(
                            name = permInfo.name,
                            isGranted = granted,
                            isDangerous = isDangerous,
                            isDeclared = isDeclared
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                // ⚡ custom checks
                val isSystemApp = (appsInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isOemApp = (appsInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val isSideloaded = (appsInfo.flags and ApplicationInfo.FLAG_INSTALLED) != 0 &&
                        (appsInfo.sourceDir?.startsWith("/data/app/") == true)


                val hasInternetPermission = packageInfo.requestedPermissions?.contains(
                    android.Manifest.permission.INTERNET
                ) ?: false

                val sharedUserId = packageInfo.sharedUserId
                val installerPackageName = pm.getInstallerPackageName(appsInfo.packageName)
                val isFromPlayStore = installerPackageName == "com.android.vending"
                val installSource = when {
                    isSystemApp -> "System App"
                    isFromPlayStore -> "Play Store"
                    else -> "APK / Unknown"
                }


                val isCloned = (appsInfo.packageName.contains(":")) // clone apps usually have colon suffix
                val isActiveProfile = android.os.Process.myUid() / 100000 == appsInfo.uid / 100000
                val isManagedProfile = (appsInfo.flags and ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA) != 0

                val hasCustomBatterySetting =
                    try {
                        Settings.Secure.getInt(
                            context.contentResolver,
                            "adaptive_battery_management_enabled_${appsInfo.packageName}"
                        ) == 1
                    } catch (e: Exception) {
                        false
                    }

                val isAccessibilityService =
                    packageInfo.services?.any { it.permission == android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE } == true

                AppDetail(
                    appName = pm.getApplicationLabel(appsInfo).toString(),
                    packageName = appsInfo.packageName,
                    icon = pm.getApplicationIcon(appsInfo),
                    versionName = packageInfo.versionName ?: "",
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
                    else @Suppress("DEPRECATION") packageInfo.versionCode.toLong(),
                    minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appsInfo.minSdkVersion else 0,
                    targetSdk = appsInfo.targetSdkVersion,
                    compileSdk = Build.VERSION.SDK_INT,
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime,
                    sourceDir = appsInfo.sourceDir,
                    isSystemApp = isSystemApp,
                    permissions = permissions,


                    isOemApp = isOemApp,
                    isSideloaded = isSideloaded,
                    hasInternetPermission = hasInternetPermission,
                    sharedUserId = sharedUserId,
                    isCloned = isCloned,
                    isActiveProfile = isActiveProfile,
                    isManagedProfile = isManagedProfile,
                    hasCustomBatterySetting = hasCustomBatterySetting,
                    isAccessibilityService = isAccessibilityService,
                    isFromPlayStore = isFromPlayStore,


                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
