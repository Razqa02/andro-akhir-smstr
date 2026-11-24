package com.example.tiltball.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tiltball.data.model.GameResult
import com.example.tiltball.ui.components.ConfirmDialog
import com.example.tiltball.ui.components.EmptyState
import com.example.tiltball.ui.components.formatTime
import com.example.tiltball.ui.theme.*
import com.example.tiltball.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Layar Riwayat & Rekor:
 * - Skor tertinggi keseluruhan
 * - Rekor per level
 * - Daftar riwayat permainan dengan format waktu & penalti
 * - Hapus per item, hapus semua, reset progres
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyVm: HistoryViewModel,
    onBack: () -> Unit
) {
    val uiState by historyVm.uiState.collectAsStateWithLifecycle()

    var itemToDelete by remember { mutableStateOf<GameResult?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showResetProgressConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        historyVm.refresh()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Riwayat & Rekor",
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
                actions = {
                    if (uiState.results.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllConfirm = true }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Hapus Semua Riwayat",
                                tint = OnSurfaceVariant
                            )
                        }
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
            // ─── Kartu Skor Tertinggi Keseluruhan ─────────────────────────────
            item(key = "high_score_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = BorderStroke(1.5.dp, Warning.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "SKOR TERTINGGI KESELURUHAN",
                                style = MaterialTheme.typography.labelMedium,
                                color = Warning,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = if (uiState.highestScore > 0) "${uiState.highestScore}" else "—",
                            style = MaterialTheme.typography.displaySmall,
                            color = if (uiState.highestScore > 0) Warning else OnSurfaceVariant,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // ─── Rekor per level ──────────────────────────────────────────────
            item(key = "level_records_header") {
                Text(
                    text = "Rekor per Level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
            }

            item(key = "level_records_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.progress.forEach { prog ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "L${prog.level}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (prog.unlocked) Primary else OnSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (prog.bestScore > 0) "${prog.bestScore}" else "—",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (prog.bestScore > 0) Warning else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ─── Riwayat Permainan ────────────────────────────────────────────
            item(key = "history_header") {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Permainan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceVariant,
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text(
                            text = "${uiState.results.size} sesi",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (uiState.results.isEmpty()) {
                item(key = "empty_state") {
                    EmptyState(
                        message = "Belum ada riwayat permainan.\nSelesaikan level untuk mencatat rekor barumu!",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(uiState.results, key = { it.id }) { result ->
                    HistoryItemCard(
                        result = result,
                        onDelete = { itemToDelete = result }
                    )
                }
            }

            // ─── Tombol Reset Progres ─────────────────────────────────────────
            item(key = "reset_progress_button") {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showResetProgressConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Reset Progres Level (Kunci Ulang Level 2-5)", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ─── Dialog Konfirmasi Hapus Satu ─────────────────────────────────────────
    itemToDelete?.let { result ->
        ConfirmDialog(
            title = "Hapus Riwayat?",
            message = "Hapus catatan sesi Level ${result.level} (Skor: ${result.score})?",
            confirmLabel = "Hapus",
            onConfirm = {
                historyVm.deleteResult(result.id)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }

    // ─── Dialog Konfirmasi Hapus Semua ────────────────────────────────────────
    if (showDeleteAllConfirm) {
        ConfirmDialog(
            title = "Hapus Semua Riwayat?",
            message = "Semua catatan riwayat permainan akan dihapus. Progres level yang sudah terbuka TIDAK akan dikunci kembali.",
            confirmLabel = "Hapus Semua",
            onConfirm = {
                historyVm.deleteAllResults()
                showDeleteAllConfirm = false
            },
            onDismiss = { showDeleteAllConfirm = false }
        )
    }

    // ─── Dialog Konfirmasi Reset Progres ──────────────────────────────────────
    if (showResetProgressConfirm) {
        ConfirmDialog(
            title = "Reset Progres Level?",
            message = "Level 2-5 akan dikunci kembali dan rekor terbaik akan direset ke 0. Catatan riwayat permainan tetap tersimpan.",
            confirmLabel = "Reset Progres",
            onConfirm = {
                historyVm.resetProgress()
                showResetProgressConfirm = false
            },
            onDismiss = { showResetProgressConfirm = false }
        )
    }
}

@Composable
private fun HistoryItemCard(
    result: GameResult,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(result.completedAt) {
        dateFormat.format(Date(result.completedAt))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Level ${result.level}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    Text(
                        text = "Skor: ${result.score}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Warning
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Waktu: ${formatTime(result.durationMs)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface
                    )

                    if (result.penaltyMs > 0) {
                        Text(
                            text = "+${formatTime(result.penaltyMs)} penalti",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = OnSurfaceVariant
                )
            }
        }
    }
}
