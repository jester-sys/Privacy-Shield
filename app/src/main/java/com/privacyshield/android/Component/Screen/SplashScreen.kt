package com.privacyshield.android.Component.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.privacyshield.android.R
import kotlinx.coroutines.delay

@Composable

fun SplashScreen(modifier: Modifier = Modifier,onNavigate :()-> Unit) {

    LaunchedEffect(Unit) {
        delay(2000)
        onNavigate()
    }
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        Image(painter = painterResource(R.drawable.app_icon), contentDescription = null,modifier = modifier.align(
            Alignment.Center).size(150.dp))
    }
}