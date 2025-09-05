package com.privacyshield.android.Component.Scanner

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.privacyshield.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


//@Composable
//fun FileRow(
//    title: String,
//    files: List<File>,
//    type: String,
//    onItemClick: (List<File>, String) -> Unit
//) {
//    Column {
//        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
//        Spacer(modifier = Modifier.height(6.dp))
//
//        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//            val maxItems = 5
//            val limited = files.take(maxItems)
//            items(limited) { file ->
//                FilePreview(file, type) {
//                    // 🔹 Jab user kisi item pe click kare -> full screen open
//                    onItemClick(files, type)
//                }
//            }
//
//            if (files.size > maxItems) {
//                item {
//                    Card(
//                        modifier = Modifier
//                            .size(80.dp)
//                            .padding(6.dp)
//                            .clickable { onItemClick(files, type) },
//                        shape = RoundedCornerShape(12.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
//                    ) {
//                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                            Text(
//                                "+${files.size - maxItems} more",
//                                color = Color.White,
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}






@Composable
fun FileRow(
    title: String,
    files: List<File>,
    type: String,
    onClick: (List<File>, String) -> Unit
) {
    if (files.isEmpty()) return

    val maxItems = 10
    val limitedFiles = files.take(maxItems)
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column {
        Text(
            title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(limitedFiles) { file ->

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A2A2A))
                        .clickable { onClick(files, type) },
                    contentAlignment = Alignment.Center
                ) {

                        when (type) {
                            "image" -> {
                                AsyncImage(
                                    model = file,
                                    contentDescription = file.name,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            "video" -> {

                                LaunchedEffect(file) {
                                    withContext(Dispatchers.IO) {
                                        val thumb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            ThumbnailUtils.createVideoThumbnail(
                                                file,
                                                Size(128, 128),
                                                null
                                            )
                                        } else {
                                            ThumbnailUtils.createVideoThumbnail(
                                                file.path,
                                                android.provider.MediaStore.Images.Thumbnails.MINI_KIND
                                            )
                                        }
                                        bitmap = thumb
                                     }
                                }

                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = file.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } ?: Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) // placeholder
                            }

                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "File",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = file.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // 🔹 Show "+N more" if files exceed maxItems
            if (files.size > maxItems) {
                item {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A2A2A))
                            .clickable { onClick(files, type) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${files.size - maxItems} more",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
