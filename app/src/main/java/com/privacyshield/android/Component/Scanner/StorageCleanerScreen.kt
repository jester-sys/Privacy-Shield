package com.privacyshield.android.Component.Scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import coil.compose.AsyncImage
import com.privacyshield.android.Component.Scanner.QuickScan.CleanerViewModel
import com.privacyshield.android.Component.Scanner.QuickScan.ScanningState
import com.privacyshield.android.Component.Service.VirusTotalScanService
import com.privacyshield.android.Component.Settings.Trash.TrashViewModel
import kotlinx.coroutines.delay
import java.io.File

// Update the QuickScanResult data class to include all new categories
data class QuickScanResult(
    val cacheSize: Long,
    val junkSize: Long,
    val largeFiles: List<File>,
    val duplicateFiles: List<File>,
    val apkFiles: List<File>,
    val emptyFolders: List<File>,
    val downloadFiles: List<File>,
    val screenshotFiles: List<File>,
    val videoFiles: List<File>,
    val photoFiles: List<File>
)



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScanScreen(
    viewModel: CleanerViewModel = hiltViewModel(),
    trashViewModel: TrashViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }
    val vtScanProgress by viewModel.vtScanProgress.collectAsState()

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedFiles by remember { mutableStateOf<Set<File>>(emptySet()) }
    var isGridView by remember { mutableStateOf(false) } // 🔹 NEW state
    var context  = LocalContext.current


    val scanResult by viewModel.scanResult.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanningState by viewModel.scanningState.collectAsState()
    val vtProgress by viewModel.vtScanProgress.collectAsState()
    val vtScanDialog by viewModel.vtScanDialog.collectAsState()
    val scannedFiles by viewModel.scannedFiles.collectAsState()
    val totalFiles by viewModel.totalFiles.collectAsState()
    val scanCompleted by viewModel.scanCompleted.collectAsState()
    val showScanDialog by viewModel.showScanDialog.collectAsState()
    val currentScanFile by viewModel.currentScanFile.collectAsState()


    // ✅ Add this to handle scan completion
    LaunchedEffect(Unit) {
        snapshotFlow { scanCompleted }
            .collect { completed ->
                if (completed) {
                    // Auto-close dialog after 2 seconds when scan completes
                    delay(2000)
                    viewModel.setShowScanDialog(false)
                    viewModel.resetScanState()
                }
            }
    }

    // Broadcast receiver for scan updates
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    "VT_SCAN_PROGRESS" -> {
                        val progress = intent.getIntExtra("progress", 0)
                        val currentFile = intent.getIntExtra("currentFile", 0)
                        val totalFiles = intent.getIntExtra("totalFiles", 0)
                        val fileName = intent.getStringExtra("fileName")

                        viewModel.updateVTProgress(progress)
                        viewModel.updateScanProgress(currentFile, totalFiles)
                        fileName?.let { viewModel.updateCurrentScanFile(it) }
                    }
                    "VT_SCAN_COMPLETE" -> {
                        val paths = intent.getStringArrayExtra("files") ?: emptyArray()
                        viewModel.markScanComplete()

                        // Show completion message
                        Toast.makeText(
                            context,
                            "Scan completed! Results saved to Downloads/VT_Scan_Results",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    "VT_SCAN_ERROR" -> {
                        val error = intent.getStringExtra("error")
                        viewModel.setShowScanDialog(false)
                        viewModel.resetScanState()
                        Toast.makeText(
                            context,
                            "Scan failed: $error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    "VT_SCAN_FINISHED" -> {
                        // This will be called whether scan completed or was cancelled
                        viewModel.setShowScanDialog(false)
                        viewModel.resetScanState()
                    }
                    "VT_SCAN_CANCELLED" -> {
                        viewModel.setShowScanDialog(false)
                        viewModel.resetScanState()
                        Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
                    }
                    "VT_SCAN_COMPLETE" -> {
                        viewModel.markScanComplete()
                        Toast.makeText(
                            context,
                            "Scan completed! Results saved to Downloads/VT_Scan_Results",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("VT_SCAN_PROGRESS")
            addAction("VT_SCAN_COMPLETE")
            addAction("VT_SCAN_ERROR")
            addAction("VT_SCAN_FINISHED")
            addAction("VT_SCAN_CANCELLED")
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }




    LaunchedEffect(Unit) {
        if (scanResult == null && scanningState == ScanningState.IDLE) {
            viewModel.quickScan()
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }



    TrashDialogs(
        showTrashDialog = showTrashDialog,
        onDismiss = { showTrashDialog = false },
        selectedFiles = selectedFiles,
        onMoved = { selectedFiles = emptySet() }
    )

    Scaffold(
        topBar = {
            if (selectedCategory == null) {
                TopAppBar(
                    title = {
                        Text(
                            "Smart Storage Cleaner",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF101010)
                    ),
                    actions = {
                        if (scanningState == ScanningState.COMPLETED) {
                            IconButton(onClick = {
                                viewModel.clearResults()
                                selectedCategory = null
                                selectedFiles = emptySet()
                            }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Rescan",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            selectedCategory ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF101010)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { selectedCategory = null }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // 🔹 Select All / Deselect All
                        scanResult?.let { result ->
                            val files = when (selectedCategory) {
                                "Large Files" -> result.largeFiles
                                "Duplicate Files" -> result.duplicateFiles
                                "APK Files" -> result.apkFiles
                                "Empty Folders" -> result.emptyFolders
                                "Downloads" -> result.downloadFiles
                                "Screenshots" -> result.screenshotFiles
                                "Videos" -> result.videoFiles
                                "Photos" -> result.photoFiles
                                else -> emptyList()
                            }
                            if (files.isNotEmpty()) {
                                val allSelected = selectedFiles.size == files.size
                                IconButton(onClick = {
                                    selectedFiles =
                                        if (allSelected) emptySet()
                                        else files.toSet()
                                }) {
                                    Icon(
                                        if (allSelected) Icons.Default.Close else Icons.Default.DoneAll,
                                        contentDescription = "Select All",
                                        tint = Color.White
                                    )
                                }
                            }
                        }


                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = "Toggle View",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (scanningState) {
                ScanningState.SCANNING -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = scanProgress / 100f,
                                modifier = Modifier.size(180.dp),
                                strokeWidth = 10.dp,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                "$scanProgress%",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Scanning your storage...", color = Color.Gray, fontSize = 16.sp)
                    }
                }

                ScanningState.COMPLETED -> {
                    scanResult?.let { result ->
                        if (selectedCategory != null) {
                            when (selectedCategory) {
                                "Cache" -> CacheJunkView("Cache", result.cacheSize)
                                "Junk" -> CacheJunkView("Junk", result.junkSize)

                                "Large Files" -> FileGridView(
                                    title = "Large Files",
                                    files = result.largeFiles,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView   // ✅ pass here
                                )

                                "Duplicate Files" -> FileGridView(
                                    title = "Duplicate Files",
                                    files = result.duplicateFiles,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView
                                )

                                "APK Files" -> FileGridView(
                                    title = "APK Files",
                                    files = result.apkFiles,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView
                                )

                                "Empty Folders" -> FileGridView(
                                    title = "Empty Folders",
                                    files = result.emptyFolders,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView
                                )

                                "Downloads" -> FileGridView(
                                    title = "Downloads",
                                    files = result.downloadFiles,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView
                                )

                                "Screenshots" -> MediaGridView(
                                    title = "Screenshots",
                                    files = result.screenshotFiles,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView
                                )

                                "Videos" -> MediaGridView(
                                    title = "Videos",
                                    files = result.videoFiles,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView
                                )
                                "Photos" -> MediaGridView(
                                    title = "Photos",
                                    files = result.photoFiles,
                                    selectedFiles = selectedFiles,
                                    onSelectionChange = { selectedFiles = it },
                                    isGridView = isGridView
                                )
                            }

                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Basic Cleaning Section
                                SectionHeader("Basic Cleaning")
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.height(100.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    item {
                                        SummaryCard(
                                            title = "Cache Files",
                                            size = result.cacheSize,
                                            icon = Icons.Default.Cached,
                                            color = Color(0xFFFF9800),
                                            onClick = { selectedCategory = "Cache" }
                                        )
                                    }
                                    item {
                                        SummaryCard(
                                            title = "Junk Files",
                                            size = result.junkSize,
                                            icon = Icons.Default.Delete,
                                            color = Color(0xFFF44336),
                                            onClick = { selectedCategory = "Junk" }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // File Management Section
                                SectionHeader("File Management")
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.height(100.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    item {
                                        SummaryCard(
                                            title = "Large Files",
                                            size = result.largeFiles.sumOf { it.length() },
                                            icon = Icons.Default.Storage,
                                            color = Color(0xFF2196F3),
                                            onClick = { selectedCategory = "Large Files" }
                                        )
                                    }
                                    item {
                                        SummaryCard(
                                            title = "Duplicate Files",
                                            size = result.duplicateFiles.sumOf { it.length() },
                                            icon = Icons.Default.CopyAll,
                                            color = Color(0xFF9C27B0),
                                             onClick = { selectedCategory = "Duplicate Files" }
                                        )
                                      }
                                }

                                Spacer(Modifier.height(8.dp))

                                // App Files Section
                                SectionHeader("App Files")
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.height(100.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    item {
                                        SummaryCard(
                                            title = "APK Files",
                                            size = result.apkFiles.sumOf { it.length() },
                                            icon = Icons.Default.Android,
                                            color = Color(0xFF4CAF50),
                                            onClick = { selectedCategory = "APK Files" }
                                        )
                                    }
                                    item {
                                        SummaryCard(
                                            title = "Empty Folders",
                                            size = result.emptyFolders.sumOf { it.length() },
                                            icon = Icons.Default.FolderOpen,
                                            color = Color(0xFFFFC107),
                                            onClick = { selectedCategory = "Empty Folders" }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Media Files Section
                                SectionHeader("Media Files")
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.height(200.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    item {
                                        SummaryCard(
                                            title = "Downloads",
                                            size = result.downloadFiles.sumOf { it.length() },
                                            icon = Icons.Default.Download,
                                            color = Color(0xFF607D8B),
                                            onClick = { selectedCategory = "Downloads" }
                                        )
                                    }
                                    item {
                                        SummaryCard(
                                            title = "Screenshots",
                                            size = result.screenshotFiles.sumOf { it.length() },
                                            icon = Icons.Default.Screenshot,
                                            color = Color(0xFFE91E63),
                                            onClick = { selectedCategory = "Screenshots" }
                                        )
                                    }
                                    item {
                                        SummaryCard(
                                            title = "Videos",
                                            size = result.videoFiles.sumOf { it.length() },
                                            icon = Icons.Default.Videocam,
                                            color = Color(0xFF3F51B5),
                                            onClick = { selectedCategory = "Videos" }
                                        )
                                    }
                                    item {
                                        SummaryCard(
                                            title = "Photos",
                                            size = result.photoFiles.sumOf { it.length() },
                                            icon = Icons.Default.Photo,
                                            color = Color(0xFF009688),
                                            onClick = { selectedCategory = "Photos" }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Clean All Button
                                Button(
                                    onClick = { /* TODO delete all files */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(50.dp)
                                ) {
                                    Icon(Icons.Default.CleanHands, contentDescription = "Clean", modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Clean All", color = Color.White, fontSize = 16.sp)
                                }

                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(Modifier.height(12.dp))
                        Text("Preparing scan...", color = Color.Gray)
                    }
                }
            }
            if (selectedFiles.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ❌ Delete Button
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)), // Softer red
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp) // Taller button
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                        Text(
                            "Delete",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 🗑 Move to Trash Button
                    Button(
                        onClick = { showTrashDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)), // Warm orange
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Move to Trash",
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Move to Trash",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val filesToScan = selectedFiles.filter { it.isFile && it.exists() && it.length() <= 32 * 1024 * 1024 }
                            if (filesToScan.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "No valid files selected or files too large (max 32MB)!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (filesToScan.size > 5) {
                                Toast.makeText(
                                    context,
                                    "Maximum 5 files allowed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                viewModel.resetScanState() // ✅ Reset state before new scan
                                viewModel.startScan(filesToScan, context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = "Scan", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Scan Files", color = Color.White, fontSize = 15.sp)
                    }

                    if (showScanDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                // Only allow dismiss if scan is completed
                                if (scanCompleted) {
                                    viewModel.setShowScanDialog(false)
                                    viewModel.resetScanState()
                                }
                            },
                            title = {
                                Column {
                                    Text("Scanning Files")
                                    if (!currentScanFile.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentScanFile!!,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            },
                            text = {
                                Column {
                                    Text("Scanned $scannedFiles of $totalFiles files")
                                    LinearProgressIndicator(
                                        progress = if (totalFiles > 0) scannedFiles.toFloat() / totalFiles else 0f,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Progress: ${vtProgress}%",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            confirmButton = {

                                TextButton(
                                    onClick = {
                                        viewModel.setShowScanDialog(false)
                                        viewModel.resetScanState()
                                    },
                                    enabled = scanCompleted || !scanCompleted // Always enabled
                                ) {
                                    Text(if (scanCompleted) "Close" else "Dismiss")
                                }
                            },
                            // In your AlertDialog
                            dismissButton = {
                                if (!scanCompleted) {
                                    TextButton(onClick = {
                                        viewModel.cancelScan() // This should send the cancel broadcast
                                    }) {
                                        Text("Cancel Scan")
                                    }
                                }
                            }
                        )
                    }
                }

                }
            }


        }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            // Delete files from disk
                            selectedFiles.forEach { it.delete() }

                            // ✅ Update ViewModel state so UI recomposes
                            viewModel.updateAfterDelete(selectedFiles, selectedCategory)

                            // Clear selection
                            selectedFiles = emptySet()
                            showDeleteDialog = false
                        }) {
                            Text("Delete", color = Color.Red)
                        }
                    },

                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Delete Files?", color = Color.White) },
                    text = { Text("Are you sure you want to permanently delete the selected files?", color = Color.Gray) },
                    containerColor = Color(0xFF1E1E1E)
                )
            }


        }




// Updated SectionHeader with better spacing
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}



// Media View (Grid + List Toggle)
@Composable
fun MediaGridView(
    title: String,
    files: List<File>,
    selectedFiles: Set<File>,
    onSelectionChange: (Set<File>) -> Unit,
    isGridView: Boolean   // 🔹 New param
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${files.size} items found",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No media found", color = Color.Gray)
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
                    items(files) { file ->
                        MediaGridItem(
                            file = file,
                            isSelected = selectedFiles.contains(file),
                            onSelect = {
                                val newSel = selectedFiles.toMutableSet()
                                if (!newSel.add(file)) newSel.remove(file)
                                onSelectionChange(newSel)
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files) { file ->
                        MediaGridItem(
                            file = file,
                            isSelected = selectedFiles.contains(file),
                            onSelect = {
                                val newSel = selectedFiles.toMutableSet()
                                if (!newSel.add(file)) newSel.remove(file)
                                onSelectionChange(newSel)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Media Grid Item with Thumbnail + Size
@Composable
fun MediaGridItem(file: File, isSelected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
    ) {
        // 🔹 Thumbnail
        AsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 File size overlay (bottom-left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(topEnd = 8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = formatSize(file.length()), // ✅ show size
                color = Color.White,
                fontSize = 11.sp
            )
        }

        // 🔹 Selection Overlay
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x802196F3))
            )
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
            )
        }
    }
}


// File View (List + Grid Toggle)
@Composable
fun FileGridView(
    title: String,
    files: List<File>,
    selectedFiles: Set<File>,
    onSelectionChange: (Set<File>) -> Unit,
    isGridView: Boolean   // 🔹 New param
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${files.size} items found",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No files found", color = Color.Gray)
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(files) { file ->
                        FileItem(
                            file = file,
                            isSelected = selectedFiles.contains(file),
                            onSelect = {
                                val newSel = selectedFiles.toMutableSet()
                                if (!newSel.add(file)) newSel.remove(file)
                                onSelectionChange(newSel)
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files) { file ->
                        FileItem(
                            file = file,
                            isSelected = selectedFiles.contains(file),
                            onSelect = {
                                val newSel = selectedFiles.toMutableSet()
                                if (!newSel.add(file)) newSel.remove(file)
                                onSelectionChange(newSel)
                            }
                        )
                    }
                }
            }
        }
    }
}



// Grid Item for Files
@Composable
fun FileGridItem(file: File, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1B5E20)
            else Color(0xFF2A2A2A)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // File Icon and Name
            Column {
                Icon(
                    imageVector = when {
                        file.extension.contains("jpg", true) ||
                                file.extension.contains("png", true) ||
                                file.extension.contains("jpeg", true) -> Icons.Default.Image
                        file.extension.contains("mp4", true) ||
                                file.extension.contains("mov", true) ||
                                file.extension.contains("avi", true) -> Icons.Default.Videocam
                        file.extension.contains("mp3", true) ||
                                file.extension.contains("wav", true) -> Icons.Default.AudioFile
                        file.extension.contains("pdf", true) -> Icons.Default.PictureAsPdf
                        file.extension.contains("doc", true) ||
                                file.extension.contains("docx", true) -> Icons.Default.Description
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = "File Type",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    file.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // File Size and Selection Indicator
            Column {
                Text(
                    formatSize(file.length()),
                    color = Color(0xFF4CAF50),
                    fontSize = 10.sp
                )

                if (isSelected) {
                    Spacer(Modifier.height(4.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Medium SummaryCard design
@Composable
fun SummaryCard(
    title: String,
    size: Long,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // Medium height
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), // Medium corners
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Slight elevation
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp) // Medium padding
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp) // Medium icon
            )
            Spacer(Modifier.width(10.dp)) // Medium spacing
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp, // Medium font
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatSize(size),
                    color = Color.Gray,
                    fontSize = 12.sp // Medium secondary text
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp) // Medium chevron
            )
        }
    }
}

// Fix CacheJunkView as well
// CacheJunkView with proper layout
@Composable
fun CacheJunkView(title: String, size: Long) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = formatSize(size),
            color = Color(0xFF4CAF50),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "This space can be freed by cleaning $title files",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: Clean cache/junk */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Clean $title")
        }
    }
}

// File Item with Thumbnail
@Composable
fun FileItem(file: File, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0x802196F3) else Color(0xFF2A2A2A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 🔹 Thumbnail
            Icon(
                imageVector = when {
                    file.extension.equals("apk", true) -> Icons.Default.Android
                    file.extension.equals("zip", true) || file.extension.equals("rar", true) -> Icons.Default.Archive
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = Color(0xFF90CAF9),
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )

            // 🔹 File Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    file.absolutePath,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 🔹 File Size + Check Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatSize(file.length()), // ✅ Size shown
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )

                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun TrashDialogs(
    showTrashDialog: Boolean,
    onDismiss: () -> Unit,
    selectedFiles: Set<File>,
    onMoved: () -> Unit,
    trashViewModel: TrashViewModel = hiltViewModel()
) {
    if (showTrashDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(onClick = {
                    trashViewModel.moveToTrash(selectedFiles) // ✅ Set<File> pass
                    onMoved()
                    onDismiss()
                }) {
                    Text("Move", color = Color(0xFFFF9800))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            },
            title = { Text("Move to Trash?", color = Color.White) },
            text = {
                Text(
                    "Selected files will be moved to Trash. You can restore or delete them later.",
                    color = Color.Gray
                )
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}


