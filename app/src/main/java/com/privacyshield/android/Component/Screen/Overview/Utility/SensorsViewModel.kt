package com.privacyshield.android.Component.Screen.Overview.Utility

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.privacyshield.android.Component.Screen.Overview.Model.SensorInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val _sensors = MutableStateFlow<List<SensorInfo>>(emptyList())
    val sensors: StateFlow<List<SensorInfo>> = _sensors

    init {
        loadSensors()
    }

    private fun loadSensors() {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val list = sm.getSensorList(Sensor.TYPE_ALL).map { sensor ->
            SensorInfo(
                name = sensor.name,
                type = sensor.type,
                vendor = sensor.vendor,
                version = sensor.version,
                resolution = sensor.resolution,
                power = sensor.power,
                maxRange = sensor.maximumRange,
                minDelay = sensor.minDelay
            )
        }
        _sensors.value = list
    }
}
