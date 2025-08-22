package com.privacyshield.android.Component.navigation.UtilsScreen


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Model.AppDetail
import java.util.Date


@Composable
fun AppDetailCard(app: AppDetail) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        Image(
            painter = app.icon.toPainter(),  // ✅ convert Drawable to Painter
            contentDescription = app.appName,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = app.appName, fontWeight = FontWeight.Bold)
            Text(text = "Package: ${app.packageName}", fontSize = 12.sp)
            Text(text = "Version: ${app.versionName} (${app.versionCode})", fontSize = 12.sp)
            Text(text = "Min SDK: ${app.minSdk}, Target SDK: ${app.targetSdk}", fontSize = 12.sp)
            Text(text = "Installed: ${Date(app.firstInstallTime)}", fontSize = 10.sp)
            Text(text = "Updated: ${Date(app.lastUpdateTime)}", fontSize = 10.sp)
            Text(text = "Source: ${app.sourceDir}", fontSize = 10.sp)
            Text(text = if (app.isSystemApp) "System App" else "User App", fontSize = 12.sp)
        }
    }
}

