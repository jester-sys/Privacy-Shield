package com.privacyshield.android.Component.Settings.Trash

// Compose
import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ViewList

// Checkbox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

// StateFlow
import androidx.hilt.navigation.compose.hiltViewModel
// ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyshield.android.Component.db.TrashFile
import kotlinx.coroutines.launch

// Hilt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


// File model
import java.io.File


@Composable
fun TrashScreen(trashViewModel: TrashViewModel = hiltViewModel()) {
    val trashFiles by trashViewModel.trashFiles.collectAsState()
    val selectedFiles by trashViewModel.selectedFiles.collectAsState()
    var isGridView by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar: Title + Select All + View Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Trash (${trashFiles.size})",
                style = MaterialTheme.typography.titleLarge
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (selectedFiles.size == trashFiles.size) {
                        trashViewModel.clearSelection()
                    } else {
                        trashViewModel.selectAll()
                    }
                }) {
                    Icon(
                        imageVector = if (selectedFiles.size == trashFiles.size)
                            Icons.Default.CheckBox
                        else
                            Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Select All"
                    )
                }

                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle View"
                    )
                }
            }
        }

        // File List / Grid
        if (trashFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No files in Trash", color = Color.Gray)
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(trashFiles) { file ->
                        TrashFileItem(
                            file = file,
                            isSelected = selectedFiles.contains(file),
                            onClick = { trashViewModel.toggleSelection(file) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trashFiles) { file ->
                        TrashFileItem(
                            file = file,
                            isSelected = selectedFiles.contains(file),
                            onClick = { trashViewModel.toggleSelection(file) }
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
        if (selectedFiles.isNotEmpty()) {
            FileActionsBar(
                selectedCount = selectedFiles.size,
                onRestore = { trashViewModel.restoreFiles(selectedFiles.toList()) },
                onDelete = { trashViewModel.deleteFilesPermanently(selectedFiles.toList()) }
            )
        }
    }
}


@Composable
fun FileActionsBar(
    selectedCount: Int,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),

        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRestore,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f).height(55.dp)
        ) {
            Icon(Icons.Default.Restore, contentDescription = "Restore", tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text("Restore ($selectedCount)", color = Color.White)
        }

        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f).height(55.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text("Delete", color = Color.White)
        }
    }
}

@Composable
fun TrashFileItem(
    file: TrashFile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fileSizeKb = remember(file.size) { file.size / 1024 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Thumbnail / Icon
            when {
                file.name.endsWith(".jpg", true) || file.name.endsWith(".png", true) -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(file.path))
                            .crossfade(true)
                            .build(),
                        contentDescription = file.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                file.name.endsWith(".mp4", true) || file.name.endsWith(".mkv", true) -> {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = file.name,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = file.name,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 File name
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 🔹 File size
            Text(
                text = "$fileSizeKb KB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
