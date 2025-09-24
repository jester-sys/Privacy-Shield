package com.privacyshield.android.Component.Screen.Overview.Utility

import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Model.AndroidInfo
import com.privacyshield.android.Component.Screen.Overview.Model.CpuInfo
import com.privacyshield.android.Component.Screen.Overview.Model.GpuInfo
import com.privacyshield.android.Component.Screen.Overview.Model.HardwareInfo
import com.privacyshield.android.Component.Screen.Overview.Model.RamInfo
import com.privacyshield.android.Component.Screen.Overview.Model.SensorInfo
import com.privacyshield.android.Component.Screen.Overview.Model.StorageInfo
import com.privacyshield.android.Component.Screen.Overview.Utility.helper.buildInfoString
import com.privacyshield.android.Component.Screen.Overview.Utility.helper.buildPrompt


suspend fun getDeviceExplanation(
    explanationType: ExplanationType,
    userQuestion: String? = null,
    // Common parameters
    cpuInfo: CpuInfo? = null,
    gpuInfo: GpuInfo? = null,
    storageInfo: StorageInfo? = null,
    androidInfo: AndroidInfo? = null,
    ramInfo: RamInfo? = null,
    hardwareInfo: HardwareInfo? = null,
    sensorsInfo: List<List<SensorInfo>>? = null
): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = com.privacyshield.android.BuildConfig.AI_KEY
    )

    val infoString = buildInfoString(explanationType, cpuInfo, gpuInfo, storageInfo, androidInfo, ramInfo, hardwareInfo, sensorsInfo)
    val prompt = buildPrompt(explanationType, infoString, userQuestion)

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "I couldn't generate an explanation at the moment. Please try again."
    } catch (e: Exception) {
        "Error getting explanation: ${e.localizedMessage}"
    }
}