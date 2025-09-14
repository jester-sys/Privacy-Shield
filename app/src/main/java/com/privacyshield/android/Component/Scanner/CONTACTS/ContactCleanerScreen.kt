package com.privacyshield.android.Component.Scanner.CONTACTS

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var merging by remember { mutableStateOf(false) }
    var selectedPrimaryId by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current




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


    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duplicate Contacts Cleaner", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            if (selected.isNotEmpty()) {
                Surface(
                    shadowElevation = 6.dp,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (selected.size > 1) {
                            Button(onClick = { showMergeDialog = true }) {
                                Icon(Icons.Default.Merge, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Merge")
                            }
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                }
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
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearch(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search contacts...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { viewModel.toggleSortOrder() }) {
                    Icon(
                        if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowDownward
                        else Icons.Default.ArrowUpward,
                        contentDescription = "Sort"
                    )
                }
            }

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }

                duplicates.isEmpty() -> {
                    Box(Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("No duplicate contacts 🎉", fontWeight = FontWeight.Medium)
                            Text(
                                "${contacts.size} contacts scanned",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = "Found ${duplicates.size} duplicate groups",
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        duplicates.forEach { (phone, group) ->
                            val isExpanded = expandedGroups[phone] == true

                            item(key = phone) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clickable {
                                            expandedGroups[phone] = !isExpanded
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Text(
                                                    text = phone,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    text = "${group.size} duplicate contacts",
                                                    color = Color.Gray,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Icon(
                                                if (isExpanded) Icons.Default.KeyboardArrowUp
                                                else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null
                                            )
                                        }

                                        AnimatedVisibility(visible = isExpanded) {
                                            Column {
                                                Spacer(Modifier.height(8.dp))
                                                group.forEach { contact ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 6.dp)
                                                    ) {
                                                        Checkbox(
                                                            checked = selected.contains(contact.id),
                                                            onCheckedChange = {
                                                                viewModel.toggleSelect(
                                                                    contact.id
                                                                )
                                                            }
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = contact.name
                                                                    ?: "(no name)",
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            Text(
                                                                text = contact.numbers.joinToString(
                                                                    ", "
                                                                ),
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
                            }
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete selected contacts?") },
            text = { Text("This will permanently delete ${selected.size} contact(s).") },
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

    // Merge Dialog
    if (showMergeDialog) {
        val selectedContacts = contacts.filter { it.id in selected }

        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("Choose primary contact") },
            text = {
                Column {
                    selectedContacts.forEach { contact ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPrimaryId = contact.id }
                                .padding(4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedPrimaryId == contact.id),
                                onClick = { selectedPrimaryId = contact.id }
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(contact.name ?: "(no name)")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedPrimaryId == null) {
                        Toast.makeText(context, "Select a name", Toast.LENGTH_SHORT).show()
                    } else {
                        showMergeDialog = false
                        merging = true
                        coroutineScope.launch {
                            val merged = viewModel.mergeSelectedDuplicates(selectedPrimaryId!!)
                            merging = false
                            Toast.makeText(
                                context,
                                "Merged $merged duplicates",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }) { Text("Merge") }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Loading overlay
    if (deleting || merging) {
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
                Text(
                    if (deleting) "Deleting..." else "Merging...",
                    color = Color.White
                )
            }
        }
    }

}