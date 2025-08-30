package com.privacyshield.android.Component.Screen.Overview.Utility

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HardwareViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val _supportedAbis = MutableStateFlow("")
    val supportedAbis: StateFlow<String> = _supportedAbis

    private val _cpuAbi = MutableStateFlow("")
    val cpuAbi: StateFlow<String> = _cpuAbi

    private val _cpuAbi2 = MutableStateFlow("")
    val cpuAbi2: StateFlow<String> = _cpuAbi2

    private val _board = MutableStateFlow("")
    val board: StateFlow<String> = _board

    private val _hardware = MutableStateFlow("")
    val hardware: StateFlow<String> = _hardware

    private val _device = MutableStateFlow("")
    val device: StateFlow<String> = _device

    private val _product = MutableStateFlow("")
    val product: StateFlow<String> = _product

    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand

    private val _model = MutableStateFlow("")
    val model: StateFlow<String> = _model

    private val _manufacturer = MutableStateFlow("")
    val manufacturer: StateFlow<String> = _manufacturer

    private val _host = MutableStateFlow("")
    val host: StateFlow<String> = _host

    private val _buildId = MutableStateFlow("")
    val buildId: StateFlow<String> = _buildId

    private val _fingerprint = MutableStateFlow("")
    val fingerprint: StateFlow<String> = _fingerprint

    private val _serial = MutableStateFlow("")
    val serial: StateFlow<String> = _serial

    private val _cpuCores = MutableStateFlow(0)
    val cpuCores: StateFlow<Int> = _cpuCores

    private val _maxFreq = MutableStateFlow("")
    val maxFreq: StateFlow<String> = _maxFreq

    private val _minFreq = MutableStateFlow("")
    val minFreq: StateFlow<String> = _minFreq

    private val _currentFreq = MutableStateFlow("")
    val currentFreq: StateFlow<String> = _currentFreq

    private val _gpuInfo = MutableStateFlow("")
    val gpuInfo: StateFlow<String> = _gpuInfo

    private val _thermalInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val thermalInfo: StateFlow<Map<String, String>> = _thermalInfo

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            loadHardwareInfo()
        }
    }

    private fun loadHardwareInfo() {
        try {
            // Basic Build fields (defensive: null-safe & fallback)
            _supportedAbis.value = (Build.SUPPORTED_ABIS ?: emptyArray()).joinToString(", ")
            _cpuAbi.value = safeString(Build::class.java.getDeclaredField("CPU_ABI")?.get(null) as? String ?: Build.SUPPORTED_ABIS.firstOrNull().orEmpty())
            _cpuAbi2.value = safeString(Build::class.java.getDeclaredField("CPU_ABI2")?.get(null) as? String ?: "")
            _board.value = safeString(Build.BOARD)
            _hardware.value = safeString(Build.HARDWARE)
            _device.value = safeString(Build.DEVICE)
            _product.value = safeString(Build.PRODUCT)
            _brand.value = safeString(Build.BRAND)
            _model.value = safeString(Build.MODEL)
            _manufacturer.value = safeString(Build.MANUFACTURER)
            _host.value = safeString(Build.HOST)
            _buildId.value = safeString(Build.ID)
            _fingerprint.value = safeString(Build.FINGERPRINT)

            // Serial: use safe access because Build.SERIAL/getSerial may throw on newer Android without permission
            _serial.value = try {
                // prefer public API if available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Build.getSerial() ?: "Unknown"
                    } catch (se: SecurityException) {
                        // no permission; fallback to Build.SERIAL (may be "unknown")
                        safeString(Build.SERIAL)
                    }
                } else {
                    safeString(Build.SERIAL)
                }
            } catch (_: Throwable) {
                "Unavailable"
            }

            // CPU cores
            _cpuCores.value = Runtime.getRuntime().availableProcessors()

            // CPU frequencies (defensive read)
            _maxFreq.value = readCpuFileSafe("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            _minFreq.value = readCpuFileSafe("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq")
            _currentFreq.value = readCpuFileSafe("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")

            // GPU info — DO NOT call GLES directly (can crash). Try sysfs first, fallback to "Unavailable".
            _gpuInfo.value = readGpuInfoFromSysfs() ?: "Unavailable"

            // Thermal zones (for CPU/GPU temps, if exposed by kernel)
            _thermalInfo.value = readThermalInfo()

        } catch (t: Throwable) {
            // Always catch everything to avoid process-level crash in background coroutine
            t.printStackTrace()
        }
    }

    // Helpers

    private fun safeString(s: String?): String = s?.takeIf { it.isNotBlank() } ?: "Unknown"

    private fun readCpuFileSafe(path: String): String {
        return try {
            val text = File(path).takeIf { it.exists() }?.readText()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                // text might be in kHz or Hz depending on platform; try best-effort parse
                val num = text.toLongOrNull() ?: text.toIntOrNull()?.toLong()
                if (num != null) {
                    // heuristics: many kernels expose kHz (e.g., "1800000"), convert to MHz
                    val mhz = if (num > 10000) num / 1000 else num
                    "${mhz} MHz"
                } else {
                    text
                }
            } else "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }

    /**
     * Try reading GPU model from common sysfs paths. Avoid any GLES calls to prevent native crashes.
     */
    private fun readGpuInfoFromSysfs(): String? {
        val candidates = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_model",   // Adreno on many Qualcomm devices
            "/sys/class/kgsl/kgsl-3d0/gpu_version",
            "/sys/class/drm/card0/device/driver",   // possible GPU driver path
            "/proc/driver/nvidia/gpus/0/information" // rare
        )
        try {
            for (p in candidates) {
                val f = File(p)
                if (f.exists()) {
                    val text = f.readText().trim()
                    if (text.isNotEmpty()) return text
                }
            }
        } catch (_: Exception) { /* ignore */ }
        return null
    }

    private fun readThermalInfo(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            val thermalDir = File("/sys/class/thermal/")
            thermalDir.listFiles()?.forEach { zone ->
                try {
                    val typeFile = File(zone, "type")
                    val tempFile = File(zone, "temp")
                    if (!typeFile.exists()) return@forEach
                    val type = typeFile.readText().trim()
                    if (tempFile.exists()) {
                        val raw = tempFile.readText().trim()
                        val temp = raw.toIntOrNull()
                        if (temp != null) {
                            // usually millidegree → convert to °C
                            result[type] = "${temp / 1000.0} °C"
                        }
                    }
                } catch (_: Exception) { /* ignore zone */ }
            }
        } catch (_: Exception) { /* ignore */ }
        return result
    }
}
