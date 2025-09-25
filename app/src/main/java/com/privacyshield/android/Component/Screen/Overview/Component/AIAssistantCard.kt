package com.privacyshield.android.Component.Screen.Overview.Component


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privacyshield.android.Component.Settings.theme.AppSettings

@Composable
fun AIAssistantCard(
    explanation: String?,
    question: String,
    answer: String?,
    isLoading: Boolean,
    textColor: Color,
    primaryColor: Color,
    appSettings: AppSettings,
    onAsk: (String) -> Unit,
    onClear: () -> Unit,
    onQuestionChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "AI Assistant",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = primaryColor,
                    modifier = Modifier.size(28.dp).padding(4.dp)
                )
            }

            explanation?.let { Text(it, color = textColor) }

            if (explanation != null) {
                OutlinedTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    label = { Text("Ask About Device", color = textColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = if (appSettings.highContrast)
                            textColor.copy(alpha = 0.7f)
                        else primaryColor.copy(alpha = 0.4f),
                        cursorColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = textColor.copy(alpha = 0.6f)
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onAsk(question) },
                        enabled = question.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = if (appSettings.highContrast) {
                                if (appSettings.darkTheme) Color.Black else Color.White
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    ) { Text("Ask AI") }

                    OutlinedButton(
                        onClick = onClear,
                        border = BorderStroke(1.dp, primaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryColor
                        )
                    ) { Text("Clear Chat") }
                }

                answer?.let { Text("Answer: $it", color = textColor) }
            }
        }
    }
}
