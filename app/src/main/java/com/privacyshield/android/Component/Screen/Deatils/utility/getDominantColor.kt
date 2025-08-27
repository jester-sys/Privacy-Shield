package com.privacyshield.android.Component.Screen.Deatils.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
@SuppressLint("NewApi")
fun getDominantGradient(context: Context, packageName: String): Brush {
    val pm = context.packageManager
    val drawable = pm.getApplicationIcon(packageName)

    // Convert Drawable → Bitmap safely
    val bitmap = when (drawable) {
        is BitmapDrawable -> drawable.bitmap
        is AdaptiveIconDrawable -> {
            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
        else -> {
            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
    }

    val palette = Palette.from(bitmap).generate()
    val dominantColor = Color(palette.getDominantColor(Color.Gray.toArgb()))


    return Brush.linearGradient(
        colors = listOf(
            dominantColor.copy(alpha = 0.5f), // halka app color
            Color.Black                        // niche black
        ),
        start = Offset(0f, 0f),    // top-left
        end = Offset(1000f, 1000f) // bottom-right
    )
}
