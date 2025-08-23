package com.privacyshield.android.Component.Screen.Home

import com.privacyshield.android.Model.AppDetail

fun applyFilters(
    apps: List<AppDetail>,
    allApps: List<AppDetail>,
    selectedFilters: List<String>,
    searchQuery: String
): List<AppDetail> {
    var filtered = apps
    when {
        "System Apps" in selectedFilters -> filtered = filtered.filter { it.isSystemApp }
        "User Apps" in selectedFilters -> filtered = filtered.filter { !it.isSystemApp }
        "All Apps" in selectedFilters || selectedFilters.isEmpty() ->
            filtered = allApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        "Google Play Store" in selectedFilters -> filtered = filtered.filter { it.packageName == "com.android.vending" }
        "OEM Apps" in selectedFilters -> filtered = filtered.filter { it.isOemApp }
        "Sideloaded Apps" in selectedFilters -> filtered = filtered.filter { it.isSideloaded }
        "Apps Without Internet Access" in selectedFilters -> filtered = filtered.filter { !it.hasInternetPermission }
        "Shared User ID" in selectedFilters -> filtered = filtered.filter { it.sharedUserId != null }
        "Cloned Apps" in selectedFilters -> filtered = filtered.filter { it.isCloned }
        "Active Profile" in selectedFilters -> filtered = filtered.filter { it.isActiveProfile }
        "Managed Profile(s)" in selectedFilters -> filtered = filtered.filter { it.isManagedProfile }
        "Custom Battery Option" in selectedFilters -> filtered = filtered.filter { it.hasCustomBatterySetting }
        "Accessibility Services" in selectedFilters -> filtered = filtered.filter { it.isAccessibilityService }
    }
    return filtered
}

fun applySorting(apps: List<AppDetail>, selectedSort: String): List<AppDetail> {
    return when (selectedSort) {
        "App Name" -> apps.sortedBy { it.appName.lowercase() }
        "Granted Permissions" -> apps.sortedByDescending { it.permissions.count { p -> p.isGranted } }
        "Requested Permissions" -> apps.sortedByDescending { it.permissions.size }
        "Declared Permissions" -> apps.sortedBy { it.permissions.size }
        "Install Date" -> apps.sortedBy { it.firstInstallTime }
        "Last Update" -> apps.sortedByDescending { it.lastUpdateTime }
        "Install Source" -> apps.sortedBy { it.sourceDir }
        else -> apps
    }
}
