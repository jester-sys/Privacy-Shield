package com.privacyshield.android.Component.Screen.Permission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import com.google.ai.client.generativeai.GenerativeModel
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Screen.Deatils.utility.getDominantGradient
import com.privacyshield.android.Model.AppDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PermissionDetailsScreen(
    permission: String,
    allApps: List<AppDetail>?,
    onAppClick: (AppDetail) -> Unit,
    clickedApp: AppDetail? = null,
    currentApp: String? = null
) {
    var isLoading by remember { mutableStateOf(true) }

    // ✅ Artificial delay for loading screen
    LaunchedEffect(Unit) {
        delay(1500)
        isLoading = false
    }

    val isDataLoading = allApps == null || currentApp == null || isLoading

    if (isDataLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF9C27B0),
                strokeWidth = 4.dp,
                modifier = Modifier.size(50.dp)
            )
        }
        return
    }

    // ✅ Safe access
    val appsUsingPermission = allApps!!.filter { app ->
        app.permissions.any { it.name == permission }
    }
    val currentAppDetail = appsUsingPermission.find { it.packageName == currentApp }

    var aiExplanation by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(true) }

    // ✅ Fetch AI explanation on screen load
    LaunchedEffect(permission) {
        val result = getPermissionExplanation(permission)
        aiExplanation = result
        isAiLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFF121212))
            .padding(12.dp)
    ) {
        // 🔹 Permission Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Permission", color = Color.Gray, fontSize = 14.sp)

                Text(
                    text = permission,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                        )
                    )
                )

                Spacer(Modifier.height(12.dp))

                if (isAiLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Loading AI explanation...", color = Color.White)
                    }
                } else {
                    Text(
                        text = aiExplanation ?: "",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Current App
        currentAppDetail?.let { curr ->
            Text("Current App", color = Color(0xFF80DEEA), fontSize = 14.sp)
            AppCard(curr)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 🔹 Selected App
        clickedApp?.let { selected ->
            Text("Selected App", color = Color(0xFFA5D6A7), fontSize = 14.sp)
            AppCard(selected)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 🔹 Other Apps using the permission
        val otherApps = appsUsingPermission.filter {
            it.packageName != clickedApp?.packageName &&
                    it.packageName != currentAppDetail?.packageName
        }

        if (otherApps.isNotEmpty()) {
            Text("Other Apps", color = Color(0xFFFFAB91), fontSize = 14.sp)
            otherApps.forEach { app ->
                AppCard(app, onClick = { onAppClick(app) })
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No apps are using this permission.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}




@Composable
fun AppCard(app: AppDetail, onClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dominantGradient by remember { mutableStateOf<Brush>(Brush.verticalGradient(listOf(Color.DarkGray, Color.Black))) }

    LaunchedEffect(app.packageName) {
        val gradient = withContext(Dispatchers.Default) {
            getDominantGradient(context, app.packageName)
        }
        dominantGradient = gradient
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(dominantGradient)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                ,
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



suspend fun getPermissionExplanation(permission: String): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBPGYfEx4ls61jPvdIjXmmDXzAKVIcvio0"
    )

    val prompt = "Explain the Android permission: $permission in simple language."

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}

