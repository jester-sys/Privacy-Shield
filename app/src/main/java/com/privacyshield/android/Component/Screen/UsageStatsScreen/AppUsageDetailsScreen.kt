package com.privacyshield.android.Component.Screen.UsageStatsScreen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Model.AppDetail
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageDetailsScreen(
    context: Context,
    app: AppDetail,
    navController: NavHostController
) {
    val usageStats = remember {
        getAppUsageStats(context, app.packageName)
    }

    Scaffold(

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app.packageName)
                Spacer(Modifier.width(16.dp))
                Text(app.appName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            usageStats?.let {
                Text("Last Used: ${Date(it.lastTimeUsed)}", fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Total Foreground Time: ${it.totalTimeInForeground / 1000 / 60} min",
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Text("First Time Stamp: ${Date(it.firstTimeStamp)}", fontSize = 16.sp)
            } ?: Text("No usage data available", fontSize = 16.sp, color = Color.Gray)
        }
    }
}
