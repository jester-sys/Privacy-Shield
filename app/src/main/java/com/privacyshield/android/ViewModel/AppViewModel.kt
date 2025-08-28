package com.privacyshield.android.ViewModel

import androidx.lifecycle.ViewModel
import com.privacyshield.android.Model.AppDetail

class AppViewModel: ViewModel() {
    var allApps: List<AppDetail> = emptyList()
    var selectedApp: AppDetail? = null
}
