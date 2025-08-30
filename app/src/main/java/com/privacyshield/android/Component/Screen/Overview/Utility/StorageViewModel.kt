package com.privacyshield.android.Component.Screen.Overview.Utility

import android.app.Application
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    // Internal storage
    private val _internalTotal = MutableStateFlow("")
    val internalTotal: StateFlow<String> = _internalTotal

    private val _internalAvail = MutableStateFlow("")
    val internalAvail: StateFlow<String> = _internalAvail

    private val _internalUsedPct = MutableStateFlow(0f) // 0..100
    val internalUsedPct: StateFlow<Float> = _internalUsedPct

    // External (shared) storage (may be absent)
    private val _externalTotal = MutableStateFlow<String?>(null)
    val externalTotal: StateFlow<String?> = _externalTotal

    private val _externalAvail = MutableStateFlow<String?>(null)
    val externalAvail: StateFlow<String?> = _externalAvail

    private val _externalUsedPct = MutableStateFlow<Float?>(null)
    val externalUsedPct: StateFlow<Float?> = _externalUsedPct

    // App (this app) storage breakdown (API 26+), graceful fallback text
    private val _appCode = MutableStateFlow<String?>(null)
    val appCode: StateFlow<String?> = _appCode

    private val _appData = MutableStateFlow<String?>(null)
    val appData: StateFlow<String?> = _appData

    private val _appCache = MutableStateFlow<String?>(null)
    val appCache: StateFlow<String?> = _appCache

    private val _appStatsNote = MutableStateFlow<String?>(null)
    val appStatsNote: StateFlow<String?> = _appStatsNote

    init {
        refreshAll()
    }

    fun refreshAll() = viewModelScope.launch(Dispatchers.IO) {
        loadInternal()
        loadExternal()
        loadAppStorage()
    }

    // --------- Helpers ---------
    private fun statFsSafe(path: File): Pair<Long, Long>? = try {
        val s = StatFs(path.absolutePath)
        val total = s.blockSizeLong * s.blockCountLong
        val avail = s.blockSizeLong * s.availableBlocksLong
        total to avail
    } catch (_: Throwable) { null }

    private fun pctUsed(total: Long, avail: Long): Float {
        if (total <= 0L) return 0f
        val used = total - avail
        return (used.toFloat() / total.toFloat()) * 100f
    }

    private fun human(bytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        val tb = gb * 1024
        return when {
            bytes >= tb -> String.format("%.2f TB", bytes / tb)
            bytes >= gb -> String.format("%.2f GB", bytes / gb)
            bytes >= mb -> String.format("%.2f MB", bytes / mb)
            bytes >= kb -> String.format("%.2f KB", bytes / kb)
            else -> "$bytes B"
        }
    }

    // --------- Loaders ---------
    private fun loadInternal() {
        val info = statFsSafe(ctx.dataDir /* /data/user/0/<pkg> */) ?: return
        val (total, avail) = info
        _internalTotal.value = human(total)
        _internalAvail.value = human(avail)
        _internalUsedPct.value = pctUsed(total, avail)
    }

    private fun loadExternal() {
        val extDir = Environment.getExternalStorageDirectory() // shared storage root
        val info = statFsSafe(extDir)
        if (info == null) {
            _externalTotal.value = null
            _externalAvail.value = null
            _externalUsedPct.value = null
            return
        }
        val (total, avail) = info
        _externalTotal.value = human(total)
        _externalAvail.value = human(avail)
        _externalUsedPct.value = pctUsed(total, avail)
    }

    private fun loadAppStorage() {
        // Try StorageStatsManager (API 26+) for code/data/cache
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val sm = ctx.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val storage = ctx.getSystemService(Context.STORAGE_SERVICE) as android.os.storage.StorageManager
                val uuid = storage.getUuidForPath(ctx.filesDir)
                val uid = ctx.applicationInfo.uid
                val stats = sm.queryStatsForUid(uuid, uid)
                // On API 26: appBytes ~ code, dataBytes ~ data, cacheBytes ~ cache (naming slightly varies across docs)
                _appCode.value = human(stats.appBytes)
                _appData.value = human(stats.dataBytes)
                _appCache.value = human(stats.cacheBytes)
                _appStatsNote.value = null
            } catch (t: Throwable) {
                // Fallback using dirs sizes (approx)
                fallbackAppSizes(t.message ?: "Permission/Access error")
            }
        } else {
            fallbackAppSizes("Requires Android O+ for precise app stats")
        }
    }

    private fun fallbackAppSizes(note: String) {
        // Approx by folder sizes (can be a bit slow on huge trees; still OK for demo)
        val code = try { // APK/ODEX size roughly from sourceDir (not always accurate)
            val apk = File(ctx.applicationInfo.sourceDir)
            if (apk.exists()) human(apk.length()) else "N/A"
        } catch (_: Throwable) { "N/A" }

        val dataBytes = dirSizeSafe(ctx.filesDir) + dirSizeSafe(ctx.noBackupFilesDir)
        val cacheBytes = dirSizeSafe(ctx.cacheDir) + (ctx.externalCacheDir?.let { dirSizeSafe(it) } ?: 0L)

        _appCode.value = code
        _appData.value = human(dataBytes)
        _appCache.value = human(cacheBytes)
        _appStatsNote.value = note
    }

    private fun dirSizeSafe(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return try {
            dir.walkTopDown().sumOf { if (it.isFile) it.length() else 0L }
        } catch (_: Throwable) {
            0L
        }
    }
}
