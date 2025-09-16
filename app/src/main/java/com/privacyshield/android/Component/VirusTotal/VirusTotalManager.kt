package com.privacyshield.android.Component.VirusTotal

import android.content.Context
import android.os.Environment
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class VirusTotalManager @Inject constructor(
    private val virusTotalRepo: VirusTotalRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun scanFilesWithVirusTotal(files: Set<File>) {
        val results = mutableListOf<VirusTotalResult>()

        files.forEach { file ->
            val result = virusTotalRepo.scanFile(file)
            results.add(result)
        }

        saveResultsToDownloads(results)

        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "✅ Scan Completed! Check Downloads → VT_Scan_Results",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun saveResultsToDownloads(results: List<VirusTotalResult>) {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val scanFolder = File(downloads, "VT_Scan_Results").apply { mkdirs() }

        // Add timestamp to folder name
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sessionFolder = File(scanFolder, "Scan_$timestamp").apply { mkdirs() }

        results.forEach { r ->
            val fileOut = File(sessionFolder, "${r.fileName}_scan.html")

            // Parse JSON for vendor results & details
            val vendorRows = StringBuilder()
            val detailsRows = StringBuilder()

            try {
                val json = org.json.JSONObject(r.rawJson)
                val data = json.optJSONObject("data")
                val attributes = data?.optJSONObject("attributes")

                // Vendors
                val vendors = attributes?.optJSONObject("last_analysis_results")
                vendors?.keys()?.forEach { vendor ->
                    val obj = vendors.optJSONObject(vendor)
                    val result = obj?.optString("result", "N/A")
                    val style = when (result?.lowercase(Locale.getDefault())) {
                        "malicious" -> "malicious"
                        "suspicious" -> "suspicious"
                        "harmless" -> "harmless"
                        "undetected" -> "undetected"
                        else -> "other"
                    }
                    vendorRows.append("<tr><td>$vendor</td><td class='$style'>$result</td></tr>")
                }

                // Details
                detailsRows.append("<tr><td>MD5</td><td>${attributes?.optString("md5", "N/A")}</td></tr>")
                detailsRows.append("<tr><td>SHA-1</td><td>${attributes?.optString("sha1", "N/A")}</td></tr>")
                detailsRows.append("<tr><td>SHA-256</td><td>${attributes?.optString("sha256", "N/A")}</td></tr>")
                detailsRows.append("<tr><td>SSDEEP</td><td>${attributes?.optString("ssdeep", "N/A")}</td></tr>")
                detailsRows.append("<tr><td>TLSH</td><td>${attributes?.optString("tlsh", "N/A")}</td></tr>")
                detailsRows.append("<tr><td>File Type</td><td>${attributes?.optString("type_description", "N/A")}</td></tr>")
                detailsRows.append("<tr><td>Magic</td><td>${attributes?.optString("magic", "N/A")}</td></tr>")
                detailsRows.append("<tr><td>TrID</td><td>${attributes?.optJSONArray("trid")?.join(", ") ?: "N/A"}</td></tr>")
                detailsRows.append("<tr><td>Size</td><td>${attributes?.optLong("size", 0)} bytes</td></tr>")

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                detailsRows.append("<tr><td>First Submission</td><td>${
                    attributes?.optLong("first_submission_date")?.let { dateFormat.format(Date(it*1000)) } ?: "N/A"
                }</td></tr>")
                detailsRows.append("<tr><td>Last Submission</td><td>${
                    attributes?.optLong("last_submission_date")?.let { dateFormat.format(Date(it*1000)) } ?: "N/A"
                }</td></tr>")
                detailsRows.append("<tr><td>Last Analysis</td><td>${
                    attributes?.optLong("last_analysis_date")?.let { dateFormat.format(Date(it*1000)) } ?: "N/A"
                }</td></tr>")
            } catch (e: Exception) {
                vendorRows.append("<tr><td colspan='2'>Could not parse vendor results</td></tr>")
                detailsRows.append("<tr><td colspan='2'>Could not parse details</td></tr>")
            }

            val htmlContent = """
                <html>
                <head>
                    <title>Scan Report - ${r.fileName}</title>
                    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                    <style>
                        body { background: #0d1117; color: #c9d1d9; font-family: Arial, sans-serif; padding:24px; }
                        h1,h2 { color:#58a6ff; }
                        table { width:100%; border-collapse: collapse; margin:20px 0; }
                        th,td { padding:8px 12px; border-bottom: 1px solid #30363d; }
                        th { background:#161b22; text-align:left; }
                        tr:hover { background:#21262d; }
                        .malicious { color:#ff4c4c; font-weight:bold; }
                        .harmless { color:#4caf50; font-weight:bold; }
                        .suspicious { color:#ffc107; font-weight:bold; }
                        .undetected { color:#9e9e9e; font-weight:bold; }
                        .other { color:#00e5ff; }
                        .card { background:#161b22; border-radius:8px; padding:16px; margin-bottom:24px; box-shadow:0 0 10px rgba(0,0,0,0.5); }
                        canvas { max-width:400px; margin:20px auto; display:block; }
                    </style>
                </head>
                <body>
                    <h1>Scan Report: ${r.fileName}</h1>
                    <div class="card">
                        <h2>Summary</h2>
                        <p><b>Malicious:</b> ${r.malicious} | <b>Harmless:</b> ${r.harmless} | <b>Suspicious:</b> ${r.suspicious} | <b>Undetected:</b> ${r.undetected} | <b>Timeout:</b> ${r.timeout}</p>
                        <canvas id="summaryChart"></canvas>
                    </div>
                    
                    <div class="card">
                        <h2>Security vendors' analysis</h2>
                        <table>
                            <tr><th>Vendor</th><th>Result</th></tr>
                            $vendorRows
                        </table>
                    </div>
                    
                    <div class="card">
                        <h2>Details</h2>
                        <table>
                            $detailsRows
                        </table>
                    </div>
                    
                    <script>
                        new Chart(document.getElementById('summaryChart'), {
                          type:'doughnut',
                          data:{
                            labels:['Malicious','Harmless','Suspicious','Undetected','Timeout'],
                            datasets:[{
                              data:[${r.malicious},${r.harmless},${r.suspicious},${r.undetected},${r.timeout}],
                              backgroundColor:['#ff4c4c','#4caf50','#ffc107','#9e9e9e','#ff00ff']
                            }]
                          },
                          options:{responsive:true, plugins:{legend:{labels:{color:'#c9d1d9'}}}}
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

            fileOut.writeText(htmlContent)
        }
    }
}