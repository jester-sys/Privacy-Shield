package com.privacyshield.android.Component.Scanner

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileDetailScreen(
    title: String,
    files: List<File>,
    type: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectionMode by remember { mutableStateOf(false) }
    val selectedFiles = remember { mutableStateListOf<File>() }

    // ✅ Local list state for filtering
    var fileList by remember { mutableStateOf(files) }

    // Jab bhi parent se nayi sorted files aayengi to sync karo
    LaunchedEffect(files) {
        fileList = files
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
                        Text("Move to Trash")
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF121212)),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(fileList) { file ->
                val isSelected = selectedFiles.contains(file)

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
                        FileThumbnail(file = file, modifier = Modifier.fillMaxSize())

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
                    val trashDir = File(fileList.firstOrNull()?.parentFile, "Trash")
                    if (!trashDir.exists()) trashDir.mkdir()
                    selectedFiles.forEach { it.renameTo(File(trashDir, it.name)) }

                    // ✅ Filter removed files
                    fileList = fileList.filterNot { selectedFiles.contains(it) }

                    selectedFiles.clear()
                    selectionMode = false
                    showTrashDialog = false
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
                    selectedFiles.forEach { it.delete() }

                    // ✅ Filter removed files
                    fileList = fileList.filterNot { selectedFiles.contains(it) }

                    selectedFiles.clear()
                    selectionMode = false
                    showDeleteDialog = false
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

fun openFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, getMimeType(file.absolutePath))
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

fun getMimeType(path: String): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(path)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
}


@Composable
fun FileThumbnail(file: File, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mimeType = getMimeType(file.absolutePath) ?: ""

    when {
        mimeType.startsWith("image") -> {
            // 🖼 Image thumbnail
            AsyncImage(
                model = file,
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }

        mimeType.startsWith("video") -> {
            // 🎥 Video thumbnail (non-blocking)
            val bitmap by produceState<Bitmap?>(initialValue = null, file) {
                value = withContext(Dispatchers.IO) {
                    var retriever: MediaMetadataRetriever? = null
                    try {
                        retriever = MediaMetadataRetriever()
                        retriever.setDataSource(file.absolutePath)
                        retriever.getFrameAtTime(1_000_000) // 1 second frame
                    } catch (e: Exception) {
                        null
                    } finally {
                        retriever?.release()
                    }
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = modifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = modifier.padding(16.dp)
                )
            }
        }

        mimeType == "application/pdf" -> {
            // 📄 PDF icon
            Icon(
                Icons.Default.Description,
                contentDescription = "PDF",
                tint = Color(0xFFE53935),
                modifier = modifier.padding(20.dp)
            )
        }

        mimeType.startsWith("audio") -> {
            // 🎵 Audio icon
            Icon(
                Icons.Default.MusicNote,
                contentDescription = "Audio",
                tint = Color(0xFF29B6F6),
                modifier = modifier.padding(20.dp)
            )
        }

        else -> {
            // 📂 Default file icon
            Icon(
                Icons.Default.InsertDriveFile,
                contentDescription = "File",
                tint = Color.Gray,
                modifier = modifier.padding(20.dp)
            )
        }
    }
}
