package com.privacyshield.android.Component.Scanner

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.util.Log
import android.util.Size
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privacyshield.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileDetailScreen(
    title: String,
    files: List<File>,
    type: String,
    onBack: () -> Unit,
    navController: NavController,
    gridColumns: Dp
) {
    val context = LocalContext.current
    // 🔹 Debug logs
    LaunchedEffect(files) {
        Log.d("FileDetailScreen", "Title: $title, Type: $type, Files size: ${files.size}")
        files.forEach {
            Log.d("FileDetailScreen", "File: ${it.absolutePath}")
        }
    }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedFiles = remember { mutableStateListOf<File>() }
    val gridState = rememberLazyGridState()

    val scope = rememberCoroutineScope()
    val sortOption by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("sortOption", "Name")
        ?.collectAsState() ?: remember { mutableStateOf("Name") }

    // Use derivedStateOf for efficient sorting
    val fileList by remember(files, sortOption) {
        derivedStateOf {
            when (sortOption) {
                "Name" -> files.sortedBy { it.name.lowercase() }
                "Date" -> files.sortedByDescending { it.lastModified() }
                "Size" -> files.sortedByDescending { it.length() }
                else -> files
            }
        }
    }

    // 🔹 Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (selectionMode && selectedFiles.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { showTrashDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3A))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Trash", tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Move to Trash", color = Color.White)
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = gridColumns),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF121212)),
            state = gridState,
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(fileList, key = { it.absolutePath }) { file ->
                val isSelected by derivedStateOf { selectedFiles.contains(file) }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .combinedClickable(
                            onClick = {
                                if (selectionMode) {
                                    if (isSelected) selectedFiles.remove(file)
                                    else selectedFiles.add(file)
                                } else {
                                    openFile(context, file)
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) selectionMode = true
                                if (isSelected) selectedFiles.remove(file)
                                else selectedFiles.add(file)
                            }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF3A3F77) else Color(0xFF2A2A2A)
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OptimizedFileThumbnail(file = file, modifier = Modifier.fillMaxSize())

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = file.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        if (selectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedFiles.add(file) else selectedFiles.remove(file)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 🔹 Trash Confirm Dialog
    if (showTrashDialog) {
        AlertDialog(
            onDismissRequest = { showTrashDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val trashDir = File(fileList.firstOrNull()?.parentFile, "Trash")
                        if (!trashDir.exists()) trashDir.mkdir()
                        selectedFiles.forEach { it.renameTo(File(trashDir, it.name)) }

                        // Clear selection in main thread
                        withContext(Dispatchers.Main) {
                            selectedFiles.clear()
                            selectionMode = false
                            showTrashDialog = false
                        }
                    }
                }) {
                    Text("Yes, Move", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrashDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Move to Trash", color = Color.White) },
            text = { Text("Do you really want to move selected files to Trash?", color = Color.LightGray) },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    // 🔹 Delete Confirm Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        selectedFiles.forEach { it.delete() }

                        // Clear selection in main thread
                        withContext(Dispatchers.Main) {
                            selectedFiles.clear()
                            selectionMode = false
                            showDeleteDialog = false
                        }
                    }
                }) {
                    Text("Yes, Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Delete Files", color = Color.White) },
            text = { Text("This action cannot be undone. Are you sure?", color = Color.LightGray) },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@SuppressLint("NewApi")
@Composable
fun OptimizedFileThumbnail(file: File, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mimeType = remember(file.absolutePath) { getMimeType(file.absolutePath) ?: "" }

    val thumbnailModifier = modifier.size(120.dp)

    when {
        // ------------------ IMAGE ------------------
        mimeType.startsWith("image") -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(file)
                    .size(240, 240)
                    .diskCacheKey(file.absolutePath)   // cache on disk
                    .memoryCacheKey(file.absolutePath) // cache in RAM
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = thumbnailModifier,
                contentScale = ContentScale.Crop
            )
        }

        // ------------------ VIDEO ------------------
        mimeType.startsWith("video") -> {
            // Cache thumbnail per file (no recreation on scroll)
            val bitmap by produceState<Bitmap?>(null, file.absolutePath) {
                withContext(Dispatchers.IO) {
                    delay(100L) // 👈 delay to smooth scroll
                    value = ThumbnailUtils.createVideoThumbnail(
                        file,
                        Size(240, 240),
                        null
                    )
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Video thumbnail",
                    modifier = thumbnailModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = thumbnailModifier.background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // ------------------ PDF ------------------
        mimeType == "application/pdf" -> {
            Box(
                modifier = thumbnailModifier.background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "PDF",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // ------------------ AUDIO ------------------
        mimeType.startsWith("audio") -> {
            Box(
                modifier = thumbnailModifier.background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Audio",
                    tint = Color(0xFF29B6F6),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // ------------------ OTHER ------------------
        else -> {
            Box(
                modifier = thumbnailModifier.background(Color(0xFFFAFAFA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.InsertDriveFile,
                    contentDescription = "File",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


fun openFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, getMimeType(file.absolutePath))
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    // Use try-catch to prevent crashes
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }
}

fun getMimeType(path: String): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(path)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
}