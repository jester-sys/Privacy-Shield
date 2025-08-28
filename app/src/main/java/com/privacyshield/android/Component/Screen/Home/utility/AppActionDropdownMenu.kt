package com.privacyshield.android.Component.Screen.Home.utility

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.palette.graphics.Palette
import coil.compose.rememberAsyncImagePainter
import com.privacyshield.android.Component.Helper.AppIcon
import com.privacyshield.android.Component.Helper.toPainter
import com.privacyshield.android.Model.AppDetail
import com.privacyshield.android.R
import com.privacyshield.android.ui.theme.TextPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActionPopupMenu(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    app: AppDetail,
    onAction: (AppDetail, String) -> Unit
) {
    if (showSheet) {
        val context = LocalContext.current
        val dominantColor = remember { mutableStateOf(Color(0xFF1E1E1E)) } // default dark gray
        LaunchedEffect(app.packageName) {
            try {
                val drawable = context.packageManager.getApplicationIcon(app.packageName)
                val bitmap = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

                Palette.from(bitmap).generate { palette ->
                    palette?.dominantSwatch?.rgb?.let {
                        dominantColor.value = Color(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF121212),
            tonalElevation = 8.dp
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // App Icon & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,){
                        AppIcon(app.packageName)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = app.appName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = app.versionName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Action Buttons
                val actions = listOf(
                    Triple("Open", Icons.Default.PlayArrow, Color(0xFF4CAF50)),
                    Triple("Uninstall", Icons.Default.Delete, Color(0xFFF44336)),
                    Triple("Share", Icons.Default.Share, Color(0xFF2196F3))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    actions.forEach { (label, icon, color) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                    onAction(app, label.lowercase())
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = label,
                                    tint = color,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp,)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                val infoItems = listOf(
                    Triple("Data Usage", R.drawable.ic_database_icon, Color(0xFF4CAF50)),
                    Triple("Battery Usage", R.drawable.ic_battery_icon, Color(0xFFFF9800)),
                    Triple("Storage Usage", R.drawable.ic_storage_icon, Color(0xFF2196F3)),
                    Triple("Permissions", Icons.Default.Settings, Color(0xFFF44336)),
                    Triple("Open By Default", R.drawable.ic_browser_icon, Color(0xFF9C27B0))
                )

                infoItems.forEach { (label, iconResource, color) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onDismiss()
                                onAction(app, label.lowercase().replace(" ", "_"))
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), // lighter background
                                contentAlignment = Alignment.Center
                            ) {
                                if (iconResource is ImageVector) {
                                    Icon(iconResource, contentDescription = label, tint = color)
                                } else if (iconResource is Int) { // Drawable resource
                                    Image(
                                        painter = painterResource(id = iconResource),
                                        contentDescription = label,
                                        modifier = Modifier.size(24.dp),
                                        colorFilter = ColorFilter.tint(color)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


            }
        }
    }
}