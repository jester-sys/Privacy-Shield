package com.privacyshield.android.Component.Screen.Overview.TabScreen

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ai.client.generativeai.GenerativeModel
import com.privacyshield.android.Component.Screen.Overview.Utility.GpuInfoRenderer
import com.privacyshield.android.Component.Screen.Permission.getPermissionExplanation
import com.privacyshield.android.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpuTab() {
    var renderer by remember { mutableStateOf("Loading...") }
    var vendor by remember { mutableStateOf("Loading...") }
    var version by remember { mutableStateOf("Loading...") }
    var extensions by remember { mutableStateOf("Loading...") }

    var explanation by remember { mutableStateOf<String?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var showAssistantCard by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
    ) {
        Column {

            // 🔹 Top Loading Indicator (sirf FAB click ke baad dikhega)
            if (isLoading && !showAssistantCard) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color.DarkGray
                )
            }

            // GPU info collect karne ke liye
            AndroidView(
                factory = { context ->
                    GLSurfaceView(context).apply {
                        setEGLContextClientVersion(2)
                        setRenderer(GpuInfoRenderer { r, v, ver, ext ->
                            renderer = r
                            vendor = v
                            version = ver
                            extensions = ext
                        })
                        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                    }
                },
                modifier = Modifier.size(1.dp) // invisible
            )

            // GPU Overview UI
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 🔽 AI Assistant Card
                if (showAssistantCard) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "AI GPU Assistant",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .padding(4.dp)
                                    )
                                }

                                explanation?.let {
                                    Text(it, color = Color.White)
                                }

                                if (explanation != null) {
                                    OutlinedTextField(
                                        value = question,
                                        onValueChange = { question = it },
                                        label = { Text("Ask about GPU", color = Color.White) },
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
                                                        answer = getGpuExplanation(
                                                            renderer,
                                                            vendor,
                                                            version,
                                                            extensions,
                                                            question
                                                        )
                                                        isLoading = false
                                                        question = ""
                                                    }
                                                }
                                            },
                                            enabled = question.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                        ) {
                                            Text("Ask AI", color = Color(0xFF4CAF50))
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

                // GPU Overview heading
                item {
                    Text(
                        "GPU Overview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFE0E0E0)
                    )
                }

                item { InfoCard("Renderer", renderer, Color(0xFF388E3C), Icons.Default.Memory) }
                item { InfoCard("Vendor", vendor, Color(0xFF388E3C), Icons.Default.DeveloperMode) }
                item { InfoCard("OpenGL Version", version, Color(0xFF388E3C), Icons.Default.Info) }
                item {
                    InfoCard(
                        "Extensions",
                        extensions.take(150) + "...",
                        Color(0xFF388E3C),
                        Icons.Default.List
                    )
                }
            }
        }

        // ✅ FloatingActionButton → sidha explanation fetch kare
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    explanation = getGpuExplanation(renderer, vendor, version, extensions)
                    isLoading = false
                    showAssistantCard = true
                }
            },
            containerColor = Color(0xFF4CAF50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_icon), // <-- drawable icon
                contentDescription = "AI Info"
            )
        }
    }
}

suspend fun getGpuExplanation(
    renderer: String,
    vendor: String,
    version: String,
    extensions: String,
    userQuestion: String? = null
): String {
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBnM52QDppM97TN1CMIPm3yyWD0g09vYtA"
    )

    // GPU info ko ek string me combine karte hain
    val gpuInfo = """
        Renderer: $renderer
        Vendor: $vendor
        OpenGL Version: $version
        Extensions: $extensions
    """.trimIndent()

    val prompt = if (userQuestion.isNullOrBlank()) {
        "Explain this GPU in simple language:\n$gpuInfo"
    } else {
        "Here is the GPU info:\n$gpuInfo\n\nNow answer this question in simple terms:\n$userQuestion"
    }

    return try {
        val response = model.generateContent(prompt)
        response.text ?: "No explanation found."
    } catch (e: Exception) {
        "Error: ${e.localizedMessage}"
    }
}

