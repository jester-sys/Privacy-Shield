package com.privacyshield.android.Component.Screen.Overview.Utility

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RamViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _totalRam = MutableStateFlow("")
    val totalRam: StateFlow<String> = _totalRam

    private val _availableRam = MutableStateFlow("")
    val availableRam: StateFlow<String> = _availableRam

    private val _usedRamPercent = MutableStateFlow(0f) // 0–100
    val usedRamPercent: StateFlow<Float> = _usedRamPercent

    private val _isLowMemory = MutableStateFlow(false)
    val isLowMemory: StateFlow<Boolean> = _isLowMemory

    private val _freeRam = MutableStateFlow("")
    val freeRam: StateFlow<String> = _freeRam

    private val _cachedRam = MutableStateFlow("")
    val cachedRam: StateFlow<String> = _cachedRam

    private val _swapTotal = MutableStateFlow("")
    val swapTotal: StateFlow<String> = _swapTotal

    private val _swapUsed = MutableStateFlow("")
    val swapUsed: StateFlow<String> = _swapUsed

    private val _processMemory = MutableStateFlow<Map<String, String>>(emptyMap())
    val processMemory: StateFlow<Map<String, String>> = _processMemory

    private val _explanation = MutableStateFlow("")
    val explanation: StateFlow<String> = _explanation

    private val activityManager = getApplication<Application>()
        .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    init {
        fetchRamInfo()
    }

    private fun fetchRamInfo() {
        // 📌 MemoryInfo
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val total = memInfo.totalMem / (1024 * 1024)
        val avail = memInfo.availMem / (1024 * 1024)
        val usedPercent = ((total - avail).toFloat() / total.toFloat()) * 100f

        _totalRam.value = "$total MB"
        _availableRam.value = "$avail MB"
        _usedRamPercent.value = usedPercent
        _isLowMemory.value = memInfo.lowMemory

        // 📌 Free vs Cached RAM (from /proc/meminfo)
        val memInfoMap = readProcMemInfo()
        _freeRam.value = "${memInfoMap["MemFree"] ?: 0} MB"
        _cachedRam.value = "${memInfoMap["Cached"] ?: 0} MB"

        // 📌 Swap info
        val swapTotalVal = memInfoMap["SwapTotal"] ?: 0
        val swapFreeVal = memInfoMap["SwapFree"] ?: 0
        _swapTotal.value = "$swapTotalVal MB"
        _swapUsed.value = "${swapTotalVal - swapFreeVal} MB"

        // 📌 Per-process memory usage
        _processMemory.value = getProcessMemoryInfo()

        // 📌 AI Explanation
        _explanation.value = generateAIExplanation(total, avail, usedPercent)
    }

    private fun readProcMemInfo(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        try {
            val file = File("/proc/meminfo")
            file.forEachLine { line ->
                val parts = line.split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val key = parts[0].removeSuffix(":")
                    val value = parts[1].toIntOrNull()?.div(1024) // kB -> MB
                    if (value != null) map[key] = value
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return map
    }

    private fun getProcessMemoryInfo(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val processes = activityManager.runningAppProcesses
        processes?.forEach { pInfo ->
            try {
                val pids = intArrayOf(pInfo.pid)
                val memInfos = activityManager.getProcessMemoryInfo(pids)
                val memMb = memInfos[0].totalPss / 1024 // KB -> MB
                map[pInfo.processName ?: "unknown"] = "$memMb MB"
            } catch (_: Exception) { }
        }
        return map
    }

    private fun generateAIExplanation(total: Long, avail: Long, usedPercent: Float): String {
        return when {
            usedPercent > 90 -> "High RAM usage detected! Consider closing background apps or clearing cache."
            usedPercent > 70 -> "⚠Moderate RAM usage. Some heavy apps may slow down your device."
            usedPercent > 40 -> "ℹRAM usage is normal."
            else -> "Low RAM usage. Device has plenty of free memory."
        }
    }
}
