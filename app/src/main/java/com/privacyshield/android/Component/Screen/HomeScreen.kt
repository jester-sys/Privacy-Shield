package com.privacyshield.android.Component.Screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.privacyshield.android.Component.navigation.UtilsScreen.AppDetailCard
import com.privacyshield.android.ViewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint



@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val apps by viewModel.apps.collectAsState()

    if(apps.isEmpty()){
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading apps...")
        }
    }else{
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(apps){apps ->
                AppDetailCard(apps)
                Divider()

            }
        }
    }
}