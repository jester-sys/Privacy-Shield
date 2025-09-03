package com.privacyshield.android.Component.Scanner

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

@Composable
fun FileRow(title: String, files: List<File>, type: String, onItemClick: (List<File>, String) -> Unit) {
    Column {
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow {
            val maxItems = 5
            val limited = files.take(maxItems)
            items(limited) { file ->
                FilePreview(file, type) {
                    // 🔹 Jab user kisi item pe click kare -> full screen open
                    onItemClick(files, type)
                }
            }

            if (files.size > maxItems) {
                item {
                    Card(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(6.dp)
                            .clickable { onItemClick(files, type) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "+${files.size - maxItems} more",
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
}

