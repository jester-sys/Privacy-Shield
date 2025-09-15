package com.privacyshield.android.Component.VirusTotal

import android.content.Context
import android.os.Environment
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
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

        files.forEachIndexed { index, file ->
            val result = virusTotalRepo.scanFile(file)
            results.add(result)
            // Progress can be broadcasted if needed
        }

        // Save HTML reports
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val scanFolder = File(downloads, "VT_Scan_Results").apply { mkdirs() }

        results.forEach { r ->
            val fileOut = File(scanFolder, "${r.fileName}_scan.html")

            val htmlContent = """
                <html> 
                <head>
                    <title>Scan Report - ${r.fileName}</title>
                    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                    <style>
                        body { background: #121212; color: #fff; font-family: sans-serif; padding:24px;}
                        h2 { color: #3DDC84;}
                        table { width:100%; border-collapse: collapse; margin-top:16px;}
                        th,td { padding:10px; text-align:left;}
                        th { background-color:#1E1E1E; }
                        td { background-color:#2A2A2A; }
                        .malicious{color:#FF4C4C;font-weight:bold;}
                        .harmless{color:#4CAF50;font-weight:bold;}
                        .suspicious{color:#FFC107;font-weight:bold;}
                        .undetected{color:#9E9E9E;font-weight:bold;}
                        .timeout{color:#FF00FF;font-weight:bold;}
                        canvas{margin-top:24px; max-width:300px; display:block; margin:auto;}
                    </style>
                </head>
                <body>
                    <h2>Scan Report: ${r.fileName}</h2>
                    <p>Scan Date: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(
                Date()
            )}</p>
                    <table>
                        <tr><th>Result Type</th><th>Count</th></tr>
                        <tr><td class="malicious">Malicious</td><td>${r.malicious}</td></tr>
                        <tr><td class="harmless">Harmless</td><td>${r.harmless}</td></tr>
                        <tr><td class="suspicious">Suspicious</td><td>${r.suspicious}</td></tr>
                        <tr><td class="undetected">Undetected</td><td>${r.undetected}</td></tr>
                        <tr><td class="timeout">Timeout</td><td>${r.timeout}</td></tr>
                    </table>
                    <canvas id="vtChart"></canvas>
                    <script>
                        const ctx = document.getElementById('vtChart').getContext('2d');
                        new Chart(ctx, {
                            type:'pie',
                            data:{
                                labels:['Malicious','Harmless','Suspicious','Undetected','Timeout'],
                                datasets:[{
                                    data:[${r.malicious},${r.harmless},${r.suspicious},${r.undetected},${r.timeout}],
                                    backgroundColor:['#FF4C4C','#4CAF50','#FFC107','#9E9E9E','#FF00FF'],
                                    borderWidth:2,borderColor:'#121212'
                                }]
                            },
                            options:{responsive:true}
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

            fileOut.writeText(htmlContent)
        }

        Toast.makeText(context, "Scan Completed! Check Downloads/VT_Scan_Results", Toast.LENGTH_LONG).show()
    }
}
