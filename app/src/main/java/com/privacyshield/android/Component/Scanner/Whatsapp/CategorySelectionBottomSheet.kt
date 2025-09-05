package com.privacyshield.android.Component.Scanner.Whatsapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionBottomSheet(
    categories: List<FileCategory>,
    selectedCategories: SnapshotStateList<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ✅ Title
            Text(
                text = "Select Media to Delete",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ✅ Category List
            categories.forEach { category ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (selectedCategories.contains(category.name)) {
                                selectedCategories.remove(category.name)
                            } else {
                                selectedCategories.add(category.name)
                            }
                        }
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = category.iconRes,
                        contentDescription = category.name,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${category.fileCount} files • ${formatSize(category.size)}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Checkbox(
                        checked = selectedCategories.contains(category.name),
                        onCheckedChange = {
                            if (it) selectedCategories.add(category.name)
                            else selectedCategories.remove(category.name)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF25D366),
                            uncheckedColor = Color.Gray
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(Modifier.height(20.dp))

            // ✅ Confirm Delete Button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Delete Selected Media", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}
