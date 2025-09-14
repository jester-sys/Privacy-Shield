package com.privacyshield.android.Component.Settings.AppSecurity

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.os.Build
import com.privacyshield.android.Component.Settings.AppScanResult
import java.io.FileInputStream
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun computeSha256OfFile(path: String): String? = withContext(Dispatchers.IO) {
    try {
        val buffer = ByteArray(8 * 1024)
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(path).use { fis ->
            var read = fis.read(buffer)
            while (read > 0) {
                digest.update(buffer, 0, read)
                read = fis.read(buffer)
            }
        }
        return@withContext digest.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}

suspend fun scanInstalledApps(context: Context): List<AppScanResult> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNING_CERTIFICATES)

    val results = mutableListOf<AppScanResult>()
    for (pkg in packages) {
        val pkgName = pkg.packageName
        val appLabel = (pkg.applicationInfo?.loadLabel(pm) ?: pkgName).toString()
        val installer = try { pm.getInstallerPackageName(pkgName) } catch(_: Exception) { null }
        val isSystem = (pkg.applicationInfo!!.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        val isDebuggable = (pkg.applicationInfo!!.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // dangerous permissions requested
        val dangerousPerms = pkg.requestedPermissions
            ?.filter { perm ->
                try {
                    val permInfo = pm.getPermissionInfo(perm, 0)
                    // permission protection level >= dangerous
                    (permInfo.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_MASK_BASE) >= android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
                } catch (e: Exception) { false }
            }?.toList() ?: emptyList()

        // compute APK sha256
        val sourceDir = pkg.applicationInfo?.sourceDir
        val sha256 = sourceDir?.let { computeSha256OfFile(it) }

        // simple risk scoring heuristics
        val reasons = mutableListOf<String>()
        var score = 0
        if (isDebuggable) { score += 30; reasons += "App is debuggable" }
        if (!installer.isNullOrEmpty() && installer != "com.android.vending") {
            score += 10; reasons += "Installed from non-Play source: $installer"
        }
        if (dangerousPerms.isNotEmpty()) {
            score += (dangerousPerms.size * 5)
            reasons += "Requests dangerous permissions: ${dangerousPerms.joinToString { it.substringAfterLast('.') }}"
        }
        if (sha256 == null) { score += 10; reasons += "Could not compute APK hash" }
        if (isSystem) { score = (score * 40) / 100 } // system apps less likely to be risky (adjust as needed)

        // Cap score
        if (score > 100) score = 100

        results += AppScanResult(
            packageName = pkgName,
            appName = appLabel,
            installer = installer,
            isSystemApp = isSystem,
            isDebuggable = isDebuggable,
            dangerousPermissions = dangerousPerms,
            apkSha256 = sha256,
            riskScore = score,
            riskReasons = reasons
        )
    }
    return@withContext results
}
