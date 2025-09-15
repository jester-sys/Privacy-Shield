package com.privacyshield.android.Component.Scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.privacyshield.android.Component.Scanner.QuickScan.Helper.scanCacheFiles
import com.privacyshield.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val base = getWhatsAppBasePath()

    var hasRequestedPermissions by remember { mutableStateOf(false) }

    var isExpanded by rememberSaveable { mutableStateOf(true) }


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
        ScanCard(navController,3,"")



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
                        color = Color(0xFF4CAF50), // Green
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

@Composable
fun ScanCard(
    navController: NavController,
    scannedFilesCount: Int = 0,
    lastScanTime: String = "Never"
) {
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
                onClick = { navController.navigate("scan_files_apps") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84))
            ) {
                Text(text = "Quick Scan", color = Color.White)
            }
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



