package com.privacyshield.android.Component.Scanner

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    var images by mutableStateOf<List<File>>(emptyList())
        private set
    var videos by mutableStateOf<List<File>>(emptyList())
        private set
    var documents by mutableStateOf<List<File>>(emptyList())
        private set
    var stickers by mutableStateOf<List<File>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var totalSize by mutableStateOf(0L)
        private set

    private fun calculateTotalSize(): Long {
        return images.sumOf { it.length() } +
                videos.sumOf { it.length() } +
                documents.sumOf { it.length() } +
                stickers.sumOf { it.length() }
    }

    fun loadWhatsAppData(base: String) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val img = fetchFilesFromPossibleFolders(
                listOf("$base/WhatsApp Images", "$base/WhatsApp Image"),
                listOf(".jpg", ".jpeg", ".png", ".webp")
            )
            val vid = fetchFilesFromPossibleFolders(
                listOf("$base/WhatsApp Video", "$base/WhatsApp Videos"),
                listOf(".mp4", ".3gp", ".mkv", ".avi")
            )
            val docs = fetchFilesFromPossibleFolders(
                listOf("$base/WhatsApp Documents", "$base/WhatsApp Document"),
                listOf(".pdf", ".docx", ".doc", ".pptx", ".ppt", ".xls", ".xlsx", ".txt")
            )
            val stick = fetchFilesFromPossibleFolders(
                listOf("$base/WhatsApp Stickers", "$base/WhatsApp Sticker"),
                listOf(".webp")
            )

            withContext(Dispatchers.Main) {
                images = img
                videos = vid
                documents = docs
                stickers = stick
                totalSize = calculateTotalSize()
                isLoading = false
            }
        }
    }
}
