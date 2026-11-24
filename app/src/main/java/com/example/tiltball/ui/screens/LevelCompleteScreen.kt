package com.example.tiltball.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tiltball.game.Levels
import com.example.tiltball.ui.components.ResultCard
import com.example.tiltball.ui.components.formatTime
import com.example.tiltball.ui.theme.*
import com.example.tiltball.viewmodel.GameViewModel
import com.example.tiltball.viewmodel.HistoryViewModel

/**
 * Layar hasil level selesai: Desain perayaan kemenangan yang memuaskan,
 * ringkasan statistik (Skor, Waktu, Penalti), dan tombol navigasi aksi berikutnya.
 */
@Composable
fun LevelCompleteScreen(
    level: Int,
    gameVm: GameViewModel,
    historyVm: HistoryViewModel,
    onNextLevel: (Int) -> Unit,
    onReplay: () -> Unit,
    onMenu: () -> Unit
) {
    val gameState by gameVm.uiState.collectAsStateWithLifecycle()
    val historyState by historyVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        historyVm.refresh()
    }

    val nextLevel = level + 1
    val nextLevelData = Levels.getLevel(nextLevel)
    val nextUnlocked = historyState.progress.firstOrNull { it.level == nextLevel }?.unlocked ?: false

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // ─── Ikon Trophy & Gelar Menang ──────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Success.copy(alpha = 0.15f))
                    .shadow(16.dp, CircleShape, ambientColor = Success, spotColor = Success)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "LEVEL $level SELESAI!",
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.Black,
                color = OnSurface
            )

            if (gameState.isNewRecord) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Warning.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Warning.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "✨ REKOR BARU TERCAPAI! ✨",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Warning
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── Kartu Statistik Hasil (2x2 Grid) ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultCard(
                    label = "Skor Akhir",
                    value = "${gameState.lastScore}",
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
                ResultCard(
                    label = "Waktu Bersih",
                    value = formatTime(gameState.elapsedMs),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultCard(
                    label = "Penalti Lubang",
                    value = "+${formatTime(gameState.penaltyMs)}",
                    modifier = Modifier.weight(1f)
                )
                ResultCard(
                    label = "Total Waktu",
                    value = formatTime(gameState.totalMs),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // ─── Tombol Aksi Navigasi ─────────────────────────────────────────
            if (nextLevelData != null && nextUnlocked) {
                Button(
                    onClick = { onNextLevel(nextLevel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Primary, spotColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OnPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "LANJUT: LEVEL $nextLevel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onReplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Mainkan Ulang Level $level",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kembali ke Menu Utama",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}
