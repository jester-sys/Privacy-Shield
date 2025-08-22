package com.privacyshield.android.ViewModel

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
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppDetail>>(emptyList())
    val apps: StateFlow<List<AppDetail>> = _apps

    init {
        viewModelScope.launch {
            _apps.value = repository.getInstalledApps()
        }
    }
}
