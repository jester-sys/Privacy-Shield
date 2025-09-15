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
    val summary: String
        get() = buildString {
            appendLine("File: $fileName")
            appendLine("Malicious: $malicious")
            appendLine("Harmless: $harmless")
            appendLine("Suspicious: $suspicious")
            appendLine("Undetected: $undetected")
            appendLine("Timeout: $timeout")
            lastAnalysisDate?.let {
                val formatted = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(it * 1000))
                appendLine("Last Analysis: $formatted")
            }
            appendLine("\n--- RAW JSON ---\n")
            appendLine(rawJson)
        }
}
