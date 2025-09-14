package com.privacyshield.android.Component.Settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyshield.android.Component.Settings.AppSecurity.computeSha256OfFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PlayProtectStatus(
    val isEnabled: Boolean,
    val lastScanTime: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // primary aggregated security status
    private val _status = MutableStateFlow<SecurityStatus?>(null)
    val status: StateFlow<SecurityStatus?> = _status

    // Play Protect specific state
    private val _playProtectStatus = MutableStateFlow(
        PlayProtectStatus(isEnabled = false, lastScanTime = "Never")
    )
    val playProtectStatus: StateFlow<PlayProtectStatus> = _playProtectStatus

    // ✅ Scanning state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    init {
        viewModelScope.launch {
            val playProtectAvailable = isPlayProtectAvailable(context)
            val deviceFinder = isDeviceFinderAvailable(context)

            _status.value = SecurityStatus(
                systemUpdates = isSystemAutoUpdateOn(context),
                deviceUnlock = isDeviceSecure(context),
                playProtect = playProtectAvailable,
                cameraUsage = isPermissionAllowed(context, AppOpsManager.OPSTR_CAMERA),
                micUsage = isPermissionAllowed(context, AppOpsManager.OPSTR_RECORD_AUDIO),
                locationUsage = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED,
                usageInsights = hasUsageAccess(context),
                deviceFinderAvailable = deviceFinder
            )

            _playProtectStatus.value = PlayProtectStatus(
                isEnabled = playProtectAvailable,
                lastScanTime = if (playProtectAvailable) "Unknown" else "Unavailable"
            )
        }
    }

    /**
     * Simulate Play Protect scan
     */
    fun scanPlayProtect(apiKey: String) {
        viewModelScope.launch {
            _isScanning.value = true

            // ✅ APK ka path
            val apkPath = context.applicationInfo.sourceDir

            // ✅ SHA256 compute
            val sha256 = computeSha256OfFile(apkPath) ?: "Failed"

            // ✅ VirusTotal se result lana
            val result = queryVirusTotal(apiKey, sha256)

            val time = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            _playProtectStatus.value = PlayProtectStatus(
                isEnabled = _playProtectStatus.value.isEnabled,
                lastScanTime = buildString {
                    append(time)
                    append("\nSHA256: $sha256")
                    if (result != null) {
                        append("\nMalicious: ${result.malicious}")
                        append("\nSuspicious: ${result.suspicious}")
                        append("\nUndetected: ${result.undetected}")
                    } else {
                        append("\nScan: Failed / API Error")
                    }
                }
            )

            _isScanning.value = false
        }
    }

    // ---------------------- Helpers ----------------------

    fun isSystemAutoUpdateOn(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME, 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    fun isDeviceSecure(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    fun isPlayProtectAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.gms", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isDeviceFinderAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.gms", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("NewApi")
    fun isPermissionAllowed(context: Context, op: String): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(op, android.os.Process.myUid(), context.packageName)
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("WrongConstant")
    fun hasUsageAccess(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}
