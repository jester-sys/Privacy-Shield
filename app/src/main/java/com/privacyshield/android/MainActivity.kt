package com.privacyshield.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.privacyshield.android.Component.Scanner.FilePickerHelper
import com.privacyshield.android.Component.Scanner.unusgeApp.scheduleUnusedAppsWorker
import com.privacyshield.android.Component.Service.VirusTotalScanService
import com.privacyshield.android.Component.Settings.theme.AppSettingsRepository
import com.privacyshield.android.Component.Settings.theme.providable.LocalAppSettings
import com.privacyshield.android.Component.navigation.AppNavGraph
import com.privacyshield.android.ViewModel.MainViewModel
import com.privacyshield.android.ui.theme.PrivacyShieldTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject




@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    private val mainViewModel: MainViewModel by viewModels()

    // File picker launcher for widget
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                startFileScanFromUri(uri, "widget")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        mainViewModel.initializeData(this)

        // Handle initial intent
        handleIncomingIntent(intent)

        setContent {
            val userPreferences by appSettingsRepository.data.collectAsStateWithLifecycle(
                initialValue = null
            )

            if (userPreferences == null) {
                // Loading indicator can be shown here
                return@setContent
            }

            CompositionLocalProvider(LocalAppSettings provides userPreferences!!) {
                PrivacyShieldTheme(
                    themeColor = userPreferences!!.themeColor.color,
                    isDynamicColor = userPreferences!!.dynamicColor,
                    theme = userPreferences!!.darkTheme,
                    contrastMode = userPreferences!!.highContrast
                ) {
                    AppNavGraph(activity = this@MainActivity, viewModel = mainViewModel)

                    LaunchedEffect(userPreferences) {
                        val enableExperimental = userPreferences!!.enableExperimentalDetections
                        mainViewModel.performTask(
                            this@MainActivity,
                            packageManager,
                            enableExperimental
                        )
                    }
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent) // Don't forget to call super!
        intent?.let { handleIncomingIntent(it) }
    }

    private fun handleIncomingIntent(intent: Intent) {
        when {
            // Widget actions
            intent.hasExtra("action") -> handleWidgetIntent(intent)
            // Share actions
            intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE ->
                handleSharedIntent(intent)
            // Custom actions from widget
            intent.action == "WIDGET_UPLOAD_ACTION" -> openFilePickerForWidget()
            intent.action == "WIDGET_SEARCH_ACTION" -> handleSearchAction()
        }
    }

    private fun handleWidgetIntent(intent: Intent) {
        when (intent.getStringExtra("action")) {
            "search" -> handleSearchAction()
            "upload" -> openFilePickerForWidget()
        }
    }

    private fun handleSearchAction() {
        // Navigate to search screen
        // For now, show toast
        Toast.makeText(this, "Opening search...", Toast.LENGTH_SHORT).show()

        // You can navigate to search screen here
        // mainViewModel.navigateToSearch()
    }

    private fun openFilePickerForWidget() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        filePickerLauncher.launch(intent)
    }

    private fun handleSharedIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)

                uri?.let {
                    startFileScanFromUri(it, "share")
                } ?: text?.let {
                    handleSharedText(it)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                uris?.forEach { uri ->
                    startFileScanFromUri(uri, "share")
                }
            }
        }
    }

    private fun startFileScanFromUri(uri: Uri, source: String) {
        lifecycleScope.launch {
            try {
                showLoadingToast("Processing file...")

                val filePath = getFilePathFromUri(uri)
                if (filePath != null) {
                    startVirusTotalScanService(filePath, source)
                    showSuccessToast("Scan started for: ${getFileNameFromPath(filePath)}")
                } else {
                    showErrorToast("Could not access the file")
                }
            } catch (e: Exception) {
                showErrorToast("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun startVirusTotalScanService(filePath: String, source: String) {
        val scanIntent = Intent(this, VirusTotalScanService::class.java).apply {
            putExtra("files", arrayOf(filePath)) // Yeh change karo
            putExtra("scanMode", "SINGLE") // Yeh bhi change karo (capital letters)
            putExtra("source", source)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(scanIntent)
            } else {
                startService(scanIntent)
            }
            Log.d("ScanService", "Service started successfully for: $filePath")
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start scan service: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    private fun handleSharedText(text: String) {
        if (isValidUrl(text)) {
            // Handle URL scanning
            Toast.makeText(this, "Scanning URL: ${text.take(50)}...", Toast.LENGTH_SHORT).show()
            // mainViewModel.scanUrl(text)
        } else {
            Toast.makeText(this, "Text received: ${text.take(50)}...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidUrl(text: String): Boolean {
        return Patterns.WEB_URL.matcher(text).matches()
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            FilePickerHelper(this).getFilePathFromUri(uri)
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileNameFromPath(filePath: String): String {
        return filePath.substringAfterLast("/", "Unknown File")
    }

    private fun showLoadingToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessToast(message: String) {
        Toast.makeText(this, "✓ $message", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, "✗ $message", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}