package com.privacyshield.android.Component.Settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

data class VirusTotalResult(
    val sha256: String,
    val malicious: Int,
    val harmless: Int,
    val suspicious: Int,
    val undetected: Int,
    val timeout: Int,
    val lastAnalysisDateEpochSec: Long?, // nullable
    val rawJson: String
)

suspend fun queryVirusTotal(apiKey: String, sha256: String): VirusTotalResult? = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder().build()
    val url = "https://www.virustotal.com/api/v3/files/$sha256"

    val request = Request.Builder()
        .url(url)
        .header("x-apikey", apiKey.trim())
        .get()
        .build()

    try {
        client.newCall(request).execute().use { response ->
            val code = response.code
            val bodyString = response.body?.string()

            if (!response.isSuccessful) {
                println("❌ VirusTotal API error: code=$code, message=${response.message}")
                println("Response body: $bodyString")
                return@withContext null
            }

            if (bodyString.isNullOrEmpty()) {
                println("⚠️ VirusTotal API empty body for sha256=$sha256")
                return@withContext null
            }

            return@withContext try {
                val json = JSONObject(bodyString)
                val data = json.optJSONObject("data")
                val attributes = data?.optJSONObject("attributes")
                val stats = attributes?.optJSONObject("last_analysis_stats")

                VirusTotalResult(
                    sha256 = sha256,
                    malicious = stats?.optInt("malicious", 0) ?: 0,
                    harmless = stats?.optInt("harmless", 0) ?: 0,
                    suspicious = stats?.optInt("suspicious", 0) ?: 0,
                    undetected = stats?.optInt("undetected", 0) ?: 0,
                    timeout = stats?.optInt("timeout", 0) ?: 0,
                    lastAnalysisDateEpochSec = attributes?.optLong("last_analysis_date")?.takeIf { it > 0 },
                    rawJson = bodyString
                )
            } catch (e: Exception) {
                println("❌ JSON parsing error: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    } catch (e: IOException) {
        println("❌ Network error: ${e.message}")
        e.printStackTrace()
        null
    }
}
