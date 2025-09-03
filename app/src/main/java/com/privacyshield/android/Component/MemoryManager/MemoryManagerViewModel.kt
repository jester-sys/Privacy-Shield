package com.privacyshield.android.Component.MemoryManager

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyshield.android.Component.MemoryManager.model.CategoryItem
import com.privacyshield.android.Component.MemoryManager.usecases.getDocumentsSize
import com.privacyshield.android.Component.MemoryManager.usecases.getDownloadsSize
import com.privacyshield.android.Component.MemoryManager.usecases.getImagesSize
import com.privacyshield.android.Component.MemoryManager.usecases.getInstalledAppsSize
import com.privacyshield.android.Component.MemoryManager.usecases.getMusicSize
import com.privacyshield.android.Component.MemoryManager.usecases.getStorageInfo
import com.privacyshield.android.Component.MemoryManager.usecases.getSystemSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryManagerViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories

    private val _storageInfo = MutableStateFlow(Triple(0L, 0L, 0L)) // total, used, free
    val storageInfo: StateFlow<Triple<Long, Long, Long>> = _storageInfo

    init {
        loadStorageInfo()
        loadCategories()
    }

    private fun loadStorageInfo() {
        val (total, used, free) = getStorageInfo()
        _storageInfo.value = Triple(total, used, free)
    }

    private fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = listOf(
                CategoryItem("Installed apps", getInstalledAppsSize(app), Icons.Filled.Apps),
                CategoryItem("System", getSystemSize(), Icons.Filled.Settings),
                CategoryItem("Music", getMusicSize(app), Icons.Filled.MusicNote),
                CategoryItem("Images", getImagesSize(app), Icons.Filled.Image),
                CategoryItem("Documents", getDocumentsSize(app), Icons.Filled.Description),
                CategoryItem("Downloads", getDownloadsSize(), Icons.Filled.FileDownload),
                CategoryItem("Other Files", "Coming Soon", Icons.Filled.Folder)
            )
            _categories.value = updated
        }
    }
}
