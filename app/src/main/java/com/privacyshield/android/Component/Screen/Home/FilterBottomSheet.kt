package com.privacyshield.android.Component.Screen.Home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Component.Screen.Home.utility.FilterType
import com.privacyshield.android.Component.Screen.Home.utility.SortType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filterOptions: List<FilterType>,
    pendingFilters: SnapshotStateList<FilterType>,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding(16.dp)
        ) {
            Text("Filter Apps", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(filterOptions) { option ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable {
                                if (option in pendingFilters) pendingFilters.remove(option)
                                else pendingFilters.add(option)
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = option in pendingFilters,
                            onCheckedChange = {
                                if (it) pendingFilters.add(option) else pendingFilters.remove(option)
                            }
                        )
                         Text(option.label,  Modifier.padding(start = 8.dp))
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancel" ,style = TextStyle(color = Color.White, fontWeight = FontWeight.W600)) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) { Text("Apply", style = TextStyle(color = Color.White, fontWeight = FontWeight.W600)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    sortOptions: List<SortType>,
    selectedSort: SortType,
    onSelect: (SortType) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {},
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Sort Apps", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            sortOptions.forEach { option ->
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { onSelect(option) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSort == option,
                        onClick = { onSelect(option) }
                    )
                    Text(option.label, Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

