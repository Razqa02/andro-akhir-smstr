package com.example.tiltball.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tiltball.ui.theme.*
import com.example.tiltball.viewmodel.HistoryViewModel

/**
 * Layar menu utama: Desain arcade modern, tombol aksi besar,
 * badge skor tertinggi, dan navigasi yang rapi & intuitif.
 */
@Composable
fun MainMenuScreen(
    historyVm: HistoryViewModel,
    onPlayClick: () -> Unit,
    onHighScoreClick: () -> Unit,
    onHowToPlayClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSensorsClick: () -> Unit
) {
    val uiState by historyVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        historyVm.refresh()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Bagian Header / Logo ─────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                AnimatedLogoBall()

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "TILT BALL",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        letterSpacing = 3.sp
                    ),
                    fontWeight = FontWeight.Black,
                    color = Primary
                )

                Text(
                    text = "Miringkan HP & taklukkan labirin!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                // Badge Skor Tertinggi
                if (!uiState.isLoading && uiState.highestScore > 0) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Warning.copy(alpha = 0.5f)),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Skor Tertinggi: ${uiState.highestScore}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Warning
                            )
                        }
                    }
                }
            }

            // ─── Tombol Utama & Opsi Cepat ───────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                // Tombol Main Utama (Besar & Menyala)
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = Primary, spotColor = Primary),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "MULAI MAIN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = OnPrimary
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Menu Cepat Baris 1: Pilih Level & Riwayat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MenuOptionCard(
                        icon = Icons.Default.ViewModule,
                        label = "Pilih Level",
                        onClick = onPlayClick,
                        modifier = Modifier.weight(1f)
                    )
                    MenuOptionCard(
                        icon = Icons.Default.EmojiEvents,
                        label = "Riwayat & Rekor",
                        onClick = onHighScoreClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Menu Cepat Baris 2: Cara Main & Sensor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MenuOptionCard(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        label = "Cara Bermain",
                        onClick = onHowToPlayClick,
                        modifier = Modifier.weight(1f)
                    )
                    MenuOptionCard(
                        icon = Icons.Default.Sensors,
                        label = "Tes Sensor",
                        onClick = onSensorsClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tombol Tentang
                TextButton(
                    onClick = onAboutClick,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Tentang Aplikasi",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Footer Version
            Text(
                text = "Tilt Ball v2.0 • Offline Physics",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/** Kartu Tombol Menu Sekunder */
@Composable
private fun MenuOptionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        shadowElevation = 2.dp,
        modifier = modifier.height(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }
    }
}

/** Logo Bola dengan Animasi Bernapas Halus */
@Composable
private fun AnimatedLogoBall() {
    val infiniteTransition = rememberInfiniteTransition(label = "ballGlow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(110.dp)
    ) {
        // Glow latar belakang
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = (size.minDimension / 2f) * pulse
            drawCircle(
                color = Primary.copy(alpha = 0.15f),
                radius = r * 1.15f,
                center = Offset(size.width / 2f, size.height / 2f)
            )
        }

        // Bola utama 3D
        Canvas(modifier = Modifier.size(90.dp)) {
            val r = size.minDimension / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Bayangan
            drawCircle(
                color = BallShadowColor,
                radius = r * 1.05f,
                center = Offset(cx + 4f, cy + 8f)
            )

            // Permukaan bola dengan gradien
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BallHighlight,
                        BallColor,
                        Color(0xFF0369A1)
                    ),
                    center = Offset(cx - r * 0.3f, cy - r * 0.3f),
                    radius = r * 1.3f
                ),
                radius = r,
                center = Offset(cx, cy)
            )

            // Specular reflection
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = r * 0.22f,
                center = Offset(cx - r * 0.35f, cy - r * 0.35f)
            )
        }
    }
}
