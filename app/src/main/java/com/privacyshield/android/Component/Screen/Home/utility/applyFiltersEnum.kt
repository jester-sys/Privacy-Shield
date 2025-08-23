package com.privacyshield.android.Component.Screen.Home.utility

import com.privacyshield.android.Model.AppDetail


 fun applyFiltersEnum(
    allApps: List<AppDetail>,
    selectedFilters: List<FilterType>,
    searchQuery: String
): List<AppDetail>
{

    if (selectedFilters.size == 1 && selectedFilters.contains(FilterType.ALL_APPS)) {
        return allApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }


    var filtered = allApps

    selectedFilters.forEach { filter ->
        filtered = when (filter) {
            FilterType.ALL_APPS -> filtered
            FilterType.SYSTEM_APPS -> filtered.filter { it.isSystemApp }
            FilterType.USER_APPS -> filtered.filter { !it.isSystemApp }
            FilterType.GOOGLE_PLAY -> filtered.filter { it.packageName == "com.android.vending" }
            FilterType.OEM_APPS -> filtered.filter { it.isOemApp }
            FilterType.SIDELOADED -> filtered.filter { it.isSideloaded }
            FilterType.NO_INTERNET -> filtered.filter { !it.hasInternetPermission }
            FilterType.SHARED_USER_ID -> filtered.filter { it.sharedUserId != null }
            FilterType.CLONED -> filtered.filter { it.isCloned }
            FilterType.ACTIVE_PROFILE -> filtered.filter { it.isActiveProfile }
            FilterType.MANAGED_PROFILE -> filtered.filter { it.isManagedProfile }
            FilterType.CUSTOM_BATTERY -> filtered.filter { it.hasCustomBatterySetting }
            FilterType.ACCESSIBILITY -> filtered.filter { it.isAccessibilityService }
        }
    }

    return filtered.filter { it.appName.contains(searchQuery, ignoreCase = true) }
}


fun applySortingEnum(
    apps: List<AppDetail>,
    selectedSort: SortType
): List<AppDetail> {
    return when (selectedSort) {
        SortType.APP_NAME -> apps.sortedBy { it.appName.lowercase() }
        SortType.GRANTED_PERMISSIONS -> apps.sortedByDescending { app ->
            app.permissions.count { it.isGranted }
        }
        SortType.REQUESTED_PERMISSIONS -> apps.sortedByDescending { app ->
            app.permissions.size
        }
        SortType.DECLARED_PERMISSIONS -> apps.sortedByDescending { app ->
            app.permissions.count { it.isDeclared }
        }
        SortType.DECLARED_PERMISSIONS -> apps.sortedByDescending { app ->
            app.permissions.count { it.isDeclared }
        }
        SortType.DANGEROUS_PERMISSIONS -> apps.sortedByDescending { app ->
            app.permissions.count { it.isDangerous }
        }


        SortType.INSTALL_DATE -> apps.sortedBy { it.firstInstallTime }
        SortType.LAST_UPDATE -> apps.sortedByDescending { it.lastUpdateTime }
        SortType.INSTALL_SOURCE -> apps.sortedBy { it.sourceDir }
    }
}

