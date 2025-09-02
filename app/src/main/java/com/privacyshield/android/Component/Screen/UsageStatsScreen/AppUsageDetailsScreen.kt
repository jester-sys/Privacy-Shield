package com.privacyshield.android.Component.Screen.UsageStatsScreen

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController

import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Screen.Deatils.utility.getDominantGradient
import com.privacyshield.android.Component.Screen.UsageStatsScreen.model.formatLastOpened
import com.privacyshield.android.Component.Screen.UsageStatsScreen.model.getUsageBetweenDates
import com.privacyshield.android.Component.Screen.UsageStatsScreen.model.hasUsageStatsPermission
import com.privacyshield.android.Model.AppDetail
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@SuppressLint("ContextCastToActivity", "RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageDetailsScreen(
    context: Context,
    app: AppDetail,
    navController: NavHostController
) {
    // 🔹 Always fetch last 7 days
    val startDate = getNDaysAgo(7)
    val endDate = System.currentTimeMillis()

    // 🔹 Usage data based on last 7 days
    var usageData by remember {
        mutableStateOf(
            getUsageBetweenDates(context, app.packageName, startDate, endDate)
         )
    }
    val totalUsageMillis = usageData.sumOf { it.totalTime }
    val totalOpens = usageData.sumOf { it.openCount }

    val maxUsage = usageData.maxOfOrNull { it.totalTime / 1000 / 60 }?.coerceAtLeast(1) ?: 1

    LaunchedEffect(Unit) {
        if (!hasUsageStatsPermission(context)) {
            context.startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🔹 App Header (Gradient BG)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(getDominantGradient(context, app.packageName))
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(app.packageName, size = 60.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(app.appName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Usage Insights (Last 7 days)", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // 🔹 Total Usage Card
        item {
            val totalMinutes = usageData.sumOf { it.totalTime / 1000 / 60 }
            val totalOpens = usageData.sumOf { it.openCount }
            val progress = (totalUsageMillis.toFloat() / (maxUsage * usageData.size * 60_000L).toFloat())
                .coerceAtMost(1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                elevation = CardDefaults.elevatedCardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Screen Time (7 Days)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(formatTime(totalUsageMillis), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Opens: $totalOpens", fontSize = 14.sp, color = Color.Gray)
                        }


                        CircularProgressIndicator(
                            progress = { progress }, // ✅ ab float milega
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

        }

        // 🔹 Weekly Usage Chart
        item {
            Text("Usage Chart (Last 7 days)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF121212))
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState()), // ✅ scrollable
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                usageData.forEach { day ->
                    val minutes = day.totalTime / 1000 / 60
                    val fraction = minutes.toFloat() / maxUsage.toFloat()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(32.dp) // ✅ fixed width per bar
                                .height((140.dp * fraction).coerceAtLeast(8.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF66BB6A), Color(0xFF2E7D32))
                                    )
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(day.dayName.take(3), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // 🔹 Daily Breakdown
        items(usageData.takeLast(7)) { dayUsage -> // ✅ last 7 days only
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${dayUsage.dayName} (${dayUsage.date})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Usage: ${formatTime(dayUsage.totalTime)}", fontSize = 14.sp)
                    Text("Opens: ${dayUsage.openCount}", fontSize = 14.sp)
                    Text("Last Opened: ${formatLastOpened(dayUsage.lastOpened)}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

// 🔹 Helper Functions
fun getNDaysAgo(days: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -days)
    return cal.timeInMillis
}

fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDateSelected: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDateRangePickerState()

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val start = state.selectedStartDateMillis
                val end = state.selectedEndDateMillis
                if (start != null && end != null) {
                    onDateSelected(start, end)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(state = state)
    }
}

//@Composable
//fun DateRangePicker(
//    startDate: Long,
//    endDate: Long,
//    onStartDateChange: (Long) -> Unit,
//    onEndDateChange: (Long) -> Unit
//) {
//    val context = LocalContext.current
//    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        // Start Date
//        OutlinedButton(onClick = {
//            android.app.DatePickerDialog(
//                context,
//                { _, year, month, day ->
//                    val cal = Calendar.getInstance()
//                    cal.set(year, month, day, 0, 0, 0)
//                    onStartDateChange(cal.timeInMillis)
//                },
//                Calendar.getInstance().apply { timeInMillis = startDate }.get(Calendar.YEAR),
//                Calendar.getInstance().apply { timeInMillis = startDate }.get(Calendar.MONTH),
//                Calendar.getInstance().apply { timeInMillis = startDate }.get(Calendar.DAY_OF_MONTH)
//            ).show()
//        }) {
//            Text("From: ${dateFormatter.format(Date(startDate))}")
//        }
//
//        // End Date
//        OutlinedButton(onClick = {
//            android.app.DatePickerDialog(
//                context,
//                { _, year, month, day ->
//                    val cal = Calendar.getInstance()
//                    cal.set(year, month, day, 23, 59, 59)
//                    onEndDateChange(cal.timeInMillis)
//                },
//                Calendar.getInstance().apply { timeInMillis = endDate }.get(Calendar.YEAR),
//                Calendar.getInstance().apply { timeInMillis = endDate }.get(Calendar.MONTH),
//                Calendar.getInstance().apply { timeInMillis = endDate }.get(Calendar.DAY_OF_MONTH)
//            ).show()
//        }) {
//            Text("To: ${dateFormatter.format(Date(endDate))}")
//        }
//    }
//}
