package com.example.tiltball.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiltball.sensor.CompassData
import com.example.tiltball.sensor.SensorController
import com.example.tiltball.sensor.SensorState
import com.example.tiltball.sensor.TiltData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PREFS_NAME = "tiltball_prefs"
private const val KEY_OFFSET_X = "offset_x"
private const val KEY_OFFSET_Y = "offset_y"
private const val KEY_SENSITIVITY = "sensitivity"
private const val KEY_AUTO_PAUSE_PROXIMITY = "auto_pause_proximity"

/**
 * ViewModel untuk layar SensorScreen (diagnostik & kalibrasi).
 */
class SensorViewModel(
    application: Application,
    val sensorController: SensorController
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ─── Status 3 Sensor Target ─────────────────────────────────────────────
    val accelerometerState: StateFlow<SensorState> = sensorController.accelerometerState
    val magnetometerState: StateFlow<SensorState> = sensorController.magnetometerState
    val proximityState: StateFlow<SensorState> = sensorController.proximityState

    val proximityMaxRange = sensorController.proximityMaxRange

    // ─── Data Live Sensor ───────────────────────────────────────────────────
    private val _tiltData = MutableStateFlow(TiltData(0f, 0f, 0f))
    val tiltData: StateFlow<TiltData> = _tiltData.asStateFlow()

    private val _compassData = MutableStateFlow(
        CompassData(
            azimuthDegrees = 0f,
            magneticFieldStrengthUt = 0f,
            accuracy = 0,
            isReliable = true,
            magX = 0f,
            magY = 0f,
            magZ = 0f
        )
    )
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()

    private val _isNear = MutableStateFlow(false)
    val isNear: StateFlow<Boolean> = _isNear.asStateFlow()

    // ─── Heading Acuan untuk Diagnostik Layar Sensor ─────────────────────────
    private val _compassHeadingRef = MutableStateFlow(0f)
    val compassHeadingRef: StateFlow<Float> = _compassHeadingRef.asStateFlow()

    // ─── Pengaturan ──────────────────────────────────────────────────────────
    private val _offsetX = MutableStateFlow(prefs.getFloat(KEY_OFFSET_X, 0f))
    val offsetX: StateFlow<Float> = _offsetX.asStateFlow()

    private val _offsetY = MutableStateFlow(prefs.getFloat(KEY_OFFSET_Y, 0f))
    val offsetY: StateFlow<Float> = _offsetY.asStateFlow()

    private val _sensitivity = MutableStateFlow(prefs.getFloat(KEY_SENSITIVITY, 1.0f))
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private val _autoPauseProximity = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_PAUSE_PROXIMITY, false) // Default: MATI
    )
    val autoPauseProximity: StateFlow<Boolean> = _autoPauseProximity.asStateFlow()

    // Daftar semua sensor fisik perangkat
    val allSensors: List<Sensor> = sensorController.getAllDeviceSensors()

    private var rawTiltX = 0f
    private var rawTiltY = 0f

    init {
        viewModelScope.launch {
            sensorController.tiltFlow.collect { data ->
                rawTiltX = data.x
                rawTiltY = data.y
                _tiltData.value = data
            }
        }
        viewModelScope.launch {
            sensorController.compassFlow.collect { comp ->
                _compassData.value = comp
            }
        }
        viewModelScope.launch {
            sensorController.proximityFlow.collect { near ->
                _isNear.value = near
            }
        }
    }

    /** Kalibrasi kemiringan nol */
    fun calibrate() {
        val ox = rawTiltX
        val oy = rawTiltY
        _offsetX.value = ox
        _offsetY.value = oy
        prefs.edit()
            .putFloat(KEY_OFFSET_X, ox)
            .putFloat(KEY_OFFSET_Y, oy)
            .apply()
    }

    fun resetCalibration() {
        _offsetX.value = 0f
        _offsetY.value = 0f
        prefs.edit()
            .putFloat(KEY_OFFSET_X, 0f)
            .putFloat(KEY_OFFSET_Y, 0f)
            .apply()
    }

    fun setSensitivity(value: Float) {
        val clamped = value.coerceIn(0.5f, 2.0f)
        _sensitivity.value = clamped
        prefs.edit().putFloat(KEY_SENSITIVITY, clamped).apply()
    }

    /** Set ulang arah awal kompas untuk pengujian di layar SensorScreen */
    fun resetCompassHeading() {
        _compassHeadingRef.value = _compassData.value.azimuthDegrees
    }

    /** Toggle fitur auto-pause sensor jarak (disimpan di SharedPreferences) */
    fun toggleAutoPauseProximity(enabled: Boolean) {
        _autoPauseProximity.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_PAUSE_PROXIMITY, enabled).apply()
    }
}
