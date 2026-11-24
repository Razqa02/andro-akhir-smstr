package com.example.tiltball.ui.screens

import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.tiltball.game.Levels
import com.example.tiltball.sensor.SensorController
import com.example.tiltball.ui.components.*
import com.example.tiltball.ui.theme.*
import com.example.tiltball.viewmodel.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Flag Panel Debug Overlay.
 * Set ke false agar tampilan bersih dan nyaman dimainkan.
 */
private const val DEBUG_OVERLAY = false

/**
 * Layar permainan utama Tilt Ball.
 * Menggunakan instance GameViewModel bersama untuk konsistensi state.
 */
@Composable
fun GameScreen(
    level: Int,
    gameVm: GameViewModel,
    sensorController: SensorController,
    onLevelComplete: (Int) -> Unit,
    onGameOver: (Int) -> Unit,
    onExit: () -> Unit
) {
    val uiState by gameVm.uiState.collectAsStateWithLifecycle()

    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Pastikan layar tidak mati selama bermain
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Mulai level saat pertama kali masuk atau level berganti
    LaunchedEffect(level) {
        if (uiState.level != level || uiState.status == GameStatus.Won || uiState.status == GameStatus.GameOver) {
            gameVm.startLevel(level)
        }
    }

    // ─── Koleksi sensor: tilt, compass, proximity ───────────────────────────
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                sensorController.tiltFlow.collect { tilt ->
                    gameVm.onTilt(tilt)
                }
            }
            launch {
                sensorController.compassFlow.collect { compass ->
                    gameVm.onCompassData(compass)
                }
            }
            launch {
                sensorController.magnetometerState.collect { state ->
                    gameVm.onMagnetometerState(state)
                }
            }
            launch {
                sensorController.proximityFlow.collect { near ->
                    gameVm.onProximity(near)
                }
            }
        }
    }

    // Pause otomatis saat aplikasi berpindah ke background
    val lifecycle = lifecycleOwner.lifecycle
    DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                gameVm.onAppBackground()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // ─── Game Loop Presisi ──────────────────────────────────────────────────
    LaunchedEffect(uiState.status) {
        if (uiState.status == GameStatus.Playing) {
            var last = 0L
            while (isActive) {
                withFrameNanos { now ->
                    if (last != 0L) {
                        val dtMs = ((now - last) / 1_000_000L).coerceIn(0L, 32L)
                        gameVm.onFrame(dtMs)
                    }
                    last = now
                }
            }
        }
    }

    // Navigasi saat status permainan berakhir via one-time GameEvent
    LaunchedEffect(Unit) {
        gameVm.events.collect { event ->
            when (event) {
                is GameEvent.LevelWon -> onLevelComplete(event.level)
                is GameEvent.GameOver -> onGameOver(event.level)
            }
        }
    }

    val levelData = remember(level) { Levels.getLevel(level) } ?: return

    var showExitConfirm by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = uiState.status == GameStatus.Playing) {
        gameVm.pauseManual()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ─── Canvas Game ──────────────────────────────────────────────────
        GameCanvas(
            level = levelData,
            ballX = uiState.ballX,
            ballY = uiState.ballY,
            isGateOpen = uiState.gateOpen,
            modifier = Modifier.fillMaxSize()
        )

        // ─── HUD Melayang Atas ────────────────────────────────────────────
        HudBar(
            level = uiState.level,
            elapsedMs = uiState.elapsedMs,
            remainingMs = uiState.remainingMs,
            hasGate = uiState.hasGate,
            gateOpen = uiState.gateOpen,
            gateProgress = uiState.gateProgress,
            relativeHeading = uiState.relativeHeading,
            gateInstruction = uiState.gateInstruction,
            showManualGateButton = uiState.showManualGateButton,
            onOpenGateManual = { gameVm.openGateManually() },
            onPause = { gameVm.pauseManual() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // ─── Countdown Overlay 3-2-1 ──────────────────────────────────────
        if (uiState.status == GameStatus.Countdown) {
            CountdownOverlay(value = uiState.countdownValue)
        }

        // ─── Pesan Notifikasi Toast Melayang ("+3 detik" / "Gerbang terbuka!") ──
        AnimatedVisibility(
            visible = uiState.message.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (uiState.message.contains("terbuka")) Success else Warning,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Text(
                    text = uiState.message,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnPrimary
                )
            }
        }

        // ─── Pause Overlay ─────────────────────────────────────────────────
        if (uiState.status == GameStatus.Paused) {
            PauseOverlay(
                pauseReason = uiState.pauseReason,
                isProximityNear = uiState.isProximityNear,
                onResume = { gameVm.resume() },
                onRestart = { gameVm.restart() },
                onExit = onExit
            )
        }

        // ─── Dialog Konfirmasi Keluar ─────────────────────────────────────
        if (showExitConfirm) {
            ConfirmDialog(
                title = "Keluar Level?",
                message = "Progres level saat ini tidak akan disimpan.",
                confirmLabel = "Keluar",
                onConfirm = { showExitConfirm = false; onExit() },
                onDismiss = { showExitConfirm = false }
            )
        }
    }
}
