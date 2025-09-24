package com.privacyshield.android.Component.Screen.Overview.InfoSection

import com.privacyshield.android.Component.Screen.Overview.Model.CpuCoreInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.formatFreq
import com.privacyshield.android.Component.Screen.Overview.Utility.readCpuFile

fun getCpuCoresInfo(): List<CpuCoreInfo> {
    val cores = Runtime.getRuntime().availableProcessors()
    val list = mutableListOf<CpuCoreInfo>()

    for (i in 0 until cores) {
        val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq/"
        val minFreq = readCpuFile(basePath + "cpuinfo_min_freq")
        val maxFreq = readCpuFile(basePath + "cpuinfo_max_freq")
        val curFreq = readCpuFile(basePath + "scaling_cur_freq")

        val min = minFreq.toLongOrNull() ?: 0L
        val max = maxFreq.toLongOrNull() ?: 0L
        val cur = curFreq.toLongOrNull() ?: 0L

        val percent = if (max > 0) cur.toFloat() / max else 0f

        list.add(
            CpuCoreInfo(
                coreId = i,
                minFreq = formatFreq(minFreq),
                maxFreq = formatFreq(maxFreq),
                curFreq = formatFreq(curFreq),
                curPercent = percent.coerceIn(0f, 1f)
            )
        )
    }
    return list
}
