package com.privacyshield.android.Component.Settings

import android.content.ContentUris
import android.os.Environment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.privacyshield.android.Component.Scanner.QuickScan.CleanerViewModel
import java.io.File
@Composable
fun DownloadScreen(context: Context = LocalContext.current) {
    val scanFolder = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "VT_Scan_Results"
    )
    val scanFiles = remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (scanFolder.exists()) {
            val files = scanFolder
                .listFiles()
                ?.filter { it.isFile }   // ✅ only files, no folders
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()

            Log.d("DownloadScreen", "Scan folder exists: ${scanFolder.absolutePath}, files=${files.size}")
            scanFiles.value = files
        } else {
            Log.d("DownloadScreen", "Scan folder does not exist: ${scanFolder.absolutePath}")
        }
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("VT Scan Results", fontSize = 20.sp, color = Color.White)
        Spacer(Modifier.height(12.dp))

        if (scanFiles.value.isEmpty()) {
            Text("No scan results found.", color = Color.Gray)
        } else {
            LazyColumn {
                items(scanFiles.value) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                try {
                                    Log.d("DownloadScreen", "Clicked file: ${file.absolutePath}")

                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    Log.d("DownloadScreen", "Generated URI: $uri")

                                    // ✅ Detect MIME type based on extension
                                    val mimeType = when {
                                        file.name.endsWith(".html", true) -> "text/html"
                                        file.name.endsWith(".txt", true) -> "text/plain"
                                        file.name.endsWith(".pdf", true) -> "application/pdf"
                                        else -> "text/plain"
                                    }
                                    Log.d("DownloadScreen", "Detected mimeType: $mimeType")

                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, mimeType)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        if (mimeType == "text/html") {
                                            addCategory(Intent.CATEGORY_BROWSABLE)
                                        }
                                    }

                                    // ✅ Check if activity exists
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                        Log.d("DownloadScreen", "Intent started successfully.")
                                    } else {
                                        Log.e("DownloadScreen", "No app found to open this file: ${file.name}")
                                        Toast.makeText(context, "No app found to open ${file.name}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("DownloadScreen", "Error opening file: ${file.absolutePath}, error=${e.message}", e)
                                    Toast.makeText(
                                        context,
                                        "Cannot open file: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(file.name, color = Color.White)
                                Text("Tap to open", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
