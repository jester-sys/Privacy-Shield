package com.privacyshield.android.Component.Screen.Deatils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Component.Screen.Deatils.utility.formatSourceDir
import com.privacyshield.android.Component.Screen.Deatils.utility.getDominantGradient
import com.privacyshield.android.Component.Screen.Home.Action.openApp
import com.privacyshield.android.Component.Screen.Home.Action.shareApp
import com.privacyshield.android.Component.Screen.Home.Action.uninstallApp
import com.privacyshield.android.Component.Screen.Home.formatTime
import com.privacyshield.android.Component.Screen.Home.utility.OpenAction
import com.privacyshield.android.Component.Screen.Home.utility.ShareAction
import com.privacyshield.android.Component.Screen.Home.utility.UninstallAction
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.Model.AppPermission
import com.privacyshield.android.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.security.AllPermission

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    app: AppDetail,
    navController: NavHostController,
    onPermissionClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val dominantGradient by remember {
        mutableStateOf(getDominantGradient(context, app.packageName))
    }
    var showAiDialog by remember { mutableStateOf(false) }

    val grantedPerms = app.permissions.filter { it.isGranted }
    val dangerousPerms = app.permissions.filter { it.isDangerous }
    val declaredPerms = app.permissions.filter { it.isDeclared }

    var allPermissionExpanded by remember { mutableStateOf(false) }
    var grantedPermissionExpanded by remember { mutableStateOf(false) }
    var dangerousPermissionExpanded by remember { mutableStateOf(false) }
    var declaredPermissionExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 🔹 App Header Card
            item {
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
            }
            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF121212) // dark background
                    )
                ) {


                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,

                    ) {
                        ActionButton("Open", Icons.Default.OpenInNew, Color(0xFF4CAF50), onClick = {
                            openApp(context, app)
                        })

                        ActionButton(
                            "Uninstall",
                            Icons.Default.Delete,
                            Color(0xFFF44336),
                            onClick = {
                                uninstallApp(context, app)
                            })
                        ActionButton("Share", Icons.Default.Share, Color(0xFF2196F3), onClick = {
                            shareApp(context, app)
                        })

                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 🔹 Version Info Card
            item {
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
            }

            // 🔹 Permissions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            allPermissionExpanded = !allPermissionExpanded
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Permissions (${app.permissions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFD4D4D4)
                    )

                    Icon(
                        imageVector = if (allPermissionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(Modifier.height(12.dp))

            }

            // 🔹 Expanded Permission Sections
            if (allPermissionExpanded) {
                item {
                    Column {
                        PermissionSection(
                            title = "Granted (${grantedPerms.size})",
                            color = Color.Green,
                            permissions = grantedPerms,
                            expanded = grantedPermissionExpanded,
                            onExpandToggle = {
                                grantedPermissionExpanded = !grantedPermissionExpanded
                            },
                            onPermissionClick = { onPermissionClick(it.name) }
                        )

                        Spacer(Modifier.height(12.dp))

                        PermissionSection(
                            title = "Dangerous (${dangerousPerms.size})",
                            color = Color.Red,
                            permissions = dangerousPerms,
                            expanded = dangerousPermissionExpanded,
                            onExpandToggle = {
                                dangerousPermissionExpanded = !dangerousPermissionExpanded
                            },
                            onPermissionClick = { onPermissionClick(it.name) }
                        )

                        Spacer(Modifier.height(12.dp))

                        PermissionSection(
                            title = "Declared (${declaredPerms.size})",
                            color = Color.Cyan,
                            permissions = declaredPerms,
                            expanded = declaredPermissionExpanded,
                            onExpandToggle = {
                                declaredPermissionExpanded = !declaredPermissionExpanded
                            },
                            onPermissionClick = { onPermissionClick(it.name) },
                            navController = navController,
                            app = app
                        )
                    }
                }
            }
        }

        // ✅ Auto scroll effect
        LaunchedEffect(allPermissionExpanded) {
            if (allPermissionExpanded) {
                // Scroll permissions ko thoda screen ke upar lane ke liye
                listState.animateScrollToItem(2, scrollOffset = -50)
            }
        }

        // 🔹 Floating AI Button
        FloatingActionButton(
            onClick = { showAiDialog = true },
            containerColor = Color(0xFF9C27B0),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_icon),
                contentDescription = "AI Info"
            )
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun PermissionSection(
    title: String,
    color: Color,
    permissions: List<AppPermission>,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onPermissionClick: (AppPermission) -> Unit,
    navController: NavHostController? = null,
    app: AppDetail? = null
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .bringIntoViewRequester(bringIntoViewRequester) // ✅ attach
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onExpandToggle()
                    if (!expanded) {

                        coroutineScope.launch {
                            delay(200)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFD4D4D4)
            )

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (permissions.isEmpty()) {
                        Text(
                            "No permissions required",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            title,
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            permissions.forEach { perm ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF2A2A2A),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            if (title == "Declared Permissions" && navController != null && app != null) {
                                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                                    "selectedPermission", perm.name
                                                )
                                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                                    "allApps", listOf(app)
                                                )
                                                val encodedPermission =
                                                    URLEncoder.encode(perm.name, "UTF-8")
                                                navController.navigate("permission_details/$encodedPermission")
                                            } else {
                                                onPermissionClick(perm)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = perm.name,
                                        fontSize = 12.sp,
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

fun openInPlayStore(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.setPackage("com.android.vending")
        context.startActivity(intent)
    } catch (e: Exception) {
        // Agar Play Store app na ho to browser open karo
        val intent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        context.startActivity(intent)
    }
}

