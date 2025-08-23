package com.privacyshield.android.Model

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class AppDetail(
    val appName: String,
    val packageName: String,
    val icon: @RawValue Drawable,
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

): Parcelable

