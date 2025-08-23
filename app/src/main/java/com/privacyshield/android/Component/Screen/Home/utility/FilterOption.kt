package com.privacyshield.android.Component.Screen.Home.utility

enum class FilterType(val label: String) {
    ALL_APPS("All Apps"),
    SYSTEM_APPS("System Apps"),
    USER_APPS("User Apps"),
    GOOGLE_PLAY("Google Play Store"),
    OEM_APPS("OEM Apps"),
    SIDELOADED("Sideloaded Apps"),
    NO_INTERNET("Apps Without Internet Access"),
    SHARED_USER_ID("Shared User ID"),
    CLONED("Cloned Apps"),
    ACTIVE_PROFILE("Active Profile"),
    MANAGED_PROFILE("Managed Profile(s)"),
    CUSTOM_BATTERY("Custom Battery Option"),
    ACCESSIBILITY("Accessibility Services")
}

enum class SortType(val label: String) {
    APP_NAME("App Name"),
    GRANTED_PERMISSIONS("Granted Permissions"),
    REQUESTED_PERMISSIONS("Requested Permissions"),
    DECLARED_PERMISSIONS("Declared Permissions"),
    DANGEROUS_PERMISSIONS("Dangerous Permissions"),
    INSTALL_DATE("Install Date"),
     LAST_UPDATE("Last Update"),
    INSTALL_SOURCE("Install Source")
}
