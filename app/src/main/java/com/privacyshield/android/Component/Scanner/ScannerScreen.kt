package com.privacyshield.android.Component.Scanner

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun ScannerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val base = getWhatsAppBasePath()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            context.startActivity(intent)
        }
    }

    // Images
    val images = fetchFilesFromPossibleFolders(
        listOf("$base/WhatsApp Images", "$base/WhatsApp Image"),
        listOf(".jpg", ".jpeg", ".png", ".webp")
    )

    // Videos
    val videos = fetchFilesFromPossibleFolders(
        listOf("$base/WhatsApp Video", "$base/WhatsApp Videos"),
        listOf(".mp4", ".3gp", ".mkv", ".avi")
    )

    // Documents
    val documents = fetchFilesFromPossibleFolders(
        listOf("$base/WhatsApp Documents", "$base/WhatsApp Document"),
        listOf(".pdf", ".docx", ".doc", ".pptx", ".ppt", ".xls", ".xlsx", ".txt")
    )

    // Stickers
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
        Text(
            text = "Cleaner",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text("You cleaned 0 B this week", color = Color.White, fontSize = 16.sp)
                    Text("Free: 0% • Used: 0%", color = Color.Gray, fontSize = 14.sp)
                }
                Button(
                    onClick = {
                        Toast.makeText(context, "Quick Scan Started", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A6FF7)),
                    shape = CircleShape
                ) {
                    Text("Quick Scan")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("WhatsApp Files", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            item { FileRow("Images", images, "image") }
            item { FileRow("Videos", videos, "video") }
            item { FileRow("Documents", documents, "doc") }
            item { FileRow("Stickers", stickers, "sticker") }
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


