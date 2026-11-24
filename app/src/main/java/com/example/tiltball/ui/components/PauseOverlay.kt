package com.example.tiltball.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SensorOccupied
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiltball.ui.theme.*
import com.example.tiltball.viewmodel.PauseReason

/**
 * Overlay saat game dijeda: Desain kartu melayang modern, status sensor jarak,
 * tips kompas, dan tombol aksi yang jelas.
 */
@Composable
fun PauseOverlay(
    pauseReason: PauseReason,
    isProximityNear: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Jeda
                Text(
                    text = "PERMAINAN DIJEDA",
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 1.sp),
                    fontWeight = FontWeight.Black,
                    color = Primary
                )

                // Alasan jeda jika dari sensor jarak (Proximity)
                if (pauseReason == PauseReason.PROXIMITY) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isProximityNear) Error.copy(alpha = 0.5f) else Success.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.SensorOccupied,
                                contentDescription = null,
                                tint = if (isProximityNear) Error else Success,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isProximityNear) "Sensor Jarak Terhalang" else "Sensor Jarak Bebas",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProximityNear) Error else Success
                                )
                                Text(
                                    text = if (isProximityNear) "Jauhkan tangan dari dahi HP untuk lanjut" else "Tekan Lanjutkan di bawah",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Tips Navigasi Kompas
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CompassCalibration,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Tips Kompas: Putar badan sesuai petunjuk di atas layar, atau gunakan tombol 'Buka Manual' di HUD jika di ruangan sempit.",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Tombol Lanjutkan
                Button(
                    onClick = onResume,
                    enabled = !isProximityNear,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OnPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isProximityNear) "Jauhkan Tangan Dahulu" else "LANJUTKAN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnPrimary
                    )
                }

                // Tombol Ulangi Level
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ulangi Level Ini",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                }

                // Tombol Keluar ke Menu
                TextButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Keluar ke Menu Utama",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
