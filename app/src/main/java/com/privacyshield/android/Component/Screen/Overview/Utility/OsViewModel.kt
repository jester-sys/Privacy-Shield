package com.privacyshield.android.Component.Screen.Overview.Utility

// OsViewModel + OsTab + helpers
// Put these into appropriate files in your project.

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class OsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val _androidVersion = MutableStateFlow("")
    val androidVersion: StateFlow<String> = _androidVersion

    private val _sdk = MutableStateFlow("")
    val sdk: StateFlow<String> = _sdk

    private val _securityPatch = MutableStateFlow("")
    val securityPatch: StateFlow<String> = _securityPatch

    private val _securityPatchDays = MutableStateFlow<Long?>(null)
    val securityPatchDays: StateFlow<Long?> = _securityPatchDays

    private val _kernel = MutableStateFlow("")
    val kernel: StateFlow<String> = _kernel

    private val _bootloader = MutableStateFlow("")
    val bootloader: StateFlow<String> = _bootloader

    private val _fingerprint = MutableStateFlow("")
    val fingerprint: StateFlow<String> = _fingerprint

    private val _buildId = MutableStateFlow("")
    val buildId: StateFlow<String> = _buildId

    private val _selinuxEnforced = MutableStateFlow<Boolean?>(null)
    val selinuxEnforced: StateFlow<Boolean?> = _selinuxEnforced

    private val _uptime = MutableStateFlow("")
    val uptime: StateFlow<String> = _uptime

    private val _timezone = MutableStateFlow("")
    val timezone: StateFlow<String> = _timezone

    private val _locale = MutableStateFlow("")
    val locale: StateFlow<String> = _locale

    private val _isRooted = MutableStateFlow<Boolean?>(null)
    val isRooted: StateFlow<Boolean?> = _isRooted

    private val _hasGms = MutableStateFlow<Boolean?>(null)
    val hasGms: StateFlow<Boolean?> = _hasGms

    init {
        refresh()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            loadBasic()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadBasic() {
        try {
            _androidVersion.value = Build.VERSION.RELEASE ?: "Unknown"
            _sdk.value = (Build.VERSION.SDK_INT).toString()
            _securityPatch.value = Build.VERSION.SECURITY_PATCH ?: "Unknown"

            // security patch age (days)
            val patch = Build.VERSION.SECURITY_PATCH
            _securityPatchDays.value = try {
                if (!patch.isNullOrBlank()) {
                    val fmt = DateTimeFormatter.ISO_DATE
                    val patchDate = LocalDate.parse(patch, fmt)
                    ChronoUnit.DAYS.between(patchDate, LocalDate.now())
                } else null
            } catch (_: Exception) { null }

            // kernel
            _kernel.value = try {
                File("/proc/version").readText().trim()
            } catch (e: Exception) {
                "Unavailable: ${e.localizedMessage}"
            }

            _bootloader.value = Build.BOOTLOADER ?: "Unknown"
            _fingerprint.value = Build.FINGERPRINT ?: "Unknown"
            _buildId.value = Build.ID ?: "Unknown"

            _selinuxEnforced.value = try {
                val clazz = Class.forName("android.os.SELinux")
                val isEnabled = clazz.getMethod("isSELinuxEnabled").invoke(null) as? Boolean ?: false
                val isEnforced = clazz.getMethod("isSELinuxEnforced").invoke(null) as? Boolean ?: false
                isEnabled || isEnforced
            } catch (e: Throwable) {
                null
            }




            // uptime
            val ms = SystemClock.elapsedRealtime()
            _uptime.value = formatUptime(ms)

            // timezone & locale
            _timezone.value = TimeZone.getDefault().id + " (${TimeZone.getDefault().displayName})"
            _locale.value = Locale.getDefault().toLanguageTag() + " — " + Locale.getDefault().displayName

            // root detection (simple checks)
            _isRooted.value = simpleRootCheck()

            // Google Play Services presence
            _hasGms.value = try {
                ctx.packageManager.getPackageInfo("com.google.android.gms", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            } catch (_: Exception) {
                null
            }
        } catch (t: Throwable) {
            // swallow; set defaults so UI can still render
            t.printStackTrace()
        }
    }

    private fun simpleRootCheck(): Boolean {
        // check common su locations + `which su`
        val suPaths = listOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/su/bin/su"
        )
        try {
            if (suPaths.any { File(it).exists() }) return true
        } catch (_: Exception) { }

        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val out = proc.inputStream.bufferedReader().readText().trim()
            out.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    private fun formatUptime(ms: Long): String {
        var s = ms / 1000
        val days = s / (24 * 3600); s %= 24 * 3600
        val hours = s / 3600; s %= 3600
        val mins = s / 60; val secs = s % 60
        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (mins > 0) append("${mins}m ")
            append("${secs}s")
        }
    }
}
