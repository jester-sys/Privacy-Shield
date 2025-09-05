package com.privacyshield.android.Component.Scanner.Whatsapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class WhatsAppData(
    val totalSize: Long,
    val totalFiles: Int,
    val categories: @RawValue List<FileCategory>,
    val videos: @RawValue List<FileCategory> = emptyList(),
    val documents: @RawValue List<FileCategory> = emptyList(),
    val stickers: @RawValue List<FileCategory> = emptyList()
) : Parcelable
