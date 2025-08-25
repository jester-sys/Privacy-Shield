package com.privacyshield.android.Component.Screen.Home.utility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Model.AppPermission


fun getPermissionDetails(perm: AppPermission,): AnnotatedString {
    return when (perm.name) {
        android.Manifest.permission.CAMERA -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Camera Permission:\n\n") // extra \n for spacing
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Allows the app to take pictures and record videos using your device camera.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Front and rear camera access.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Malicious apps can record video or take photos without your knowledge, leading to privacy invasion.\n")
            }
        }
        android.Manifest.permission.RECORD_AUDIO -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Microphone Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Allows the app to record audio.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access to mic input for calls, voice messages, or voice commands.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Voice recording, audio messages, video calls.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Unauthorized recording can capture private conversations.\n")
            }
        }
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Media Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access to your photos and videos.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Read-only access to images/videos.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Uploading photos/videos, gallery previews.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Apps can view personal media, potentially leaking sensitive photos/videos.\n")
            }
        }


        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Media Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access to your photos and videos.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Read-only access to images/videos.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Uploading photos/videos, gallery previews.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Apps can view personal media, potentially leaking sensitive photos/videos.\n")
            }
        }

        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Location Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access your device's GPS or network-based location.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Fine location (GPS), coarse location (network/cell towers).\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Maps, navigation, location-based services.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Apps can track your movement, creating privacy issues or unwanted ads.\n")
            }
        }

        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.WRITE_CONTACTS -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Contacts Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access to your contacts stored on the device.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Read names, phone numbers, emails; write/edit contacts.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Syncing contacts, calling, messaging apps.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Can leak your contacts to third-party apps or spam calls/messages.\n")
            }
        }

        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.SEND_SMS -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("SMS Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Read and send SMS messages.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Reading inbox/outbox, sending messages.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Verification codes, messaging features.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Apps can read sensitive messages (OTP, personal info) or send unauthorized messages.\n")
            }
        }

        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.WRITE_CALL_LOG -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Call Log Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access your device's call history.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Read call numbers, timestamps, call duration; edit/delete logs.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Call tracking, history syncing.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Privacy invasion, potential spam or fraudulent use of call history.\n")
            }
        }

        android.Manifest.permission.INTERNET -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Internet Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access the internet.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Network requests, cloud services access.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Data fetching, syncing, APIs.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Excessive data usage, tracking user behavior, potential malware downloads.\n")
            }
        }

        android.Manifest.permission.ACCESS_NETWORK_STATE -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Network State Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Checks network connectivity and status.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Detect Wi-Fi, mobile data status.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Decide online/offline mode, optimize data usage.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Can track network activity patterns for profiling.\n")
            }
        }

        android.Manifest.permission.READ_CALENDAR,
        android.Manifest.permission.WRITE_CALENDAR -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Calendar Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access your calendar events.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Read/write calendar events.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Event reminders, syncing with apps.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Apps can see private event details.\n")
            }
        }

        android.Manifest.permission.BODY_SENSORS -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Body Sensors Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Access to device sensors like heart rate or step count.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Read sensor data.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Fitness tracking, health monitoring apps.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Apps can collect sensitive health information.\n")
            }
        }

        android.Manifest.permission.VIBRATE -> buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                append("Vibration Permission:\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Allows ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Allows the app to trigger device vibration.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("- Sub-permissions ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Vibrate on notifications or events.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Use case ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Alerts, notifications, haptic feedback.\n\n")
            }
            withStyle(SpanStyle(color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                append("- Risks ->\n")
            }
            withStyle(SpanStyle(color = Color(0xFFB0BEC5), fontWeight = FontWeight.Medium, fontSize = 16.sp)) {
                append("Excessive vibration can be annoying.\n")
            }
        }

        else -> buildAnnotatedString {
            append("Other Permission:\n- Details not available.")
        }
    }

    //  Text(text = annotatedString, fontSize = 14.sp)
}
