package com.privacyshield.android.Component.Scanner

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.io.File

@Composable
fun ScannerScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val base = getWhatsAppBasePath()

    // Files
    val images = fetchFilesFromPossibleFolders(
        listOf("$base/WhatsApp Images", "$base/WhatsApp Image"),
        listOf(".jpg", ".jpeg", ".png", ".webp")
    )
    val videos = fetchFilesFromPossibleFolders(
        listOf("$base/WhatsApp Video", "$base/WhatsApp Videos"),
        listOf(".mp4", ".3gp", ".mkv", ".avi")
    )
    val documents = fetchFilesFromPossibleFolders(
        listOf("$base/WhatsApp Documents", "$base/WhatsApp Document"),
        listOf(".pdf", ".docx", ".doc", ".pptx", ".ppt", ".xls", ".xlsx", ".txt")
    )
    val stickers = fetchFilesFromPossibleFolders(
        listOf("$base/WhatsApp Stickers", "$base/WhatsApp Sticker"),
        listOf(".webp")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // Header
        Text("Cleaner", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(12.dp))

        CleanerCard(
            onQuickScan = {
                Toast.makeText(context, "Quick Scan Started", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("WhatsApp Files", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Text("Clear out media you no longer need.", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                FileRow("Images", images, "image") { files, type ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("files", files)
                    navController.currentBackStackEntry?.savedStateHandle?.set("type", type)
                    navController.navigate("full_file_screen")
                }
            }
            item {
                FileRow("Videos", videos, "video") { files, type ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("files", files)
                    navController.currentBackStackEntry?.savedStateHandle?.set("type", type)
                    navController.navigate("full_file_screen")
                }
            }
            item {
                FileRow("Documents", documents, "doc") { files, type ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("files", files)
                    navController.currentBackStackEntry?.savedStateHandle?.set("type", type)
                    navController.navigate("full_file_screen")
                }
            }
            item {
                FileRow("Stickers", stickers, "sticker") { files, type ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("files", files)
                    navController.currentBackStackEntry?.savedStateHandle?.set("type", type)
                    navController.navigate("full_file_screen")
                }
            }
        }
    }
}

@Composable
fun QuickScanButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(110.dp) // outer ring size
            .background(Color(0xFF2A2A2A), CircleShape) // ring ka color (dark background)
            .padding(6.dp) // space for inner circle
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF3A3F77), CircleShape) // inner filled circle
        ) {
            Text(
                text = "Quick\nScan",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun CleanerCard(onQuickScan: () -> Unit) {
    val storageInfo = remember { getStorageInfo() }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "You cleaned 0 B this week",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )

                Text(
                    text = "Free: ${storageInfo.freePercent}% • Used: ${storageInfo.usedPercent}%",
                    fontSize = 14.sp,
                    color = Color(0xFFBBBBBB)
                )

                Text(
                    text = "Total: ${formatSize(storageInfo.totalBytes)} | Free: ${formatSize(storageInfo.freeBytes)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            QuickScanButton(onClick = onQuickScan)
        }
    }
}

// ✅ Helper function to handle both singular/plural folder names
fun fetchFilesFromPossibleFolders(folders: List<String>, extensions: List<String>): List<File> {
    for (folder in folders) {
        val files = fetchFilesFromFolder(folder, extensions)
        if (files.isNotEmpty()) return files
    }
    return emptyList()
}

fun getWhatsAppBasePath(): String {
    val newPath = Environment.getExternalStorageDirectory().absolutePath +
            "/Android/media/com.whatsapp/WhatsApp/Media/"
    val oldPath = Environment.getExternalStorageDirectory().absolutePath +
            "/WhatsApp/Media/"

    return if (File(newPath).exists()) newPath else oldPath
}

