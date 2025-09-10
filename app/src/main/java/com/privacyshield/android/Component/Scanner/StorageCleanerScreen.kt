package com.privacyshield.android.Component.Scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScanScreen(scanResults: ScanResults, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Results") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Storage Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total scanned: ${formatSize(scanResults.totalSize)}",
                        color = Color.Gray
                    )
                }
            }

            // File type breakdown
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(scanResults.fileCategories) { category ->
                    FileCategoryItem(category)
                }
            }
        }
    }
}

@Composable
fun FileCategoryItem(category: FileCategory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on file type
            Icon(
                imageVector = when (category.type) {
                    FileType.IMAGE -> Icons.Filled.Image
                    FileType.VIDEO -> Icons.Filled.VideoLibrary
                    FileType.AUDIO -> Icons.Filled.AudioFile
                    FileType.DOCUMENT -> Icons.Filled.Description
                    FileType.APP -> Icons.Filled.Apps
                    FileType.FONT -> Icons.Filled.TextFields
                    else -> Icons.Filled.Folder
                },
                contentDescription = category.type.name,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF6200EE)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.type.displayName,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${category.fileCount} files • ${formatSize(category.totalSize)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Percentage and visual indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${category.percentage}%",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = category.percentage / 100f,
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF6200EE)
                )
            }
        }
    }
}

// Data classes
data class ScanResults(
    val totalSize: Long,
    val fileCategories: List<FileCategory>
)

data class FileCategory(
    val type: FileType,
    val fileCount: Int,
    val totalSize: Long,
    val percentage: Int
)

enum class FileType(val displayName: String) {
    IMAGE("Images"),
    VIDEO("Videos"),
    AUDIO("Audio"),
    DOCUMENT("Documents"),
    APP("Apps"),
    FONT("Fonts"),
    OTHER("Other Files")
}

// Function to perform the scan (would be implemented elsewhere)
fun performDeepScan(): ScanResults {
    // This would actually scan the device storage
    // For demonstration, returning mock data
    return ScanResults(
        totalSize = 1258490368, // 1.17 GB
        fileCategories = listOf(
            FileCategory(FileType.IMAGE, 243, 536870912, 42), // 512 MB
            FileCategory(FileType.VIDEO, 87, 322122547, 25), // 307 MB
            FileCategory(FileType.AUDIO, 156, 107374182, 8),  // 102 MB
            FileCategory(FileType.DOCUMENT, 432, 80530636, 6), // 76 MB
            FileCategory(FileType.APP, 23, 214748364, 17),    // 204 MB
            FileCategory(FileType.FONT, 12, 10485760, 1),     // 10 MB
            FileCategory(FileType.OTHER, 78, 52428800, 4)     // 50 MB
        )
    )
}

// Usage in your main screen
@Composable
fun StorageCleanerScreen() {
    var showScanResults by remember { mutableStateOf(false) }
    val scanResults = remember { mutableStateOf<ScanResults?>(null) }

    if (showScanResults) {
        scanResults.value?.let { results ->
            FileScanScreen(
                scanResults = results,
                onBack = { showScanResults = false }
            )
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            CleanerCard(onQuickScan = {
                // Perform the scan and show results
                scanResults.value = performDeepScan()
                showScanResults = true
            })
            // ... rest of your UI
        }
    }
}