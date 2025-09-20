package com.privacyshield.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.privacyshield.android.Component.Scanner.FilePickerHelper
import com.privacyshield.android.Component.Scanner.unusgeApp.scheduleUnusedAppsWorker
import com.privacyshield.android.Component.Service.VirusTotalScanService
import com.privacyshield.android.Component.navigation.AppNavGraph
import com.privacyshield.android.ui.theme.PrivacyShieldTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle shared intent when app opens
        handleSharedIntent(intent)

        setContent {
            PrivacyShieldTheme {
                AppNavGraph(activity = this)
                scheduleUnusedAppsWorker(this)
            }
        }
    }

    // ✅ Fixed: Correct method signature for ComponentActivity
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent) // Don't forget to call super!

        // Handle if activity is already running and gets new intent
        handleSharedIntent(intent)
    }

    private fun handleSharedIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                // Single file share
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)

                if (uri != null) {
                    // File shared - start scan
                    startFileScanFromUri(uri)
                } else if (!text.isNullOrEmpty()) {
                    // Text shared (could be a URL)
                    handleSharedText(text)
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                // Multiple files share
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                uris?.forEach { uri ->
                    startFileScanFromUri(uri)
                }
            }
        }
    }

    private fun startFileScanFromUri(uri: Uri) {
        // Use a coroutine to handle file processing
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filePath = getFilePathFromUri(uri)
                if (filePath != null) {
                    // Start the scan service
                    val scanIntent = Intent(this@MainActivity, VirusTotalScanService::class.java).apply {
                        putExtra("files", arrayOf(filePath))
                        putExtra("scanMode", "SINGLE")
                        putExtra("source", "share")
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(scanIntent)
                    } else {
                        startService(scanIntent)
                    }

                    // Show notification
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Scanning shared file...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Could not access the shared file",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error processing file: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleSharedText(text: String) {
        // Check if it's a URL that can be scanned
        if (isValidUrl(text)) {
            // You can handle URL scanning here
            Toast.makeText(
                this,
                "URL scanning feature coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Text received: ${text.take(50)}...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isValidUrl(text: String): Boolean {
        return Patterns.WEB_URL.matcher(text).matches()
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        // Make sure you have FilePickerHelper implementation
        return try {
            FilePickerHelper(this).getFilePathFromUri(uri)
        } catch (e: Exception) {
            null
        }
    }
}