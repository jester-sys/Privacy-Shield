package com.privacyshield.android.Component.Scanner.Whatsapp

import android.app.Application
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


// ✅ ViewModel with real-time updates
@HiltViewModel
class WhatsAppCleanerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _totalSize = MutableStateFlow(0L)
    val totalSize: StateFlow<Long> = _totalSize

    private val _totalFiles = MutableStateFlow(0)
    val totalFiles: StateFlow<Int> = _totalFiles

    private val _categories = MutableStateFlow<List<FileCategory>>(emptyList())
    val categories: StateFlow<List<FileCategory>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var fileObserver: FileObserver? = null
    private val _cleanProgress = MutableStateFlow(0)
    val cleanProgress: StateFlow<Int> = _cleanProgress



    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadWhatsAppData()
            setupFileObserver()
        }
    }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            loadWhatsAppData()
            _isLoading.value = false
        }
    }




    private suspend fun loadWhatsAppData() {
        val baseDirs = arrayOf(
            File(Environment.getExternalStorageDirectory(), "WhatsApp/Media"),
            File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp/Media")
        )




        val cats = listOf(
            Triple("Images", "WhatsApp Images", Icons.Default.Image),
            Triple("Videos", "WhatsApp Video", Icons.Default.Videocam),
            Triple("Documents", "WhatsApp Documents", Icons.Default.Description),
            Triple("Statuses", ".Statuses", Icons.Default.Feed),
            Triple("Voice Notes", "WhatsApp Voice Notes", Icons.Default.Mic),
            Triple("GIFs", "WhatsApp Animated Gifs", Icons.Default.Gif),
            Triple("Stickers", "WhatsApp Stickers", Icons.Default.StickyNote2)
        )

        val categoriesList = mutableListOf<FileCategory>()
        var totalSizeAcc = 0L
        var totalFilesAcc = 0

        for ((name, folderName, icon) in cats) {
            var folderSize = 0L
            var fileCount = 0

            // Check both possible WhatsApp directories
            for (baseDir in baseDirs) {
                val folder = File(baseDir, folderName)
                if (folder.exists() && folder.isDirectory) {
                    val (size, count) = getFolderSize(folder)
                    folderSize += size
                    fileCount += count
                }
            }

            if (fileCount > 0) {
                categoriesList.add(FileCategory(name, folderSize, fileCount, icon))
                totalSizeAcc += folderSize
                totalFilesAcc += fileCount
            }
        }

        withContext(Dispatchers.Main) {
            _categories.value = categoriesList
            _totalSize.value = totalSizeAcc
            _totalFiles.value = totalFilesAcc
            _isLoading.value = false
        }
    }

    private fun getFolderSize(dir: File): Pair<Long, Int> {
        var size = 0L
        var count = 0

        try {
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    size += file.length()
                    count++
                } else if (file.isDirectory) {
                    val (subSize, subCount) = getFolderSize(file)
                    size += subSize
                    count += subCount
                }
            }
        } catch (e: SecurityException) {
            Log.e("WhatsAppCleaner", "Permission denied for: ${dir.absolutePath}")
        }

        return size to count
    }

    private fun setupFileObserver() {
        val baseDir = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media")
        if (baseDir.exists()) {
            fileObserver = object : FileObserver(baseDir.absolutePath, ALL_EVENTS) {
                override fun onEvent(event: Int, path: String?) {
                    // Refresh data when files change
                    if (event == CREATE || event == DELETE || event == MODIFY) {
                        refreshData()
                    }
                }
            }
            fileObserver?.startWatching()
        }
    }

    fun cleanFilesByCategories(selectedCategories: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val baseDirs = arrayOf(
                File(Environment.getExternalStorageDirectory(), "WhatsApp/Media"),
                File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp/Media")
            )

            selectedCategories.forEach { categoryName ->
                for (baseDir in baseDirs) {
                    val folder = when (categoryName) {
                        "Images" -> File(baseDir, "WhatsApp Images")
                        "Videos" -> File(baseDir, "WhatsApp Video")
                        "Documents" -> File(baseDir, "WhatsApp Documents")
                        "Statuses" -> File(baseDir, ".Statuses")
                        "Voice Notes" -> File(baseDir, "WhatsApp Voice Notes")
                        "GIFs" -> File(baseDir, "WhatsApp Animated Gifs")
                        "Stickers" -> File(baseDir, "WhatsApp Stickers")
                        else -> null
                    }

                    folder?.let { deleteFolderContents(it) }
                }
            }

            loadWhatsAppData()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }


    private fun deleteFolderContents(dir: File) {
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                } else if (file.isDirectory) {
                    deleteFolderContents(file)
                    file.delete()
                }
            }
        } catch (e: SecurityException) {
            Log.e("WhatsAppCleaner", "Permission denied to delete: ${dir.absolutePath}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        fileObserver?.stopWatching()
    }
}