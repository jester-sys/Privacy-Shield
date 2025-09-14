package com.privacyshield.android.Component.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import androidx.room.*

@Dao
interface TrashFileDao {

    @Query("SELECT * FROM trash_files ORDER BY addedAt DESC")
    suspend fun getAllTrashFiles(): List<TrashFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: TrashFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<TrashFile>)

    @Delete
    suspend fun delete(file: TrashFile)

    @Query("DELETE FROM trash_files WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM trash_files WHERE addedAt < :timeLimit")
    suspend fun deleteOldFiles(timeLimit: Long)
}
