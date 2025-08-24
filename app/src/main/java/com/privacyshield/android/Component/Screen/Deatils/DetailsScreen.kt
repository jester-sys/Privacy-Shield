package com.privacyshield.android.Component.Screen.Deatils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Model.AppDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(app:AppDetail){
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(app.appName) })
        }
    ) { paddingValues ->
        Column(
            modifier =    Modifier.padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app.packageName)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = app.appName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = app.packageName, fontSize = 14.sp, color = Color.Gray)

                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Version: ${app.versionName} (${app.versionCode})", fontSize = 14.sp)
            Text("Min SDK: ${app.minSdk}", fontSize = 14.sp)
            Text("Target SDK: ${app.targetSdk}", fontSize = 14.sp)
            Text("Compile SDK: ${app.compileSdk}", fontSize = 14.sp)
            Text("First Installed: ${app.firstInstallTime}", fontSize = 14.sp)
            Text("Last Updated: ${app.lastUpdateTime}", fontSize = 14.sp)
            Text("Source Dir: ${app.sourceDir}", fontSize = 14.sp)
            Text(
                text = if (app.isSystemApp) "System App" else "User App",
                fontSize = 14.sp,
                color = if (app.isSystemApp) Color.Red else Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Permissions:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            app.permissions.forEach { perm ->
                Text("• $perm", fontSize = 13.sp, modifier = Modifier.padding(4.dp))
            }
        }
    }

}