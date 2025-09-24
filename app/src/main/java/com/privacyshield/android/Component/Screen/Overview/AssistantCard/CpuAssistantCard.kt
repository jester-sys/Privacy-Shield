package com.privacyshield.android.Component.Screen.Overview.AssistantCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import com.privacyshield.android.Component.Screen.Overview.Model.CpuCoreInfo
import com.privacyshield.android.Component.Settings.theme.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun CpuAssistantCard(
    explanation: String?,
    question: String,
    answer: String?,
    isLoading: Boolean,
    primaryColor: Color,
    textColor: Color,
    onAsk: (String) -> Unit,
    onClear: () -> Unit,
    onQuestionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("AI CPU Assistant", fontWeight = FontWeight.Bold, color = textColor)
            if (isLoading) CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(28.dp))
            explanation?.let { Text(it, color = textColor) }
            OutlinedTextField(
                value = question,
                onValueChange = onQuestionChange,
                label = { Text("Ask about CPU", color = textColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = primaryColor,
                    cursorColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = textColor.copy(alpha = 0.6f)
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { if (question.isNotBlank()) onAsk(question) }, enabled = question.isNotBlank()) {
                    Text("Ask AI")
                }
                OutlinedButton(onClick = onClear) { Text("Clear Chat") }
            }
            answer?.let { Text("Answer: $it", color = textColor) }
        }
    }
}