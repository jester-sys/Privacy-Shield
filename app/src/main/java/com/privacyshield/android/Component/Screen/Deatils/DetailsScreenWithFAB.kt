package com.privacyshield.android.Component.Screen.Deatils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.privacyshield.android.Component.Screen.Permission.getPermissionExplanation
import com.privacyshield.android.Model.AppDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun DetailsScreenWithAISuggestions(
    app: AppDetail,
    navController: NavHostController
) {
    var showAiDialog by remember { mutableStateOf(false) }
    var selectedSuggestion by remember { mutableStateOf<String?>(null) }
    var aiAnswer by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val suggestions = listOf(
        "Explain the permissions used by ${app.appName} in simple words",
        "Is ${app.appName} safe to install on my phone?",
        "What is the main purpose of ${app.appName}?",
        "Does ${app.appName} require access to sensitive data?",
        "How frequently is ${app.appName} updated?",
        "Can ${app.appName} access my personal information?",
        "Does ${app.appName} need location, camera, or contacts access?",
        "Is ${app.appName} suitable for kids?",
        "Can ${app.appName} run without internet?",
        "How much battery does ${app.appName} consume?"
    )

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Existing DetailsScreen
        DetailsScreen(app = app, navController = navController, onPermissionClick = {})

        // FAB
        FloatingActionButton(
            onClick = {
                showAiDialog = true
                selectedSuggestion = null
                aiAnswer = null
            },
            containerColor = Color(0xFF9C27B0),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "AI Info")
        }

        // AI Dialog
        if (showAiDialog) {
            AlertDialog(
                onDismissRequest = { showAiDialog = false },
                title = {
                    Text(
                        text = "AI Suggestions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFEEEEEE)
                    )
                },
                text = {
                    Box(
                        modifier = Modifier
                            .heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {

                            // AI Answer Box
                            if (aiAnswer != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFF9C27B0).copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(vertical = 16.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = aiAnswer ?: "",
                                        fontSize = 16.sp,
                                        color = Color(0xFFEEEEEE) // light text
                                    )
                                }
                            } else {
                                // Suggestions List
                                suggestions.forEach { suggestion ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable {
                                                selectedSuggestion = suggestion
                                                isLoading = true

                                                coroutineScope.launch {
                                                    val answer = try {
                                                        getPermissionExplanation(permission = suggestion)
                                                    } catch (e: Exception) {
                                                        "Error: ${e.localizedMessage}"
                                                    }
                                                    aiAnswer = answer
                                                    isLoading = false
                                                }
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2A2A3C) // dark card
                                        ),
                                        elevation = CardDefaults.cardElevation(6.dp)
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {

                                            Text(
                                                text = suggestion,
                                                color = Color(0xFFEEEEEE), // light text
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Loading Indicator
                            if (isLoading) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF9C27B0),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Loading AI Answer...",
                                        color = Color(0xFFEEEEEE),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAiDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = Color.White)
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = Color(0xFF1E1E1E) // dark background
            )
        }
    }
}



