package com.privacyshield.android.Component.Scanner.Whatsapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyshield.android.R
import kotlinx.coroutines.delay

@Composable
fun CleanWhatsAppMediaScreen(
    viewModel: WhatsAppCleanerViewModel = hiltViewModel()
) {
    val totalSize by viewModel.totalSize.collectAsState()
    val totalFiles by viewModel.totalFiles.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val progress by viewModel.cleanProgress.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    val selectedCategories = remember { mutableStateListOf<String>() }
    // ✅ Clean Button with auto shrink/expand
    var expanded by remember { mutableStateOf(false) }

// 🔄 Button expand/shrink toggle (jaise scroll ya delay pe)
    LaunchedEffect(Unit) {
        delay(2000) // 2 sec baad expand hoga (scroll event bhi use kar sakte ho)
        expanded = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF25D366))
                }
            } else {
                StorageUsageCard(
                    totalSize = totalSize,
                    totalFiles = totalFiles,
                    categories = categories
                )


                // ✅ Category Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categories) { category ->
                        CategoryCard(category)
                    }
                }
            }
        }

        // ✅ FAB sirf tab dikhana jab loading complete ho
        if (!isLoading) {
            ExtendedFloatingActionButton(
                onClick = { showConfirmDialog = true },
                expanded = expanded,
                containerColor = Color(0xFF25D366),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Delete",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                },
                text = { Text("Clean WhatsApp Media") }
            )
        }


        // ✅ Confirm Dialog (Category Selection)
        if (showConfirmDialog) {
            if (showConfirmDialog) {
                CategorySelectionBottomSheet(
                    categories = categories,
                    selectedCategories = selectedCategories,
                    onDismiss = { showConfirmDialog = false },
                    onConfirm = {
                        showConfirmDialog = false
                        showProgressDialog = true
                        viewModel.cleanFilesByCategories(selectedCategories) {
                            showProgressDialog = false
                        }
                    }
                )
            }

        }

        // ✅ Progress Dialog
        if (showProgressDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Deleting Files...") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("$progress% completed", color = Color.White)
                    }
                },
                confirmButton = {}
            )
        }
    }

    // Refresh on launch
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }
}


@Composable
fun CategoryCard(category: FileCategory) {
    val name = category.name.lowercase()
    val categoryColor = when {
        "image" in name -> Color(0xFF42A5F5)   // Blue
        "video" in name -> Color(0xFF66BB6A)   // Green
        "document" in name -> Color(0xFFFFCA28) // Yellow
        "status" in name -> Color(0xFFEF5350)   // Red
        "audio" in name || "voice" in name -> Color(0xFFAB47BC) // Purple
        "gif" in name -> Color(0xFF26C6DA)      // Cyan
        "sticker" in name -> Color(0xFFFF7043) // Orange
        else -> Color(0xFF2A2A2A)              // Default
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(4.dp)
    ) {
        // Draw background using Canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = categoryColor.copy(alpha = 0.12f), // subtle background
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = category.iconRes,
                contentDescription = category.name,
                tint = categoryColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(category.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(formatSize(category.size), color = Color(0xFFB0BEC5), fontSize = 13.sp)
                Text("${category.fileCount} files", color = Color(0xFF78909C), fontSize = 11.sp)
            }
        }
    }
}





@Composable
fun StorageUsageCard(
    totalSize: Long,
    totalFiles: Int,
    categories: List<FileCategory>
) {
    // Function to get color by category name
    fun getCategoryColor(name: String): Color = when {
        "image" in name.lowercase() -> Color(0xFF42A5F5)
        "video" in name.lowercase() -> Color(0xFF66BB6A)
        "document" in name.lowercase() -> Color(0xFFFFCA28)
        "status" in name.lowercase() -> Color(0xFFEF5350)
        "audio" in name.lowercase() || "voice" in name.lowercase() -> Color(0xFFAB47BC)
        "gif" in name.lowercase() -> Color(0xFF26C6DA)
        "sticker" in name.lowercase() -> Color(0xFFFF7043)
        else -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut Chart with WhatsApp Icon
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    var startAngle = -90f
                    categories.forEach { category ->
                        val sweepAngle = (category.size.toFloat() / totalSize.toFloat()) * 360f
                        drawArc(
                            color = getCategoryColor(category.name),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 22f, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_fill_whatsapp_icon,), // replace with your WhatsApp vector drawable
                    contentDescription = "WhatsApp",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Text Info Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Storage that can be freed",
                    color = Color(0xFFB0BEC5),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formatSize(totalSize),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$totalFiles files",
                    color = Color(0xFF90A4AE),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



// ✅ Model
data class FileCategory(
    val name: String,
    val size: Long,
    val fileCount: Int,
    val iconRes: ImageVector
)

// ✅ Utils
fun formatSize(size: Long): String {
    if (size == 0L) return "0 B"

    val kb = size / 1024f
    val mb = kb / 1024f
    val gb = mb / 1024f
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$size B"
    }
}

@Composable
fun DeleteOptionsDialog(
    categories: List<FileCategory>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedItems = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Select Files to Delete") },
        text = {
            Column {
                categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedItems.contains(category.name)) {
                                    selectedItems.remove(category.name)
                                } else {
                                    selectedItems.add(category.name)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = selectedItems.contains(category.name),
                            onCheckedChange = {
                                if (it) selectedItems.add(category.name)
                                else selectedItems.remove(category.name)
                            }
                        )
                        Text(text = category.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedItems) }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
