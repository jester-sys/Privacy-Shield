package com.privacyshield.android.Component.Scanner.CONTACTS

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCleanerScreen(
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsState()
    val duplicates by viewModel.duplicates.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            val readOk = perms[Manifest.permission.READ_CONTACTS] == true
            if (readOk) viewModel.loadContactsAfterPermissionGranted()
            else Toast.makeText(context, "Contacts permission required", Toast.LENGTH_SHORT).show()
        }
    )

    LaunchedEffect(Unit) {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!readGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        } else {
            viewModel.loadContactsAfterPermissionGranted()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Cleaner") },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selected.isEmpty()) {
                        Toast.makeText(context, "Select contacts to delete", Toast.LENGTH_SHORT).show()
                    } else {
                        showDeleteDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 🔹 Search & Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearch(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search contacts...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                )
                IconButton(onClick = { viewModel.toggleSortOrder() }) {
                    Icon(
                        if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowDownward
                        else Icons.Default.ArrowUpward,
                        contentDescription = "Sort"
                    )
                }
            }

            // 🔹 Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val file = File(context.getExternalFilesDir(null), "contacts.csv")
                            val success = viewModel.exportContactsToCSV(file)
                            Toast.makeText(
                                context,
                                if (success) "Exported to ${file.absolutePath}" else "Export failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Export")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val merged = viewModel.mergeDuplicates()
                            Toast.makeText(context, "Merged $merged duplicates", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.Merge, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Merge")
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    duplicates.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No duplicate contacts found", color = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            Text("${contacts.size} contacts scanned", color = Color.LightGray)
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Duplicate groups",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            duplicates.forEach { (phone, group) ->
                                item(key = phone) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Shared number: $phone",
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${group.size} contacts",
                                                        color = Color.Gray,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                TextButton(onClick = { viewModel.selectAllInGroup(phone) }) {
                                                    Text("Select all")
                                                }
                                            }

                                            Spacer(Modifier.height(8.dp))

                                            group.forEach { contact ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    Checkbox(
                                                        checked = selected.contains(contact.id),
                                                        onCheckedChange = { viewModel.toggleSelect(contact.id) }
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Column {
                                                        Text(text = contact.name ?: "(no name)")
                                                        Text(
                                                            text = contact.numbers.joinToString(", "),
                                                            color = Color.Gray,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }

                // Delete dialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete selected contacts?") },
                        text = {
                            Text("This will permanently delete ${selected.size} contact(s).")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showDeleteDialog = false
                                deleting = true
                                coroutineScope.launch {
                                    val (deleted, failed) = viewModel.deleteSelected()
                                    deleting = false
                                    Toast.makeText(
                                        context,
                                        "Deleted: $deleted, Failed: $failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                if (deleting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Deleting...", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

