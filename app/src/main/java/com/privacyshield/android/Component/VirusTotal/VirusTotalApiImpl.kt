package com.privacyshield.android.Component.VirusTotal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class VirusTotalApiImpl(private val apiKey: String) : VirusTotalApi {

    private val client = OkHttpClient()

    override suspend fun uploadFile(file: File): VirusTotalUploadResponse = withContext(Dispatchers.IO) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody())
            .build()

        val request = Request.Builder()
            .url("https://www.virustotal.com/api/v3/files")
            .post(requestBody)
            .header("x-apikey", apiKey)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw IOException("Empty response")
            val json = JSONObject(body)
            val id = json.getJSONObject("data").getString("id")
            VirusTotalUploadResponse(id)
        }
    }

    override suspend fun getReport(resourceId: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://www.virustotal.com/api/v3/analyses/$resourceId")
            .get()
            .header("x-apikey", apiKey)
            .build()

        client.newCall(request).execute().use { response ->
            response.body?.string() ?: ""
        }
    }

    override fun isReportReady(report: String): Boolean {
        val json = JSONObject(report)
        val data = json.optJSONObject("data") ?: return false
        val attributes = data.optJSONObject("attributes") ?: return false
        val status = attributes.optString("status", "")
        return status == "completed"
    }

}
