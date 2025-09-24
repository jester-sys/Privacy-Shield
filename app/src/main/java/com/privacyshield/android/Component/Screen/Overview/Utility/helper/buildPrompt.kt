package com.privacyshield.android.Component.Screen.Overview.Utility.helper

import com.privacyshield.android.Component.Screen.Overview.Utility.ExplanationType

// Helper function to build prompt
 fun buildPrompt(type: ExplanationType, infoString: String, userQuestion: String?): String {
    val componentName = when (type) {
        ExplanationType.CPU -> "CPU"
        ExplanationType.GPU -> "GPU"
        ExplanationType.STORAGE -> "Storage"
        ExplanationType.ANDROID -> "Android OS"
        ExplanationType.RAM -> "RAM"
        ExplanationType.HARDWARE -> "Hardware"
        ExplanationType.SENSORS -> "Sensors"
    }

    return """
        You are a technical expert explaining $componentName information to a non-technical user.
        
        $componentName Information:
        $infoString
        
        ${if (userQuestion.isNullOrBlank()) "Explain this $componentName information in very simple, easy-to-understand terms." else "Question: $userQuestion\n\nAnswer in simple terms:"}
        
        Keep the response concise and user-friendly.
    """.trimIndent()
}