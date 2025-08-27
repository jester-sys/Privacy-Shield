package com.privacyshield.android.Component.Screen.Permission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Screen.Deatils.utility.getDominantGradient
import com.privacyshield.android.Model.AppDetail

@Composable
fun PermissionDetailScreen(
    permission: String,
    allApps: List<AppDetail>,
    onAppClick: (AppDetail) -> Unit // 👈 Click handle karega
) {
    val context = LocalContext.current
    val appsUsingPermission = allApps.filter { app ->
        app.permissions.any { it.name == permission }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFF1E1E1E))
            .padding(12.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Apps using permission",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = permission,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF9C27B0)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (appsUsingPermission.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No apps are using this permission.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            appsUsingPermission.forEach { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onAppClick(app) }, // 👈 Click event
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(app.packageName)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                app.appName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                app.packageName,
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}


// -----------------------------
// New Screen (Clicked App + All Apps using same permission)
// -----------------------------
@Composable
fun AppWithPermissionScreen(
    clickedApp: AppDetail,
    permission: String,
    allApps: List<AppDetail>,
    onBack: () -> Unit
) {
    val appsUsingPermission = allApps.filter { app ->
        app.permissions.any { it.name == permission }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Back Button
        Text(
            text = "← Back",
            color = Color.Cyan,
            modifier = Modifier
                .clickable { onBack() }
                .padding(bottom = 12.dp)
        )

        // Current App
        Text("Selected App", color = Color.Gray, fontSize = 14.sp)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(clickedApp.packageName)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(clickedApp.appName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(clickedApp.packageName, color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Other apps
        Text("Other Apps using $permission", color = Color.Gray, fontSize = 14.sp)
        appsUsingPermission.forEach { app ->
            if (app.packageName != clickedApp.packageName) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(app.packageName)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(app.appName, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(app.packageName, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}