package com.privacyshield.android.Component.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [TrashFile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trashFileDao(): TrashFileDao
}
