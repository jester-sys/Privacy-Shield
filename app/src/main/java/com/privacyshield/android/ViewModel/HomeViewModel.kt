package com.privacyshield.android.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.Repo.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
): ViewModel() {

    var appList by mutableStateOf<List<AppDetail>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            isLoading = true
            appList = repository.getInstalledApps()
            isLoading = false
        }
    }
}
