package com.privacyshield.android.Component.db


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity(tableName = "trash_files")
data class TrashFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String,
    val name: String,
    val size: Long,
    val addedAt: Long = System.currentTimeMillis() // ✅ Save insert time
)
