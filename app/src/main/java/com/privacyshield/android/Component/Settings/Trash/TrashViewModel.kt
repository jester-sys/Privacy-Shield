package com.privacyshield.android.Component.Settings.Trash

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import com.privacyshield.android.Component.Scanner.QuickScan.ScanningState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor() : ViewModel() {

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult

    private val _scanningState = MutableStateFlow(ScanningState.IDLE)
    val scanningState: StateFlow<ScanningState> = _scanningState

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress

    // 🔹 Trash list
    private val _trashFiles = MutableStateFlow<List<File>>(emptyList())
    val trashFiles: StateFlow<List<File>> = _trashFiles

    fun quickScan() {
        // Your scan logic
    }

    fun clearResults() {
        _scanResult.value = null
    }

    // 🔹 Move files to trash
    fun moveToTrash(files: Set<File>) {
        _trashFiles.value = _trashFiles.value + files
    }

    // 🔹 Delete files permanently
    fun deleteFiles(files: Set<File>) {
        files.forEach { it.delete() }
    }
}
