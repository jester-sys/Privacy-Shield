package com.privacyshield.android.Component.Screen.Home.Action


import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.net.ConnectivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.ui.theme.BluePrimary
import com.privacyshield.android.ui.theme.TextPrimaryDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun AppDataUsageCard(
    context: Context,
    app: AppDetail,
    onDismiss: () -> Unit
) {
    var wifiFg by remember { mutableStateOf(0L) }
    var wifiBg by remember { mutableStateOf(0L) }
    var mobileFg by remember { mutableStateOf(0L) }
    var mobileBg by remember { mutableStateOf(0L) }
    var hasUsageAccess by remember { mutableStateOf(checkUsageAccess(context)) }

    // 👇 new loading state
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(app.packageName, hasUsageAccess) {
        if (!hasUsageAccess) {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            isLoading = true
            fetchAllNetworkUsage(context, app) { wf, wb, mf, mb ->
                wifiFg = wf
                wifiBg = wb
                mobileFg = mf
                mobileBg = mb
                isLoading = false   // ✅ data milte hi loading hata do
            }
        }
    }

    val total = (wifiFg + wifiBg + mobileFg + mobileBg).takeIf { it > 0 } ?: 1L
    val wfFraction = wifiFg.toFloat() / total
    val wbFraction = wifiBg.toFloat() / total
    val mfFraction = mobileFg.toFloat() / total
    val mbFraction = mobileBg.toFloat() / total

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF050505)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppIcon(app.packageName)
                Text(
                    text = "${app.appName} - Data Usage",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryDark
                )

                when {
                    // 👇 Show Progress while loading
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BluePrimary)
                        }
                    }

                    // 👇 No usage data case
                    wifiFg == 0L && wifiBg == 0L && mobileFg == 0L && mobileBg == 0L -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No data usage available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    else -> {
                        // 👇 Your old UI (stacked chart + usage details)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(wfFraction)
                                        .background(Color(0xFF2196F3))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(wbFraction)
                                        .background(Color(0xFF90CAF9))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(mfFraction)
                                        .background(Color(0xFFFF9800))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(mbFraction)
                                        .background(Color(0xFFFFCC80))
                                )
                            }
                        }


                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF050505)),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val usageItems = listOf(
                                    Pair("WiFi Foreground" to wifiFg, Color(0xFF2196F3)),
                                    Pair("WiFi Background" to wifiBg, Color(0xFF90CAF9)),
                                    Pair("Mobile Foreground" to mobileFg, Color(0xFFFF9800)),
                                    Pair("Mobile Background" to mobileBg, Color(0xFFFFCC80))
                                )

                                usageItems.forEach { (labelValuePair, color) ->
                                    val (label, value) = labelValuePair
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(color.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "$label: ${formatData(value)}",
                                            color = color,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    "Total: ${formatData(total)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimaryDark
                                )


                                Button(
                                    onClick = {    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${app.packageName}")
                                    }
                                        context.startActivity(intent)},
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                ) {
                                    Text("Manage", color = Color.White)
                                }
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                ) {
                                    Text("Close", color = Color.White)
                                }
                            }
                        }
                    }
                }




            }
        }
    }
}



fun formatData(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb < 1024) "%.2f MB".format(mb) else "%.2f GB".format(mb / 1024)
}


suspend fun fetchAllNetworkUsage(
    context: Context,
    app: AppDetail,
    onResult: (wifiFg: Long, wifiBg: Long, mobileFg: Long, mobileBg: Long) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val uid = try { pm.getApplicationInfo(app.packageName, 0).uid } catch (e: Exception) { 0 }
            if (uid == 0) return@withContext

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis

            val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

            val wf = getNetworkUsage(networkStatsManager, uid, startTime, endTime, ConnectivityManager.TYPE_WIFI)
            val wb = getNetworkUsage(networkStatsManager, uid, startTime, endTime, ConnectivityManager.TYPE_WIFI)
            val mf = getNetworkUsage(networkStatsManager, uid, startTime, endTime, ConnectivityManager.TYPE_MOBILE)
            val mb = getNetworkUsage(networkStatsManager, uid, startTime, endTime, ConnectivityManager.TYPE_MOBILE)

            onResult(wf, wb, mf, mb)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
fun checkUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.checkOpNoThrow(
        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

fun getNetworkUsage(
    networkStatsManager: NetworkStatsManager,
    uid: Int,
    startTime: Long,
    endTime: Long,
    networkType: Int
): Long{
    var total = 0L
    val stats = networkStatsManager.queryDetailsForUid(networkType, "", startTime, endTime, uid)
    val bucket = NetworkStats.Bucket()
    while (stats.hasNextBucket()) {
        stats.getNextBucket(bucket)
        total += bucket.rxBytes + bucket.txBytes
    }
    stats.close()
    return total
}
