package com.privacyshield.android.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class AppPermission(
    val name: String,
    val isGranted: Boolean,
    val isDangerous: Boolean,
    val isDeclared:  Boolean
): Parcelable
