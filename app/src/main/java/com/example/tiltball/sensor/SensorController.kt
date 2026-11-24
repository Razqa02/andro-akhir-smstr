package com.example.tiltball.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Status ketersediaan dan respon sensor fisik. */
enum class SensorState {
    TERSEDIA,
    TIDAK_ADA,
    TIDAK_MERESPONS
}

/** Data tilt dari accelerometer (m/s²). */
data class TiltData(val x: Float, val y: Float, val z: Float)

/** Data kompas dari gabungan accelerometer dan magnetometer. */
data class CompassData(
    val azimuthDegrees: Float,       // Arah absolut (0..360)
    val magneticFieldStrengthUt: Float, // Kekuatan medan magnet (µT)
    val accuracy: Int,
    val isReliable: Boolean,
    val magX: Float,
    val magY: Float,
    val magZ: Float
)

/**
 * SensorController mengelola 3 sensor:
 *   1. Accelerometer (Motion) - menggerakkan bola
 *   2. Magnetometer  (Position) - Gerbang Kompas
 *   3. Proximity     (Position) - Auto-pause (opsional)
 */
class SensorController(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // ─── Status tiap sensor ─────────────────────────────────────────────────
    private val _accelerometerState = MutableStateFlow(
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
            SensorState.TERSEDIA else SensorState.TIDAK_ADA
    )
    val accelerometerState: StateFlow<SensorState> = _accelerometerState.asStateFlow()

    private val _magnetometerState = MutableStateFlow(
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)
            SensorState.TERSEDIA else SensorState.TIDAK_ADA
    )
    val magnetometerState: StateFlow<SensorState> = _magnetometerState.asStateFlow()

    private val _proximityState = MutableStateFlow(
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null)
            SensorState.TERSEDIA else SensorState.TIDAK_ADA
    )
    val proximityState: StateFlow<SensorState> = _proximityState.asStateFlow()

    val proximityMaxRange: Float =
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.maximumRange ?: 5f

    /** Ambil seluruh daftar sensor fisik di perangkat untuk layar diagnostik. */
    fun getAllDeviceSensors(): List<Sensor> =
        sensorManager.getSensorList(Sensor.TYPE_ALL)

    // ─── Shared arrays untuk perhitungan orientasi kompas ────────────────────
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // ─── A) Accelerometer Flow ──────────────────────────────────────────────
    val tiltFlow: Flow<TiltData> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            _accelerometerState.value = SensorState.TIDAK_ADA
            close()
            return@callbackFlow
        }

        var lastEventTime = 0L
        var filtX = 0f
        var filtY = 0f
        var filtZ = 0f
        val alpha = 0.8f
        val deadZone = 0.3f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                lastEventTime = SystemClock.elapsedRealtime()
                _accelerometerState.value = SensorState.TERSEDIA

                // Simpan juga untuk getRotationMatrix kompas
                gravityValues[0] = alpha * gravityValues[0] + (1 - alpha) * event.values[0]
                gravityValues[1] = alpha * gravityValues[1] + (1 - alpha) * event.values[1]
                gravityValues[2] = alpha * gravityValues[2] + (1 - alpha) * event.values[2]
                hasGravity = true

                // Low-pass filter untuk kontrol bola
                filtX = alpha * filtX + (1 - alpha) * event.values[0]
                filtY = alpha * filtY + (1 - alpha) * event.values[1]
                filtZ = alpha * filtZ + (1 - alpha) * event.values[2]

                val ax = if (kotlin.math.abs(filtX) < deadZone) 0f else filtX
                val ay = if (kotlin.math.abs(filtY) < deadZone) 0f else filtY

                trySend(TiltData(x = -ax, y = ay, z = filtZ))
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        val registered = sensorManager.registerListener(
            listener, sensor, SensorManager.SENSOR_DELAY_GAME
        )
        if (!registered) {
            _accelerometerState.value = SensorState.TIDAK_ADA
            close()
            return@callbackFlow
        }

        // Cek apakah dalam 2 detik sensor merespons
        val watchdogJob = launch {
            delay(2000)
            if (lastEventTime == 0L) {
                _accelerometerState.value = SensorState.TIDAK_MERESPONS
            }
        }

        awaitClose {
            watchdogJob.cancel()
            sensorManager.unregisterListener(listener)
        }
    }.conflate()

    // ─── B) Magnetometer / Compass Flow ─────────────────────────────────────
    val compassFlow: Flow<CompassData> = callbackFlow {
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magSensor == null) {
            _magnetometerState.value = SensorState.TIDAK_ADA
            close()
            return@callbackFlow
        }

        var lastEventTime = 0L
        val magAlpha = 0.9f
        val rMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        // Circular smoothing untuk azimuth (sin & cos average)
        var smoothedSin = 0f
        var smoothedCos = 0f
        val smoothingFactor = 0.85f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                lastEventTime = SystemClock.elapsedRealtime()
                _magnetometerState.value = SensorState.TERSEDIA

                val mx = event.values[0]
                val my = event.values[1]
                val mz = event.values[2]

                // Low pass filter geomagnetic
                geomagneticValues[0] = magAlpha * geomagneticValues[0] + (1 - magAlpha) * mx
                geomagneticValues[1] = magAlpha * geomagneticValues[1] + (1 - magAlpha) * my
                geomagneticValues[2] = magAlpha * geomagneticValues[2] + (1 - magAlpha) * mz
                hasGeomagnetic = true

                // Kekuatan medan magnet dalam microtesla (µT)
                val strength = sqrt(mx * mx + my * my + mz * mz)

                // Jika gravitasi belum tersedia, lewati perhitungan rotasi
                if (!hasGravity) return

                val success = SensorManager.getRotationMatrix(
                    rMatrix, null, gravityValues, geomagneticValues
                )
                if (success) {
                    SensorManager.getOrientation(rMatrix, orientation)
                    var azimuthRad = orientation[0] // rentang -PI .. +PI
                    if (azimuthRad < 0) azimuthRad += (2 * Math.PI).toFloat()

                    // Smoothing rata-rata melingkar
                    smoothedSin = smoothingFactor * smoothedSin + (1 - smoothingFactor) * sin(azimuthRad)
                    smoothedCos = smoothingFactor * smoothedCos + (1 - smoothingFactor) * cos(azimuthRad)
                    var smoothedRad = atan2(smoothedSin, smoothedCos)
                    if (smoothedRad < 0) smoothedRad += (2 * Math.PI).toFloat()

                    val azimuthDeg = Math.toDegrees(smoothedRad.toDouble()).toFloat()
                    val isReliable = event.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE

                    trySend(
                        CompassData(
                            azimuthDegrees = azimuthDeg,
                            magneticFieldStrengthUt = strength,
                            accuracy = event.accuracy,
                            isReliable = isReliable,
                            magX = mx,
                            magY = my,
                            magZ = mz
                        )
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        val registered = sensorManager.registerListener(
            listener, magSensor, SensorManager.SENSOR_DELAY_UI
        )
        if (!registered) {
            _magnetometerState.value = SensorState.TIDAK_ADA
            close()
            return@callbackFlow
        }

        val watchdogJob = launch {
            delay(2000)
            if (lastEventTime == 0L) {
                _magnetometerState.value = SensorState.TIDAK_MERESPONS
            }
        }

        awaitClose {
            watchdogJob.cancel()
            sensorManager.unregisterListener(listener)
        }
    }.conflate()

    // ─── C) Proximity Flow ──────────────────────────────────────────────────
    val proximityFlow: Flow<Boolean> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (sensor == null) {
            _proximityState.value = SensorState.TIDAK_ADA
            close()
            return@callbackFlow
        }

        var lastEventTime = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                lastEventTime = SystemClock.elapsedRealtime()
                _proximityState.value = SensorState.TERSEDIA
                val isNear = event.values[0] < proximityMaxRange
                trySend(isNear)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        val registered = sensorManager.registerListener(
            listener, sensor, SensorManager.SENSOR_DELAY_NORMAL
        )
        if (!registered) {
            _proximityState.value = SensorState.TIDAK_ADA
            close()
            return@callbackFlow
        }

        val watchdogJob = launch {
            delay(2000)
            if (lastEventTime == 0L) {
                _proximityState.value = SensorState.TIDAK_MERESPONS
            }
        }

        awaitClose {
            watchdogJob.cancel()
            sensorManager.unregisterListener(listener)
        }
    }.conflate()
}
