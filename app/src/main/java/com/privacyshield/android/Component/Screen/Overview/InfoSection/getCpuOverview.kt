package com.privacyshield.android.Component.Screen.Overview.InfoSection

import android.os.Build
import com.privacyshield.android.Component.Screen.Overview.Utility.readCpuFile
import java.io.File

fun getCpuOverview(): Map<String, String> {
    val info = mutableMapOf<String, String>()

    info["CPU Model"] = Build.HARDWARE ?: "Unknown"
    info["CPU ABI"] = Build.SUPPORTED_ABIS.joinToString()
    info["CPU Cores"] = Runtime.getRuntime().availableProcessors().toString()
    info["CPU Arch"] = System.getProperty("os.arch") ?: "N/A"

    try {
        val cpuinfo = File("/proc/cpuinfo").readText()
        val lines = cpuinfo.split("\n")
        lines.forEach { line ->
            val parts = line.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                when {
                    key.contains("Hardware", true) -> info["Hardware"] = value
                    key.contains("Model name", true) -> info["Model Name"] = value
                    key.contains("Processor", true) -> info["Processor"] = value
                    key.contains("vendor_id", true) -> info["Vendor ID"] = value
                    key.contains("BogoMIPS", true) -> info["BogoMIPS"] = value
                    key.contains("Features", true) -> info["Features"] = value
                    key.contains("cpu family", true) -> info["CPU Family"] = value
                    key.contains("cache size", true) -> info["Cache Size"] = value
                }
            }
        }
    } catch (_: Exception) {}

    val governor = readCpuFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
    if (governor != "N/A") info["Governor"] = governor

    val thermal = readCpuFile("/sys/class/thermal/thermal_zone0/temp")
    if (thermal != "N/A") {
        try {
            val celsius = thermal.toFloat() / 1000f
            info["CPU Temperature"] = "%.1f °C".format(celsius)
        } catch (_: Exception) {}
    }

    return info
}
