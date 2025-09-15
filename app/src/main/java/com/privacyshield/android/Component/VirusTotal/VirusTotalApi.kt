package com.privacyshield.android.Component.VirusTotal

import java.io.File

interface VirusTotalApi {
    suspend fun uploadFile(file: File): VirusTotalUploadResponse
    suspend fun getReport(resourceId: String): String
    fun isReportReady(report: String): Boolean
}

data class VirusTotalUploadResponse(
    val id: String // resource id from VirusTotal
)
