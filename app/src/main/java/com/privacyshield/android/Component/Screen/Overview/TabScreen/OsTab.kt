package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Utility.OsViewModel
import com.privacyshield.android.R
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OsTab(viewModel: OsViewModel = hiltViewModel()) {
    val androidVersion by viewModel.androidVersion.collectAsState()
    val sdk by viewModel.sdk.collectAsState()
    val securityPatch by viewModel.securityPatch.collectAsState()
    val securityDays by viewModel.securityPatchDays.collectAsState()
    val kernel by viewModel.kernel.collectAsState()
    val bootloader by viewModel.bootloader.collectAsState()
    val fingerprint by viewModel.fingerprint.collectAsState()
    val buildId by viewModel.buildId.collectAsState()
    val selinux by viewModel.selinuxEnforced.collectAsState()
    val uptime by viewModel.uptime.collectAsState()
    val timezone by viewModel.timezone.collectAsState()
    val locale by viewModel.locale.collectAsState()
    val isRooted by viewModel.isRooted.collectAsState()
    val hasGms by viewModel.hasGms.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var showAssistantCard by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // Dark background
    ) {
        Column {

            if (isLoading && !showAssistantCard) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFF00796B),
                    trackColor = Color.DarkGray
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {


                // 🔹 AI Storage Assistant Card
                if (showAssistantCard) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF00796B).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                Text(
                                    "AI  Android OS Assistant",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF00796B)
                                )



                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .padding(4.dp)
                                    )
                                }

                                // 🔹 Show AI explanation
                                explanation?.let {
                                    Text(
                                        it,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                // 🔹 Question + Ask AI
                                if (explanation != null) {
                                    OutlinedTextField(
                                        value = question,
                                        onValueChange = { question = it },
                                        label = {
                                            Text(
                                                "Ask about Android OS",
                                                color = Color.White
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color(0xFFEEEEEE),
                                            cursorColor = Color.White
                                        )
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    if (question.isNotBlank()) {
                                                        isLoading = true
                                                        answer = hasGms?.let {
                                                            isRooted?.let { it1 ->
                                                                getAndroidInfoExplanation(
                                                                    androidVersion = androidVersion,
                                                                    sdk = sdk,
                                                                    securityPatch = securityPatch,
                                                                    securityDays = securityDays.toString(),
                                                                    kernel = kernel,
                                                                    bootloader = bootloader,
                                                                    fingerprint = fingerprint,
                                                                    buildId = buildId,
                                                                    selinux = selinux.toString(),
                                                                    uptime = uptime,
                                                                    timezone = timezone,
                                                                    locale = locale,
                                                                    isRooted = it1,
                                                                    hasGms = it
                                                                )
                                                            }
                                                        }
                                                        isLoading = false
                                                        question = ""
                                                    }
                                                }
                                            },
                                            enabled = question.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                        ) {
                                            Text("Ask AI", color = Color(0xFF00796B))
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                question = ""
                                                explanation = null
                                                answer = null
                                                showAssistantCard = false
                                            },
                                            border = BorderStroke(1.dp, Color.White)
                                        ) {
                                            Text("Clear Chat", color = Color.White)
                                        }
                                    }

                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(top = 4.dp)
                                        )
                                    }

                                    answer?.let {
                                        Text("Answer: $it", color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                }


                item { SectionHeader("System", Color(0xFFE0E0E0)) }

                item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF00796B).copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Android Version",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text =  "$androidVersion (SDK $sdk)",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00796B))
                            )
                            Spacer(Modifier.height(6.dp))

                        }
                    }
                }

                // Security patch age with color-coded badge + small progress (age/365)
                item {
                    val days = securityDays ?: -1L
                    val patchColor = when {
                        days < 0 -> Color(0xFF9E9E9E)
                        days <= 90 -> Color(0xFF4CAF50) // fresh
                        days <= 180 -> Color(0xFFFFC107) // moderate
                        else -> Color(0xFFF44336) // old
                    }
                    val progress =
                        if (days >= 0) ((365 - days).coerceAtLeast(0).toFloat() / 365f).coerceIn(
                            0f,
                            1f
                        ) else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(patchColor.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Security Patch",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (days >= 0) "$securityPatch — $days days ago" else securityPatch,
                                style = MaterialTheme.typography.bodySmall.copy(color = patchColor)
                            )
                            Spacer(Modifier.height(6.dp))

                        }
                    }
                }

                item { SectionHeader("Build & Kernel", Color(0xFFE0E0E0)) }

                item {
                    TintedStatBox(
                        title = "Kernel",
                        value = kernel.ifBlank { "Unavailable" },
                        progress = 1f,
                        color = Color(0xFF1976D2)
                    )
                }

                item {
                    TintedStatBox(
                        title = "Bootloader",
                        value = bootloader,
                        progress = 1f,
                        color = Color(0xFF1976D2)
                    )
                }

                item {
                    TintedStatBox(
                        title = "Build Fingerprint",
                        value = fingerprint,
                        progress = 1f,
                        color = Color(0xFF1976D2)
                    )
                }

                item { SectionHeader("Runtime / Security", Color(0xFFE0E0E0)) }

                item {
                    TintedStatBox(
                        title = "Uptime",
                        value = uptime,
                        progress = 1f,
                        color = Color(0xFF9C27B0)
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selinux == true) Color(0xFF4CAF50).copy(alpha = 0.12f) else if (selinux == false) Color(
                                    0xFFF44336
                                ).copy(alpha = 0.12f) else Color(0xFF9E9E9E).copy(alpha = 0.12f)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "SELinux",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = when (selinux) {
                                    true -> "Enforced / Enabled"
                                    false -> "Disabled or not enforced"
                                    else -> "Unknown"
                                },
                                color = Color.White
                            )
                        }
                    }
                }

                item { SectionHeader("Locale & Misc", Color(0xFFE0E0E0)) }

                item {
                    TintedStatBox(
                        title = "Timezone",
                        value = timezone,
                        progress = 1f,
                        color = Color(0xFF00BCD4)
                    )
                }

                item {
                    TintedStatBox(
                        title = "Locale",
                        value = locale,
                        progress = 1f,
                        color = Color(0xFF00BCD4)
                    )
                }

                item { SectionHeader("Security & Play Services", Color(0xFFE0E0E0)) }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "Rooted",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = when (isRooted) {
                                    true -> "Yes — device shows su binaries / which su"
                                    false -> "No"
                                    else -> "Unknown"
                                },
                                color = when (isRooted) {
                                    true -> Color(0xFFF44336)
                                    false -> Color(0xFF4CAF50)
                                    else -> Color.White
                                }
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF9E9E9E).copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "Google Play Services",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = when (hasGms) {
                                    true -> "Installed"
                                    false -> "Not found"
                                    else -> "Unknown"
                                },
                                color = if (hasGms == true) Color(0xFF4CAF50) else Color.White
                            )
                        }
                    }
                }

                item {
                    InfoHintBox(
                        "Tip: For precise app-level storage and usage stats you may need special access (Usage / Storage) depending on Android version.",
                        color = Color(0xFF00796B)
                    )
                }
            }

        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = hasGms?.let {
                        isRooted?.let { it1 ->
                            getAndroidInfoExplanation(
                                androidVersion = androidVersion,
                                sdk = sdk,
                                securityPatch = securityPatch,
                                securityDays = securityDays.toString(),
                                kernel = kernel,
                                bootloader = bootloader,
                                fingerprint = fingerprint,
                                buildId = buildId,
                                selinux = selinux.toString(),
                                uptime = uptime,
                                timezone = timezone,
                                locale = locale,
                                isRooted = it1,
                                hasGms = it
                            )
                        }
                    }
                    isLoading = false
                    showAssistantCard = true
                }

            },
            containerColor = Color(0xFF7B1FA2),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_icon),
                contentDescription = "AI Info"
            )
        }
    }
}

suspend fun getAndroidInfoExplanation(
    androidVersion: String,
    sdk: String,
    securityPatch: String,
    securityDays: String,
    kernel: String,
    bootloader: String,
    fingerprint: String,
    buildId: String,
    selinux: String,
    uptime: String,
    timezone: String,
    locale: String,
    isRooted: Boolean,
    hasGms: Boolean,
    userQuestion: String? = null,
): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA"
    )

    // ✅ Build Android info string
    val androidInfo = buildString {
        appendLine("Android Version: $androidVersion")
        appendLine("SDK: $sdk")
        appendLine("Security Patch: $securityPatch")
        appendLine("Days Since Last Security Patch: $securityDays")
        appendLine("Kernel Version: $kernel")
        appendLine("Bootloader: $bootloader")
        appendLine("Fingerprint: $fingerprint")
        appendLine("Build ID: $buildId")
        appendLine("SELinux: $selinux")
        appendLine("Uptime: $uptime")
        appendLine("Timezone: $timezone")
        appendLine("Locale: $locale")
        appendLine("Rooted Device: $isRooted")
        appendLine("Google Mobile Services Present: $hasGms")
    }

    // ✅ Build prompt
    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this Android OS information in very simple terms:\n$androidInfo"
    } else {
        "Here is the Android OS information:\n$androidInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}

