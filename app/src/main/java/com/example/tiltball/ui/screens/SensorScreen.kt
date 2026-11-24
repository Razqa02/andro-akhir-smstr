package com.example.tiltball.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiltball.sensor.SensorState
import com.example.tiltball.ui.theme.*
import com.example.tiltball.viewmodel.SensorViewModel
import com.example.tiltball.viewmodel.ViewModelFactory

/**
 * Layar Diagnostik & Kalibrasi Sensor:
 *  1. Ringkasan sensor wajib (Accelerometer + Magnetometer)
 *  2. Tiga sensor target (Accelerometer, Magnetometer, Proximity) dengan status dan nilai live
 *  3. Slider sensitivitas, kalibrasi tilt, toggle auto-pause proximity, tombol set ulang arah awal
 *  4. Daftar semua sensor perangkat fisik (LazyColumn)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorScreen(
    factory: ViewModelFactory,
    onBack: () -> Unit
) {
    val sensorVm: SensorViewModel = viewModel(factory = factory)

    val accelState by sensorVm.accelerometerState.collectAsStateWithLifecycle()
    val magnetState by sensorVm.magnetometerState.collectAsStateWithLifecycle()
    val proximityState by sensorVm.proximityState.collectAsStateWithLifecycle()

    val tiltData by sensorVm.tiltData.collectAsStateWithLifecycle()
    val compassData by sensorVm.compassData.collectAsStateWithLifecycle()
    val isNear by sensorVm.isNear.collectAsStateWithLifecycle()

    val offsetX by sensorVm.offsetX.collectAsStateWithLifecycle()
    val offsetY by sensorVm.offsetY.collectAsStateWithLifecycle()
    val sensitivity by sensorVm.sensitivity.collectAsStateWithLifecycle()
    val autoPause by sensorVm.autoPauseProximity.collectAsStateWithLifecycle()
    val compassRef by sensorVm.compassHeadingRef.collectAsStateWithLifecycle()

    var showCalibratedToast by remember { mutableStateOf(false) }

    // Hitung relativeHeading live untuk keperluan pengujian diagnostik
    var liveDiff = compassData.azimuthDegrees - compassRef
    while (liveDiff > 180f) liveDiff -= 360f
    while (liveDiff < -180f) liveDiff += 360f

    val isRequiredSensorsOk = accelState == SensorState.TERSEDIA && magnetState == SensorState.TERSEDIA

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Diagnostik & Sensor",
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // ─── 1. Ringkasan Sensor Wajib ────────────────────────────────────
            item(key = "required_sensors_summary") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRequiredSensorsOk)
                            Success.copy(alpha = 0.12f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isRequiredSensorsOk) Success.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRequiredSensorsOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isRequiredSensorsOk) Success else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sensor Wajib (Accelerometer + Magnetometer):",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = if (isRequiredSensorsOk) "SEMUA SENSOR WAJIB AKTIF" else "BERMASALAH / TIDAK LENGKAP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isRequiredSensorsOk) Success else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // ─── 2. Accelerometer (Motion) ────────────────────────────────────
            item(key = "accelerometer_card") {
                SensorDetailCard(
                    title = "1. Accelerometer (Motion)",
                    state = accelState
                ) {
                    if (accelState == SensorState.TERSEDIA) {
                        Text(
                            text = "Gaya Kemiringan Layar (X = horizontal, Y = vertikal):",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "X: %+.2f m/s²   Y: %+.2f m/s²   Z: %+.2f m/s²".format(
                                tiltData.x, tiltData.y, tiltData.z
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Offset Kalibrasi Nol: X = %+.2f, Y = %+.2f".format(offsetX, offsetY),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    sensorVm.calibrate()
                                    showCalibratedToast = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Kalibrasi Nol", color = OnPrimary, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { sensorVm.resetCalibration() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SurfaceBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }
                        }

                        if (showCalibratedToast) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "✓ Offset posisi nyaman berhasil disimpan!",
                                color = Success,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Sensitivitas Gerak Bola: %.2fx".format(sensitivity),
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = sensitivity,
                            onValueChange = { sensorVm.setSensitivity(it) },
                            valueRange = 0.5f..2.0f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Primary,
                                inactiveTrackColor = SurfaceVariant
                            )
                        )
                    } else {
                        Text(
                            text = if (accelState == SensorState.TIDAK_ADA)
                                "Sensor Accelerometer tidak ditemukan pada perangkat ini."
                            else
                                "Sensor ada tetapi tidak merespons (timeout 2 detik).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ─── 3. Magnetometer (Position / Gerbang Kompas) ─────────────────
            item(key = "magnetometer_card") {
                SensorDetailCard(
                    title = "2. Magnetometer / Kompas (Position)",
                    state = magnetState
                ) {
                    if (magnetState == SensorState.TERSEDIA) {
                        Text(
                            text = "Kekuatan Medan Magnet:",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "%.1f µT (Normal: 25 - 65 µT)".format(compassData.magneticFieldStrengthUt),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (compassData.magneticFieldStrengthUt in 20f..80f) Secondary else Warning
                        )

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Vektor Raw: X=%+.1f, Y=%+.1f, Z=%+.1f µT".format(
                                compassData.magX, compassData.magY, compassData.magZ
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Arah Azimuth:", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text(
                                    "%.1f°".format(compassData.azimuthDegrees),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Arah Relatif Acuan:", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text(
                                    "%+.1f°".format(liveDiff),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Secondary
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { sensorVm.resetCompassHeading() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set Ulang Arah Acuan Kompas (Nolkan)", color = OnPrimary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "💡 Tips: Putar HP membentuk angka 8 di udara jika nilai kompas terasa melompat atau macet.",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    } else {
                        Text(
                            text = if (magnetState == SensorState.TIDAK_ADA)
                                "Sensor Magnetometer tidak ditemukan pada HP ini."
                            else
                                "Sensor ada tetapi tidak merespons (timeout 2 detik).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ─── 4. Proximity (Position / Opsional Auto-pause) ────────────────
            item(key = "proximity_card") {
                SensorDetailCard(
                    title = "3. Proximity (Position - Opsional)",
                    state = proximityState
                ) {
                    val isAvail = proximityState == SensorState.TERSEDIA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-pause saat layar ditutup:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurface
                            )
                            Text(
                                text = if (isAvail)
                                    "Otomatis jeda saat sensor di dahi HP terhalang tangan/meja"
                                else
                                    "Sensor jarak tidak tersedia di perangkat ini",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoPause,
                            enabled = isAvail,
                            onCheckedChange = { sensorVm.toggleAutoPauseProximity(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Primary,
                                checkedTrackColor = Primary.copy(alpha = 0.5f),
                                uncheckedThumbColor = OnSurfaceVariant,
                                uncheckedTrackColor = SurfaceVariant
                            )
                        )
                    }

                    if (isAvail) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status Sensor Saat Ini:", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            Surface(
                                color = if (isNear) Warning.copy(alpha = 0.2f) else Success.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (isNear) Warning else Success),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isNear) "🚨 DEKAT (TERTUTUP)" else "🍃 JAUH (TERBUKA)",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isNear) Warning else Success
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorDetailCard(
    title: String,
    state: SensorState,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )

                val (icon, color, label) = when (state) {
                    SensorState.TERSEDIA -> Triple(Icons.Default.CheckCircle, Success, "Tersedia")
                    SensorState.TIDAK_ADA -> Triple(Icons.Default.Error, MaterialTheme.colorScheme.error, "Tidak Ada")
                    SensorState.TIDAK_MERESPONS -> Triple(Icons.Default.Warning, Warning, "Tidak Merespons")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
