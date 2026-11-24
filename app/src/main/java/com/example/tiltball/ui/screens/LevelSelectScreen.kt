package com.example.tiltball.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tiltball.game.Levels
import com.example.tiltball.ui.components.formatTime
import com.example.tiltball.ui.theme.*
import com.example.tiltball.viewmodel.HistoryViewModel

/**
 * Layar pilih level: Kartu level interaktif, status terkunci/terbuka yang jelas,
 * serta ringkasan rekor terbaik per level.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    historyVm: HistoryViewModel,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by historyVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        historyVm.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pilih Level",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground,
                    navigationIconContentColor = OnBackground
                )
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(Levels.all, key = { it.number }) { levelData ->
                val progress = uiState.progress.firstOrNull { it.level == levelData.number }
                val unlocked = progress?.unlocked ?: (levelData.number == 1)
                val bestScore = progress?.bestScore ?: 0
                val bestTime = progress?.bestTimeMs ?: 0L

                Surface(
                    onClick = { if (unlocked) onLevelSelected(levelData.number) },
                    enabled = unlocked,
                    shape = RoundedCornerShape(18.dp),
                    color = if (unlocked) Surface else SurfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (unlocked) SurfaceBorder else SurfaceBorder.copy(alpha = 0.3f)
                    ),
                    shadowElevation = if (unlocked) 4.dp else 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Badge Nomor Level
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (unlocked) Primary.copy(alpha = 0.15f)
                                        else SurfaceBorder.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        1.dp,
                                        if (unlocked) Primary.copy(alpha = 0.5f)
                                        else SurfaceBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Text(
                                    text = "${levelData.number}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (unlocked) Primary else OnSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            // Detail Info Level
                            Column {
                                Text(
                                    text = levelData.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) OnSurface else OnSurfaceVariant.copy(alpha = 0.5f)
                                )

                                Spacer(Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rating Bintang Level (Tingkat kesulitan)
                                    repeat(levelData.number) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (unlocked) Warning else OnSurfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Batas: ${formatTime(levelData.timeLimitMs)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                }

                                if (unlocked && bestScore > 0) {
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Warning,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Rekor: $bestScore pts • ${formatTime(bestTime)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Warning
                                        )
                                    }
                                }
                            }
                        }

                        // Status Kanan: Panah Masuk atau Ikon Gembok
                        if (unlocked) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Main Level Ini",
                                    tint = Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Terkunci",
                                tint = OnSurfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
