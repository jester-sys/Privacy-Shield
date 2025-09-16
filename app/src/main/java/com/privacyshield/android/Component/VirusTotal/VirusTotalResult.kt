package com.privacyshield.android.Component.VirusTotal

import java.util.Date
import java.util.Locale
data class VirusTotalResult(
    val fileName: String,
    val malicious: Int,
    val harmless: Int,
    val suspicious: Int,
    val undetected: Int,
    val timeout: Int,
    val lastAnalysisDate: Long?, // epoch seconds
    val rawJson: String
) {
    val reportString: String
        get() = buildString {
            // === Detection ===
            appendLine("=== Security vendors' analysis ===")
            try {
                val json = org.json.JSONObject(rawJson)
                val data = json.optJSONObject("data")
                val attributes = data?.optJSONObject("attributes")
                val results = attributes?.optJSONObject("last_analysis_results")

                results?.keys()?.forEach { vendor ->
                    val vendorObj = results.optJSONObject(vendor)
                    val result = vendorObj?.optString("result", "N/A")
                    appendLine("$vendor → $result")
                }
            } catch (e: Exception) {
                appendLine("Could not parse vendor results")
            }

            // === Details ===
            appendLine("\n=== Details ===")
            try {
                val json = org.json.JSONObject(rawJson)
                val data = json.optJSONObject("data")
                val attributes = data?.optJSONObject("attributes")

                appendLine("MD5: ${attributes?.optString("md5", "N/A")}")
                appendLine("SHA-1: ${attributes?.optString("sha1", "N/A")}")
                appendLine("SHA-256: ${attributes?.optString("sha256", "N/A")}")
                appendLine("SSDEEP: ${attributes?.optString("ssdeep", "N/A")}")
                appendLine("TLSH: ${attributes?.optString("tlsh", "N/A")}")
                appendLine("File type: ${attributes?.optString("type_description", "N/A")}")
                appendLine("Magic: ${attributes?.optString("magic", "N/A")}")
                appendLine("TrID: ${attributes?.optJSONArray("trid")?.join(", ") ?: "N/A"}")
                appendLine("Size: ${attributes?.optLong("size", 0)} bytes")

                appendLine("First Submission: ${attributes?.optLong("first_submission_date")?.let { ts ->
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts * 1000))
                } ?: "N/A"}")

                appendLine("Last Submission: ${attributes?.optLong("last_submission_date")?.let { ts ->
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts * 1000))
                } ?: "N/A"}")

                appendLine("Last Analysis: ${attributes?.optLong("last_analysis_date")?.let { ts ->
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts * 1000))
                } ?: "N/A"}")
            } catch (e: Exception) {
                appendLine("Could not parse details")
            }
        }
}
