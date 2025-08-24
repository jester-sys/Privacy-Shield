package com.privacyshield.android.Component.Helper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// AppIcon.kt
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.withContext
import com.privacyshield.android.R


private object AppIconCache {
    private val cacheSizeKb = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    private val lru = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }
    fun get(key: String): Bitmap? = lru.get(key)
    fun put(key: String, bmp: Bitmap) { lru.put(key, bmp) }
}

@Composable
fun AppIcon(
    packageName: String,
    size: Dp = 50.dp,
    placeholderRes: Int = R.drawable.ic_playstore_icon,
    errorRes: Int = R.drawable.ic_playstore_icon
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val targetPx = with(density) { size.roundToPx() }

    var painter by remember(packageName) { mutableStateOf<Painter?>(null) }

    LaunchedEffect(packageName, targetPx) {

        val cached = AppIconCache.get(packageName)
        if (cached != null) {
            painter = BitmapPainter(cached.asImageBitmap())
            return@LaunchedEffect
        }


        val bmp = withContext(Dispatchers.IO) {
            try {
                val drawable = context.packageManager.getApplicationIcon(packageName)

                drawable.toBitmap(width = targetPx, height = targetPx, config = Bitmap.Config.ARGB_8888)
            } catch (e: Exception) {
                ContextCompat.getDrawable(context, errorRes)
                    ?.toBitmap(width = targetPx, height = targetPx, config = Bitmap.Config.ARGB_8888)
            }
        }

        bmp?.let {
            AppIconCache.put(packageName, it)
            painter = BitmapPainter(it.asImageBitmap())
        }
    }

    Image(
        painter = painter ?: painterResource(placeholderRes),
        contentDescription = null,
        modifier = Modifier.size(size)
    )
}
