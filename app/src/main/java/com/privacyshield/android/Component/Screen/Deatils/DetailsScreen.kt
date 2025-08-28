package com.privacyshield.android.Component.Screen.Deatils

import android.os.Bundle
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Component.Screen.Deatils.utility.formatSourceDir
import com.privacyshield.android.Component.Screen.Deatils.utility.getDominantGradient
import com.privacyshield.android.Component.Screen.Home.formatTime
import com.privacyshield.android.Model.AppDetail
import java.net.URLEncoder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(app: AppDetail,   navController: NavHostController,   onPermissionClick: (String) -> Unit ) {
    val context = LocalContext.current
    val dominantGradient by remember {
        mutableStateOf(getDominantGradient(context, app.packageName))
    }
    var showAiDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {

            // App Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dominantGradient)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(app.packageName)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                app.appName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Text(
                                app.packageName,
                                fontSize = 14.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Version Info Card
            InfoCard(
                title = "App Info",
                items = listOf(
                    "Version" to "${app.versionName} (${app.versionCode})",
                    "Min SDK" to app.minSdk.toString(),
                    "Target SDK" to app.targetSdk.toString(),
                    "Compile SDK" to app.compileSdk.toString(),
                    "Installed" to formatTime(app.firstInstallTime),
                    "Updated" to formatTime(app.lastUpdateTime),
                    "Source Dir" to formatSourceDir(app.sourceDir),
                    "Type" to if (app.isSystemApp) "System App" else "User App"
                ),
                highlightKey = "Type",
                highlightColor = if (app.isSystemApp) Color.Red else Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Permissions Section - Separate by isGranted, isDangerous, isDeclared
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Permissions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (app.permissions.isEmpty()) {
                        Text(
                            "No permissions required",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        val grantedPerms = app.permissions.filter { it.isGranted }
                        val dangerousPerms = app.permissions.filter { it.isDangerous }
                        val declaredPerms = app.permissions.filter { it.isDeclared }

                        if (grantedPerms.isNotEmpty()) {
                            Text("Granted", fontWeight = FontWeight.Medium, color = Color(0xFF388E3C))
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                grantedPerms.forEach { perm ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF2A2A2A),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onPermissionClick(perm.name)

                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = perm.toString(), fontSize = 12.sp, color = Color.Green)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (dangerousPerms.isNotEmpty()) {
                            Text("Dangerous", fontWeight = FontWeight.Medium, color = Color.Red)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dangerousPerms.forEach { perm ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF2A2A2A),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onPermissionClick(perm.name)
                                            }

                                            .padding(horizontal = 12.dp, vertical = 6.dp)

                                    ) {
                                        Text(text = perm.toString(), fontSize = 12.sp, color = Color.Red)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (declaredPerms.isNotEmpty()) {
                            Text("Declared", fontWeight = FontWeight.Medium, color = Color.Cyan)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                declaredPerms.forEach { perm ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF2A2A2A),
                                                shape = RoundedCornerShape(12.dp)

                                            )
                                            .clickable {
                                                // Save selected permission
                                                navController.currentBackStackEntry?.savedStateHandle?.set("selectedPermission", perm.name)

                                                // ✅ Yahan pe sari apps pass karo
                                                navController.currentBackStackEntry?.savedStateHandle?.set("allApps", listOf(app))

                                                // Navigate
                                                val encodedPermission = URLEncoder.encode(perm.name, "UTF-8")
                                                navController.navigate("permission_details/$encodedPermission")
                                            }

                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = perm.toString(), fontSize = 12.sp, color = Color.Cyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAiDialog = true }, // <-- Click pe dialog show hoga
            containerColor = Color(0xFF9C27B0),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "AI Info")
        }
        if (showAiDialog) {
            DetailsScreenWithAISuggestions(app = app, navController = navController)
        }


    }
}

@Composable
fun InfoCard(
    title: String,
    items: List<Pair<String, String>>,
    highlightKey: String? = null,
    highlightColor: Color = Color(0xFF4CAF50) // Default green accent
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121212) // dark background
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { (key, value) ->
                val isHighlighted = key == highlightKey

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(
                            if (isHighlighted) highlightColor.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = key,
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                        color = if (isHighlighted) highlightColor else Color.White
                    )
                }
            }
        }
    }
}
