package com.privacyshield.android.Component.VirusTotal


import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class VirusTotalRepository @Inject constructor(private val api: VirusTotalApi) {

    suspend fun scanFile(file: File): VirusTotalResult {
        val uploadResp = api.uploadFile(file)
        var reportJson: String
        do {
            delay(2000) // wait 2 seconds
            reportJson = api.getReport(uploadResp.id)
        } while (!api.isReportReady(reportJson))

        // Parse JSON
        val json = JSONObject(reportJson)
        val attributes = json.getJSONObject("data").getJSONObject("attributes")
        val stats = attributes.getJSONObject("stats")

        val malicious = stats.optInt("malicious", 0)
        val harmless = stats.optInt("harmless", 0)
        val suspicious = stats.optInt("suspicious", 0)
        val undetected = stats.optInt("undetected", 0)
        val timeout = stats.optInt("timeout", 0)
        val lastAnalysisDate = attributes.optLong("last_analysis_date", System.currentTimeMillis() / 1000)

        return VirusTotalResult(
            fileName = file.name,
            malicious = malicious,
            harmless = harmless,
            suspicious = suspicious,
            undetected = undetected,
            timeout = timeout,
            lastAnalysisDate = lastAnalysisDate,
            rawJson = reportJson
        )
    }
}
