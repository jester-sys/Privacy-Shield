package com.privacyshield.android.Component.Scanner

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateRangePickerDialog(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onDismiss: () -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var context =  LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF3A3A3A)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Select Date Range", color = Color.White, fontSize = 18.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Start Date Picker
                Button(onClick = {
                    val today = LocalDate.now()
                    // For simplicity, using Android DatePickerDialog inside Compose Button
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            startDate = LocalDate.of(year, month + 1, day)
                        },
                        startDate.year, startDate.monthValue - 1, startDate.dayOfMonth
                    ).show()
                }) {
                    Text("Start: $startDate")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // End Date Picker
                Button(onClick = {
                    val today = LocalDate.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            endDate = LocalDate.of(year, month + 1, day)
                        },
                        endDate.year, endDate.monthValue - 1, endDate.dayOfMonth
                    ).show()
                }) {
                    Text("End: $endDate")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onDateRangeSelected(startDate, endDate)
                        onDismiss()
                    }) {
                        Text("OK", color = Color.White)
                    }
                }
            }
        }
    }
}

