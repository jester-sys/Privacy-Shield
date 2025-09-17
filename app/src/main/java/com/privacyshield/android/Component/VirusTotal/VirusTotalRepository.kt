package com.privacyshield.android.Component.VirusTotal


import android.util.Log
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class VirusTotalRepository @Inject constructor(private val api: VirusTotalApi) {
    suspend fun scanFile(file: File): VirusTotalResult {
        try {
            val uploadResp = api.uploadFile(file)
            var reportJson: String

            // Wait for report to be ready (with timeout)
            var attempts = 0
            val maxAttempts = 30 // 60 seconds timeout (30 * 2 seconds)

            do {
                delay(2000) // wait 2 seconds
                reportJson = api.getReport(uploadResp.id)
                attempts++
            } while (!api.isReportReady(reportJson) && attempts < maxAttempts)

            if (attempts >= maxAttempts) {
                throw Exception("Scan timeout - report not ready after 60 seconds")
            }

            // Parse the report
            val json = JSONObject(reportJson)
            val data = json.getJSONObject("data")
            val attributes = data.getJSONObject("attributes")
            val stats = attributes.getJSONObject("stats")

            return VirusTotalResult(
                fileName = file.name,
                malicious = stats.optInt("malicious", 0),
                harmless = stats.optInt("harmless", 0),
                suspicious = stats.optInt("suspicious", 0),
                undetected = stats.optInt("undetected", 0),
                timeout = stats.optInt("timeout", 0),
                lastAnalysisDate = attributes.optLong("last_analysis_date", System.currentTimeMillis() / 1000),
                rawJson = reportJson
            )
        } catch (e: Exception) {
            Log.e("VirusTotalRepository", "Error scanning file: ${e.message}")
            throw e
        }
    }
}