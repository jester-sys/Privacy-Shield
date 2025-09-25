package com.privacyshield.android.Component.Screen.Overview.Utility.helper

import com.privacyshield.android.Component.Screen.Overview.Model.AndroidInfo
import com.privacyshield.android.Component.Screen.Overview.Model.CpuInfo
import com.privacyshield.android.Component.Screen.Overview.Model.GpuInfo
import com.privacyshield.android.Component.Screen.Overview.Model.HardwareInfo
import com.privacyshield.android.Component.Screen.Overview.Model.RamInfo
import com.privacyshield.android.Component.Screen.Overview.Model.SensorInfo
import com.privacyshield.android.Component.Screen.Overview.Model.StorageInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.ExplanationType
import com.privacyshield.android.Component.Screen.Overview.Utility.formatSize


fun buildInfoString(
    type: ExplanationType,
    cpuInfo: CpuInfo? = null,
    gpuInfo: GpuInfo? = null,
    storageInfo: StorageInfo? = null,
    androidInfo: AndroidInfo? = null,
    ramInfo: RamInfo? = null,
    hardwareInfo: HardwareInfo? = null,
    sensorsInfo: List<List<SensorInfo>>? = null
): String {
    return when (type) {
        ExplanationType.CPU -> buildString {
            appendLine("CPU ABI: ${cpuInfo?.abi ?: ""}")
            appendLine("CPU Cores: ${cpuInfo?.cores ?: ""}")
            appendLine("CPU Hardware: ${cpuInfo?.hardware ?: ""}")
            appendLine("CPU Manufacturer: ${cpuInfo?.manufacturer ?: ""}")
            appendLine("CPU Model: ${cpuInfo?.model ?: ""}")
            if (!cpuInfo?.coreDetails.isNullOrBlank()) {
                appendLine("\nCore Details:\n${cpuInfo?.coreDetails}")
            }
        }

        ExplanationType.GPU -> buildString {
            appendLine("Renderer: ${gpuInfo?.renderer ?: ""}")
            appendLine("Vendor: ${gpuInfo?.vendor ?: ""}")
            appendLine("OpenGL Version: ${gpuInfo?.version ?: ""}")
            appendLine("Extensions: ${gpuInfo?.extensions ?: ""}")
        }

        ExplanationType.STORAGE -> buildString {
            appendLine("Total Storage: ${storageInfo?.totalStorage ?: ""}")
            appendLine("Used Storage: ${storageInfo?.usedStorage ?: ""}")
            appendLine("Free Storage: ${storageInfo?.freeStorage ?: ""}")
            appendLine("System Storage: ${storageInfo?.systemStorage ?: ""}")
            appendLine("App Storage: ${storageInfo?.appStorage ?: ""}")
            appendLine("Cache Storage: ${storageInfo?.cacheStorage ?: ""}")
        }

        ExplanationType.ANDROID -> buildString {
            appendLine("Android Version: ${androidInfo?.androidVersion ?: ""}")
            appendLine("SDK: ${androidInfo?.sdk ?: ""}")
            appendLine("Security Patch: ${androidInfo?.securityPatch ?: ""}")
            appendLine("Days Since Last Security Patch: ${androidInfo?.securityDays ?: ""}")
            appendLine("Kernel Version: ${androidInfo?.kernel ?: ""}")
            appendLine("Bootloader: ${androidInfo?.bootloader ?: ""}")
            appendLine("Fingerprint: ${androidInfo?.fingerprint ?: ""}")
            appendLine("Build ID: ${androidInfo?.buildId ?: ""}")
            appendLine("SELinux: ${androidInfo?.selinux ?: ""}")
            appendLine("Uptime: ${androidInfo?.uptime ?: ""}")
            appendLine("Timezone: ${androidInfo?.timezone ?: ""}")
            appendLine("Locale: ${androidInfo?.locale ?: ""}")
            appendLine("Rooted Device: ${androidInfo?.isRooted ?: ""}")
            appendLine("Google Mobile Services Present: ${androidInfo?.hasGms ?: ""}")
        }

        ExplanationType.RAM -> buildString {
            appendLine("Total RAM: ${formatSize(ramInfo?.totalRam)}")
            appendLine("Available RAM: ${formatSize(ramInfo?.availableRam)}")
            appendLine("Free RAM: ${formatSize(ramInfo?.freeRam)}")
            appendLine("Cached RAM: ${formatSize(ramInfo?.cachedRam)}")
            appendLine("Swap Used: ${formatSize(ramInfo?.swapUsed)}")
            appendLine("Swap Total: ${formatSize(ramInfo?.swapTotal)}")
        }

        ExplanationType.HARDWARE -> buildString {
            appendLine("Manufacturer: ${hardwareInfo?.manufacturer ?: ""}")
            appendLine("Model: ${hardwareInfo?.model ?: ""}")
            appendLine("Board: ${hardwareInfo?.board ?: ""}")
            appendLine("Hardware: ${hardwareInfo?.hardware ?: ""}")
            appendLine("Device: ${hardwareInfo?.device ?: ""}")
            appendLine("Product: ${hardwareInfo?.product ?: ""}")
            appendLine("Brand: ${hardwareInfo?.brand ?: ""}")
            appendLine("Host: ${hardwareInfo?.host ?: ""}")
            appendLine("Build ID: ${hardwareInfo?.buildId ?: ""}")
            appendLine("Fingerprint: ${hardwareInfo?.fingerprint ?: ""}")
            appendLine("Serial Number: ${hardwareInfo?.serial ?: ""}")
            appendLine("Supported ABIs: ${hardwareInfo?.supportedAbis ?: ""}")
            appendLine("CPU ABI 1 Present: ${hardwareInfo?.cpuAbi ?: ""}")
            appendLine("CPU ABI 2 Present: ${hardwareInfo?.cpuAbi2 ?: ""}")
            appendLine("Max CPU Frequency: ${hardwareInfo?.maxFreq ?: ""}")
            appendLine("Min CPU Frequency: ${hardwareInfo?.minFreq ?: ""}")
            appendLine("Current CPU Frequency: ${hardwareInfo?.currentFreq ?: ""}")
            appendLine("GPU Info: ${hardwareInfo?.gpuInfo ?: ""}")
            appendLine("Thermal Info: ${hardwareInfo?.thermalInfo ?: ""}")
        }

        ExplanationType.SENSORS -> buildString {
            if (sensorsInfo.isNullOrEmpty()) {
                appendLine("No sensor data available.")
            } else {
                sensorsInfo.forEachIndexed { groupIndex, sensorGroup ->
                    appendLine("Sensor Group ${groupIndex + 1}:")
                    sensorGroup.forEach { sensor ->
                        appendLine("  Name: ${sensor.name}")
                        appendLine("  Type: ${sensor.type}")
                        appendLine("  Vendor: ${sensor.vendor}")
                        appendLine("  Max Range: ${sensor.maxRange}")
                        appendLine("  Resolution: ${sensor.resolution}")
                        appendLine("  Power: ${sensor.power} mA")
                        appendLine()
                    }
                }
            }
        }
    }
}
