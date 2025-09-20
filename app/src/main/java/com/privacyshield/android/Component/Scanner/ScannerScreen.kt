package com.privacyshield.android.Component.Scanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.privacyshield.android.Component.Scanner.QuickScan.CleanerViewModel
import com.privacyshield.android.Component.Scanner.QuickScan.Helper.scanCacheFiles
import com.privacyshield.android.Component.Service.VirusTotalScanService
import com.privacyshield.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@SuppressLint("NewApi")
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel(),
    cleanerViewModel: CleanerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val base = getWhatsAppBasePath()

    var hasRequestedPermissions by remember { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(true) }

    // File Picker Helper
    val filePickerHelper = remember { FilePickerHelper(context) }
    var showScanStartedDialog by remember { mutableStateOf(false) }
    var selectedFileName by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Collect states from CleanerViewModel
    val isScanning by cleanerViewModel.isScanning.collectAsState()
    val scanProgress by cleanerViewModel.vtScanProgress.collectAsState()
    val currentScanFile by cleanerViewModel.currentScanFile.collectAsState()
    val showScanDialog by cleanerViewModel.showScanDialog.collectAsState()
    val scanCompleted by cleanerViewModel.scanCompleted.collectAsState()
    val scannedFiles by cleanerViewModel.scannedFiles.collectAsState()
    val totalFiles by cleanerViewModel.totalFiles.collectAsState()

    // Broadcast receiver for scan updates (FileScanScreen jaisa)
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    "VT_SCAN_PROGRESS" -> {
                        val progress = intent.getIntExtra("progress", 0)
                        val currentFile = intent.getIntExtra("currentFile", 0)
                        val totalFiles = intent.getIntExtra("totalFiles", 0)
                        val fileName = intent.getStringExtra("fileName")

                        cleanerViewModel.updateVTProgress(progress)
                        cleanerViewModel.updateScanProgress(currentFile, totalFiles)
                        fileName?.let { cleanerViewModel.updateCurrentScanFile(it) }
                    }
                    "VT_SCAN_COMPLETE" -> {
                        val paths = intent.getStringArrayExtra("files") ?: emptyArray()
                        cleanerViewModel.markScanComplete()

                        // Show completion message
                        Toast.makeText(
                            context,
                            "Scan completed! Results saved to Downloads/VT_Scan_Results",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    "VT_SCAN_ERROR" -> {
                        val error = intent.getStringExtra("error")
                        cleanerViewModel.setShowScanDialog(false)
                        cleanerViewModel.resetScanState()
                        Toast.makeText(
                            context,
                            "Scan failed: $error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    "VT_SCAN_FINISHED" -> {
                        // This will be called whether scan completed or was cancelled
                        cleanerViewModel.setShowScanDialog(false)
                        cleanerViewModel.resetScanState()
                    }
                    "VT_SCAN_CANCELLED" -> {
                        cleanerViewModel.setShowScanDialog(false)
                        cleanerViewModel.resetScanState()
                        Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
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

            }
        }
    }

    // Auto-close dialog when scan completes (FileScanScreen jaisa)
    LaunchedEffect(scanCompleted) {
        if (scanCompleted) {
            // Auto-close dialog after 2 seconds when scan completes
            delay(2000)
            cleanerViewModel.setShowScanDialog(false)
            cleanerViewModel.resetScanState()
        }
    }

    // Launcher for file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("FilePicker", "Selected URI: $uri")

                // Show loading immediately
                selectedFileName = "Loading file..."
                showScanStartedDialog = true

                // Process file in background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val filePath = filePickerHelper.getFilePathFromUri(uri)
                        Log.d("FilePicker", "Extracted file path: $filePath")

                        if (filePath != null && File(filePath).exists()) {
                            // Get file name for display
                            selectedFileName = File(filePath).name

                            // Use CleanerViewModel to start scan
                            val file = File(filePath)
                            cleanerViewModel.startScan(listOf(file), context)

                        } else {
                            // Try to create a temp file from the content URI
                            val tempFile = filePickerHelper.getFileFromContentUri(uri)
                            if (tempFile != null && tempFile.exists()) {
                                selectedFileName = tempFile.name

                                // Use CleanerViewModel to start scan
                                cleanerViewModel.startScan(listOf(tempFile), context)

                            } else {
                                errorMessage = "Could not access the selected file. Please try another file."
                                showErrorDialog = true
                                showScanStartedDialog = false
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error processing file: ${e.message}"
                        showErrorDialog = true
                        showScanStartedDialog = false
                    }
                }
            }
        } else {
            Log.d("FilePicker", "File picker cancelled or failed")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedPermissions) {
            // Permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    100
                )
            }

            viewModel.loadWhatsAppData(base)
            hasRequestedPermissions = true
        }
    }

    // Scan Progress Dialog (FileScanScreen jaisa)
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = {
                // Only allow dismiss if scan is completed
                if (scanCompleted) {
                    cleanerViewModel.setShowScanDialog(false)
                    cleanerViewModel.resetScanState()
                }
            },
            title = {
                Column {
                    Text("Scanning Files", color = Color.White)
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
                    Text("Scanned $scannedFiles of $totalFiles files", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = if (totalFiles > 0) scannedFiles.toFloat() / totalFiles else 0f,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF3DDC84)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "VirusTotal Progress: ${scanProgress}%",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cleanerViewModel.setShowScanDialog(false)
                        cleanerViewModel.resetScanState()
                    },
                    enabled = scanCompleted || !scanCompleted // Always enabled
                ) {
                    Text(if (scanCompleted) "Close" else "Dismiss", color = Color.White)
                }
            },
            dismissButton = {
                if (!scanCompleted) {
                    TextButton(
                        onClick = {
                            cleanerViewModel.cancelScan() // This should send the cancel broadcast
                        }
                    ) {
                        Text("Cancel Scan", color = Color.Red)
                    }
                }
            },
            containerColor = Color(0xFF1E1E1E),
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }

    // Show scan started dialog
    if (showScanStartedDialog && !showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanStartedDialog = false },
            title = {
                Text(text = "Preparing Scan", color = Color.White)
            },
            text = {
                Text(
                    text = "Preparing file: $selectedFileName",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = { showScanStartedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84))
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }

    // Show error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(text = "Error", color = Color.White)
            },
            text = {
                Text(text = errorMessage, color = Color.White)
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84))
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            "Cleaner",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            "Scan and clean WhatsApp files easily",
            fontSize = 13.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CleanerCard(navController)
        ScanCard(
            navController = navController,
            scannedFilesCount = 3,
            lastScanTime = "",
            onScanClicked = {
                // Open file picker when ScanCard is clicked
                if (hasRequestedPermissions) {
                    filePickerHelper.openFilePicker( filePickerLauncher)
                }
            }
        )



        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF25D366), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_fill_whatsapp_icon),
                                contentDescription = "WhatsApp",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "WhatsApp Storage",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                "Clear out media you no longer need.",
                                fontSize = 12.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                if (isExpanded) {


                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF25D366))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FileRow("Images", viewModel.images, "image") { files, type ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "files",
                                    files
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "type",
                                    type
                                )
                                navController.navigate("full_file_screen")
                            }

                            FileRow("Videos", viewModel.videos, "video") { files, type ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "files",
                                    files
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "type",
                                    type
                                )
                                navController.navigate("full_file_screen")
                            }

                            FileRow("Documents", viewModel.documents, "doc") { files, type ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "files",
                                    files
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "type",
                                    type
                                )
                                navController.navigate("full_file_screen")
                            }

                            FileRow("Stickers", viewModel.stickers, "image") { files, type ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "files",
                                    files
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "type",
                                    type
                                )
                                navController.navigate("full_file_screen")
                            }
                        }


                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("clean_whatsapp_media")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .align(Alignment.BottomCenter)
                            .shadow(8.dp, RoundedCornerShape(20.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_file_search_icon),
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Clean WhatsApp Media",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        UnusedAppsCard(
            onClick = {

                navController.navigate("unused_apps_screen")
            },

        )
        ContactCleanerCard (
            onClick = {

                navController.navigate("contact_cleaner_screen")

            },
        )
    }
}
class FilePickerHelper(private val context: Context) {

    fun openFilePicker(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }

        launcher.launch(intent)
    }

    fun getFilePathFromUri(uri: Uri): String? {
        Log.d("FilePicker", "Getting path for URI: $uri")

        try {
            return when {
                DocumentsContract.isDocumentUri(context, uri) -> {
                    getFilePathFromDocumentUri(uri)
                }
                "content".equals(uri.scheme, ignoreCase = true) -> {
                    getFilePathFromContentUri(uri) ?: getFileFromContentUri(uri)?.absolutePath
                }
                "file".equals(uri.scheme, ignoreCase = true) -> {
                    uri.path
                }
                else -> {
                    getFileFromContentUri(uri)?.absolutePath
                }
            }
        } catch (e: Exception) {
            Log.e("FilePicker", "Error getting file path: ${e.message}")
            return null
        }
    }

    private fun getFilePathFromDocumentUri(uri: Uri): String? {
        try {
            val docId = DocumentsContract.getDocumentId(uri)
            Log.d("FilePicker", "Document ID: $docId")

            val split = docId.split(":").toTypedArray()
            if (split.size >= 2) {
                val type = split[0]
                val path = split[1]

                return if ("primary".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().toString() + "/" + path
                } else {
                    // Handle non-primary storage
                    "/storage/$type/$path"
                }
            }
        } catch (e: Exception) {
            Log.e("FilePicker", "Error parsing document URI: ${e.message}")
        }
        return null
    }

    private fun getFilePathFromContentUri(uri: Uri): String? {
        var filePath: String? = null
        try {
            val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    filePath = cursor.getString(columnIndex)

                    // If DATA column is null, try using DISPLAY_NAME
                    if (filePath.isNullOrEmpty()) {
                        val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        val fileName = cursor.getString(nameIndex)
                        if (!fileName.isNullOrEmpty()) {
                            filePath = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FilePicker", "Error getting file path from content URI: ${e.message}")
        }
        return filePath
    }

    // Alternative method to get file from content URI
    fun getFileFromContentUri(uri: Uri): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = getFileNameFromUri(uri) ?: "scan_file_${System.currentTimeMillis()}"
            val tempFile = File(context.cacheDir, fileName)

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("FilePicker", "Created temp file: ${tempFile.absolutePath}")
            return tempFile
        } catch (e: Exception) {
            Log.e("FilePicker", "Error creating temp file: ${e.message}")
            return null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FilePicker", "Error getting file name: ${e.message}")
        }
        return fileName
    }
}

// Function to start file scan (without navigation)
fun startFileScan(context: Context, filePath: String?) {
    Log.d("FileScan", "Starting scan with file: $filePath")

    val intent = Intent(context, VirusTotalScanService::class.java).apply {
        if (filePath != null) {
            putExtra("files", arrayOf(filePath))
            Log.d("FileScan", "Added file to intent: $filePath")
        } else {
            putExtra("files", emptyArray<String>())
            Log.d("FileScan", "No file path provided")
        }
        putExtra("scanMode", "SINGLE")
    }
    ContextCompat.startForegroundService(context,intent)
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context,intent)
            Log.d("FileScan", "Started foreground service successfully")
        } else {
            context.startService(intent)
            Log.d("FileScan", "Started regular service successfully")
        }
        Toast.makeText(context, "Scan started", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("FileScan", "Failed to start service: ${e.message}")
        Toast.makeText(context, "Failed to start scan: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// Update your ScanCard to use the proper function
@Composable
fun ScanCard(
    navController: NavController,
    scannedFilesCount: Int = 0,
    lastScanTime: String = "Never",
    onScanClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onScanClicked() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "You scanned $scannedFilesCount files",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )

                Text(
                    text = "Last Scan: $lastScanTime",
                    fontSize = 14.sp,
                    color = Color(0xFFBBBBBB)
                )

                Text(
                    text = "Scan all your important files safely",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onScanClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84))
            ) {
                Text(text = "Quick Scan", color = Color.White)
            }
        }
    }
}

@Composable
fun QuickScanButton(
    progress: Float, // 0f..1f
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(110.dp)
            .clickable { onClick() }
    ) {
        // Outer Circular Progress
        CircularProgressIndicator(
            progress = progress,
            strokeWidth = 8.dp,
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF3DDC84), // highlight color
            trackColor = Color(0xFF2A2A2A)
        )

        // Inner Circle Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFF3A3F77), CircleShape)
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
fun CleanerCard(
    navController: NavController
) {
    val storageInfo = remember { getStorageInfo() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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

                // Storage percentages with color highlight
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Free: ",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${storageInfo.freePercent}%",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Used: ",
                        fontSize = 14.sp,
                        color = Color(0xFFF44336), // Red
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${storageInfo.usedPercent}%",
                        fontSize = 14.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Total: ${formatSize(storageInfo.totalBytes)} | Free: ${formatSize(storageInfo.freeBytes)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Quick Scan with Circular Progress
            QuickScanButton(
                progress = storageInfo.freePercent / 100f,
                onClick = { navController.navigate("file_scan") }
            )
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

@Composable
fun UnusedAppsCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(18.dp)
        ) {
            // 🔹 Icon ke liye round background
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Unused Apps",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Unused Apps",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Find and clean apps unused for 2–6 months",
                    color = Color(0xFFB0BEC5),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ContactCleanerCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            // 🔹 Stylish gradient icon background
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF42A5F5), Color(0xFF1565C0)) // Blue gradient
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Contacts, // 👤 Contacts icon
                    contentDescription = "Contact Cleaner",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Contact Cleaner",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Merge duplicates & remove unused contacts easily",
                    color = Color(0xFFB0BEC5),
                    fontSize = 14.sp
                )
            }
        }
    }
}



