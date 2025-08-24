package com.privacyshield.android.Model

import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.compose.ui.graphics.painter.Painter


import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue



@Parcelize
data class AppDetail(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val sourceDir: String,
    val isSystemApp: Boolean,
    val isFromPlayStore :Boolean,
    val permissions: List<AppPermission>,
    val isOemApp: Boolean,
    val isSideloaded: Boolean,
    val hasInternetPermission: Boolean,
    val sharedUserId: String?,
    val isCloned: Boolean,
    val isActiveProfile: Boolean,
    val isManagedProfile: Boolean,
    val hasCustomBatterySetting: Boolean,
    val isAccessibilityService: Boolean,

    ): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppDetail

        if (appName != other.appName) return false
        if (packageName != other.packageName) return false
        if (versionName != other.versionName) return false
        if (versionCode != other.versionCode) return false
        if (minSdk != other.minSdk) return false
        if (targetSdk != other.targetSdk) return false
        if (compileSdk != other.compileSdk) return false
        if (firstInstallTime != other.firstInstallTime) return false
        if (lastUpdateTime != other.lastUpdateTime) return false
        if (sourceDir != other.sourceDir) return false
        if (isSystemApp != other.isSystemApp) return false
        if (isFromPlayStore != other.isFromPlayStore) return false
        if (permissions != other.permissions) return false
        if (isOemApp != other.isOemApp) return false
        if (isSideloaded != other.isSideloaded) return false
        if (hasInternetPermission != other.hasInternetPermission) return false
        if (sharedUserId != other.sharedUserId) return false
        if (isCloned != other.isCloned) return false
        if (isActiveProfile != other.isActiveProfile) return false
        if (isManagedProfile != other.isManagedProfile) return false
        if (hasCustomBatterySetting != other.hasCustomBatterySetting) return false
        if (isAccessibilityService != other.isAccessibilityService) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appName.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + versionName.hashCode()
        result = 31 * result + versionCode.hashCode()
        result = 31 * result + minSdk
        result = 31 * result + targetSdk
        result = 31 * result + compileSdk
        result = 31 * result + firstInstallTime.hashCode()
        result = 31 * result + lastUpdateTime.hashCode()
        result = 31 * result + sourceDir.hashCode()
        result = 31 * result + isSystemApp.hashCode()
        result = 31 * result + isFromPlayStore.hashCode()
        result = 31 * result + permissions.hashCode()
        result = 31 * result + isOemApp.hashCode()
        result = 31 * result + isSideloaded.hashCode()
        result = 31 * result + hasInternetPermission.hashCode()
        result = 31 * result + (sharedUserId?.hashCode() ?: 0)
        result = 31 * result + isCloned.hashCode()
        result = 31 * result + isActiveProfile.hashCode()
        result = 31 * result + isManagedProfile.hashCode()
        result = 31 * result + hasCustomBatterySetting.hashCode()
        result = 31 * result + isAccessibilityService.hashCode()
        return result
    }
}

