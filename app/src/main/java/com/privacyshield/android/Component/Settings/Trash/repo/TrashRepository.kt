package com.privacyshield.android.Component.Settings.Trash.repo

import com.privacyshield.android.Component.db.TrashFile
import com.privacyshield.android.Component.db.TrashFileDao
import java.io.File

//class TrashRepository(private val dao: TrashFileDao) {
//    suspend fun addToTrash(files: List<File>) {
//        val trashFiles = files.map { file ->
//            TrashFile(path = file.absolutePath, name = file.name)
//        }
//        dao.insertFiles(trashFiles)
//    }
//
//    suspend fun getTrashFiles() = dao.getAllTrashFiles()
//}
