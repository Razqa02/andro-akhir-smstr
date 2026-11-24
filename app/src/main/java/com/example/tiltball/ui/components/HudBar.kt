package com.example.tiltball.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiltball.ui.theme.*
import java.util.Locale

/**
 * HUD bar modern, rapi, dan melayang di bagian atas layar arena.
 * Menampilkan status Level, Timer presisi, Indikator Gerbang Kompas, dan Tombol Jeda.
 */
@Composable
fun HudBar(
    level: Int,
    elapsedMs: Long,
    remainingMs: Long,
    hasGate: Boolean,
    gateOpen: Boolean,
    gateProgress: Float,
    relativeHeading: Float,
    gateInstruction: String,
    showManualGateButton: Boolean,
    onOpenGateManual: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ─── Baris Utama: Kartu Melayang HUD ──────────────────────────────────
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Surface.copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge Level
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Primary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "LEVEL $level",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary,
                        letterSpacing = 1.sp
                    )
                }

                // Timer Utama
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⏱ ${formatTime(elapsedMs)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )

                    Surface(
                        shape = CircleShape,
                        color = if (remainingMs < 15_000) Error.copy(alpha = 0.2f) else SurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (remainingMs < 15_000) Error else SurfaceBorder
                        )
                    ) {
                        Text(
                            text = "Sisa: ${formatTime(remainingMs)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (remainingMs < 15_000) Error else OnSurfaceVariant
                        )
                    }
                }

                // Tombol Jeda Modern
                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Jeda Game",
                        tint = OnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ─── Baris Gerbang Kompas (Hanya muncul jika level memiliki gerbang) ──
        if (hasGate) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Surface.copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (gateOpen) Success.copy(alpha = 0.5f) else Secondary.copy(alpha = 0.4f)
                ),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (gateOpen) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Gerbang Terbuka ✓",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CompassCalibration,
                                contentDescription = null,
                                tint = Secondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(relativeHeading)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = gateInstruction,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Secondary
                                )
                                Text(
                                    text = "Arah: %+.0f° (tahan ~1 detik)".format(relativeHeading),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }

                        // Progress Indicator Penahanan Arah
                        if (gateProgress > 0f) {
                            CircularProgressIndicator(
                                progress = { gateProgress },
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp,
                                color = Secondary,
                                trackColor = SurfaceVariant
                            )
                        }

                        // Tombol Akses Cepat Buka Manual
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onOpenGateManual,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showManualGateButton) Warning else SurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = if (showManualGateButton) OnPrimary else Primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Buka Manual",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (showManualGateButton) OnPrimary else OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format milidetik menjadi "mm:ss.d" (contoh: 01:23.4)
 */
fun formatTime(ms: Long): String {
    val nonNegMs = maxOf(0L, ms)
    val totalSeconds = nonNegMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = (nonNegMs % 1000) / 100
    return String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, tenths)
}
