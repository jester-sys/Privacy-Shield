package com.privacyshield.android.Component.Settings.Trash

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyshield.android.Component.Scanner.QuickScan.ScanningState
import com.privacyshield.android.Component.db.TrashFile
import com.privacyshield.android.Component.db.TrashFileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val dao: TrashFileDao
) : ViewModel() {

    private val _trashFiles = MutableStateFlow<List<TrashFile>>(emptyList())
    val trashFiles: StateFlow<List<TrashFile>> = _trashFiles

    private val _selectedFiles = MutableStateFlow<Set<TrashFile>>(emptySet())
    val selectedFiles: StateFlow<Set<TrashFile>> = _selectedFiles

    init {
        viewModelScope.launch {
            cleanupOldFiles()
            loadTrashFiles()
        }
    }

    private suspend fun loadTrashFiles() {
        _trashFiles.value = dao.getAllTrashFiles()
    }

    // 🔹 Delete files older than 24h
    private suspend fun cleanupOldFiles() {
        val expiryTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        dao.deleteOldFiles(expiryTime)
    }

    // 🔹 Move files to trash
    fun moveToTrash(files: Set<File>) {
        viewModelScope.launch {
            val trashEntities = files.map {
                TrashFile(
                    path = it.path,
                    name = it.name,
                    size = it.length(),
                    addedAt = System.currentTimeMillis()
                )
            }
            dao.insertAll(trashEntities)
            loadTrashFiles()
        }
    }

    // 🔹 Selection handling
    fun toggleSelection(file: TrashFile) {
        val newSet = _selectedFiles.value.toMutableSet()
        if (!newSet.add(file)) newSet.remove(file)
        _selectedFiles.value = newSet
    }

    fun selectAll() {
        _selectedFiles.value = _trashFiles.value.toSet()
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }

    // 🔹 Restore files
    fun restoreFiles(filesToRestore: List<TrashFile>) {
        viewModelScope.launch {
            filesToRestore.forEach { dao.delete(it) }
            loadTrashFiles()
            clearSelection()
        }
    }

    // 🔹 Permanently delete files
    fun deleteFilesPermanently(filesToDelete: List<TrashFile>) {
        viewModelScope.launch {
            filesToDelete.forEach {
                File(it.path).delete()
                dao.delete(it)
            }
            loadTrashFiles()
            clearSelection()
        }
    }
}
