package com.privacyshield.android.Component.Screen.Home

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyshield.android.Component.Screen.Home.utility.FilterType
import com.privacyshield.android.Component.Screen.Home.utility.SortType
import com.privacyshield.android.Component.Screen.Home.utility.applyFiltersEnum
import com.privacyshield.android.Component.Screen.Home.utility.applySortingEnum

import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.ViewModel.HomeViewModel
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onAppClick: (AppDetail, List<AppDetail>) -> Unit,   // 👈 yahan update
    activity: Activity
) {
    val apps = homeViewModel.appList
    val loading = homeViewModel.isLoading

    var searchQuery by remember { mutableStateOf("") }

    var filterSheetOpen by remember { mutableStateOf(false) }
    var sortSheetOpen by remember { mutableStateOf(false) }

    val selectedFilters = remember { mutableStateListOf<FilterType>() }
    val pendingFilters = remember { mutableStateListOf<FilterType>() }

    var selectedSort by remember { mutableStateOf(SortType.APP_NAME) }

    // 🔎 Filtering + sorting
    var filteredApps = apps.filter { it.appName.contains(searchQuery, ignoreCase = true) }

    filteredApps = applyFiltersEnum(apps, selectedFilters.toList(), searchQuery)
    filteredApps = applySortingEnum(filteredApps, selectedSort)

    Column(
        Modifier.fillMaxSize().background(Color(0xFF1E1E1E))
    ) {
        SearchBarWithActions(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onFilterClick = {
                pendingFilters.clear()
                pendingFilters.addAll(selectedFilters)
                filterSheetOpen = true
            },
            onSortClick = { sortSheetOpen = true },
            filteredSize = filteredApps.size,
            totalSize = apps.size
        )

        AppList(
            activity = activity,
            loading = loading,
            apps = filteredApps,
            onAppClick = { app ->
                onAppClick(app, apps)   // 👈 dono bhej diye
            },
            onAction = { app, action ->
                when (action) {
                    "Open" -> {}
                    "Uninstall" -> {}
                    "Share" -> {}
                    "Details" -> onAppClick(app, apps)   // 👈 yahan bhi
                }
            }
        )
    }

    if (filterSheetOpen) {
        FilterBottomSheet(
            filterOptions = FilterType.values().toList(),
            pendingFilters = pendingFilters,
            onCancel = { filterSheetOpen = false },
            onApply = {
                selectedFilters.clear()
                selectedFilters.addAll(pendingFilters)
                filterSheetOpen = false
            }
        )
    }

    if (sortSheetOpen) {
        SortBottomSheet(
            sortOptions = SortType.values().toList(),
            selectedSort = selectedSort,
            onSelect = {
                selectedSort = it
                sortSheetOpen = false
            }
        )
    }
}
