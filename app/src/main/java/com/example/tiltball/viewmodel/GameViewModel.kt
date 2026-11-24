package com.example.tiltball.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiltball.data.model.GameResult
import com.example.tiltball.data.repository.GameRepository
import com.example.tiltball.game.*
import com.example.tiltball.sensor.CompassData
import com.example.tiltball.sensor.SensorState
import com.example.tiltball.sensor.TiltData
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed interface GameEvent {
    data class LevelWon(val level: Int) : GameEvent
    data class GameOver(val level: Int) : GameEvent
}

enum class GameStatus { Idle, Countdown, Playing, Paused, Won, GameOver }

enum class PauseReason { NONE, MANUAL, PROXIMITY }

/**
 * State UI lengkap untuk GameScreen dan layar hasil (LevelCompleteScreen / GameOverScreen).
 */
data class GameUiState(
    val level: Int = 1,
    val ballX: Float = 0f,
    val ballY: Float = 0f,
    val velX: Float = 0f,
    val velY: Float = 0f,
    val status: GameStatus = GameStatus.Idle,
    val countdownValue: Int = 3,
    val elapsedMs: Long = 0L,
    val penaltyMs: Long = 0L,
    val timeLimitMs: Long = 70_000L,
    val pauseReason: PauseReason = PauseReason.NONE,
    val isProximityNear: Boolean = false,
    val lastScore: Int = 0,
    val isNewRecord: Boolean = false,
    val message: String = "",

    // ─── Gerbang Kompas State ───────────────────────────────────────────────
    val hasGate: Boolean = false,
    val gateTargetDegrees: Float = 0f,
    val gateOpen: Boolean = false,
    val gateProgress: Float = 0f,
    val relativeHeading: Float = 0f,
    val compassAzimuth: Float = 0f,
    val magnetState: SensorState = SensorState.TERSEDIA,
    val showManualGateButton: Boolean = false,
    val gateInstruction: String = ""
) {
    val totalMs: Long get() = elapsedMs + penaltyMs
    val remainingMs: Long get() = maxOf(0L, timeLimitMs - totalMs)
}

/**
 * GameViewModel: state machine permainan, fisika bola, dan Gerbang Kompas.
 * Semua pembaruan state menggunakan update {} atomic untuk mencegah race condition.
 */
class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    private val TAG = "TiltBall"

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events: Flow<GameEvent> = _events.receiveAsFlow()

    private var engine: GameEngine? = null
    private var countdownJob: Job? = null
    private var messageJob: Job? = null

    // Flag agar finishLevel hanya menyimpan hasil sekali per permainan
    private var resultSaved = false

    // Cooldown lubang untuk mencegah penalti beruntun
    private var lastHoleHitTime = 0L

    // Nilai tilt terbaru dari sensor (disimpan terpisah dan dibaca saat onFrame)
    @Volatile
    private var currentTiltX = 0f
    @Volatile
    private var currentTiltY = 0f

    private val prefs = application.getSharedPreferences("tiltball_prefs", Context.MODE_PRIVATE)

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    private var headingRef: Float? = null
    private var gateHoldStartTime: Long = 0L
    private var unreliableStartTime: Long = 0L
    private var currentAzimuth: Float = 0f

    fun startLevel(levelNumber: Int) {
        val levelData = Levels.getLevel(levelNumber) ?: return
        resultSaved = false
        lastHoleHitTime = 0L
        headingRef = null
        gateHoldStartTime = 0L
        unreliableStartTime = 0L
        currentTiltX = 0f
        currentTiltY = 0f

        engine = GameEngine(levelData)

        val hasGate = levelData.gateData != null
        val targetDeg = levelData.gateData?.targetDegrees ?: 0f
        val instruction = when {
            !hasGate -> ""
            targetDeg > 0 && targetDeg < 180 -> "Putar badan ke kanan ${targetDeg.toInt()}°"
            targetDeg < 0 -> "Putar badan ke kiri ${abs(targetDeg).toInt()}°"
            else -> "Putar balik badan (180°)"
        }

        Log.d(TAG, "startLevel $levelNumber: '${levelData.name}', hasGate=$hasGate, limit=${levelData.timeLimitMs}ms")

        _uiState.update {
            GameUiState(
                level = levelNumber,
                ballX = levelData.startCol + 0.5f,
                ballY = levelData.startRow + 0.5f,
                status = GameStatus.Countdown,
                countdownValue = 3,
                timeLimitMs = levelData.timeLimitMs,
                hasGate = hasGate,
                gateTargetDegrees = targetDeg,
                gateOpen = false,
                gateInstruction = instruction
            )
        }

        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(countdownValue = i) }
                delay(1_000)

                val autoPauseEnabled = prefs.getBoolean("auto_pause_proximity", false)
                if (autoPauseEnabled && _uiState.value.isProximityNear) {
                    Log.d(TAG, "Countdown dipause karena sensor proximity dekat")
                    _uiState.update {
                        it.copy(
                            status = GameStatus.Paused,
                            pauseReason = PauseReason.PROXIMITY
                        )
                    }
                    return@launch
                }
            }

            headingRef = currentAzimuth
            Log.d(TAG, "Countdown selesai -> status Playing. headingRef=${headingRef}°")

            _uiState.update {
                it.copy(
                    status = GameStatus.Playing,
                    countdownValue = 0
                )
            }
        }
    }

    /**
     * Langkah simulasi game (dipanggil per frame hanya saat Playing).
     * @param dtMs delta time dalam milidetik (0..32 ms)
     */
    fun onFrame(dtMs: Long) {
        val state = _uiState.value
        if (state.status != GameStatus.Playing) return

        val newElapsed = state.elapsedMs + dtMs
        val totalMs = newElapsed + state.penaltyMs

        // Cek waktu habis -> GameOver
        if (totalMs >= state.timeLimitMs) {
            Log.d(TAG, "Waktu habis: totalMs=$totalMs >= limit=${state.timeLimitMs} -> GameOver")
            _uiState.update {
                it.copy(
                    elapsedMs = newElapsed,
                    status = GameStatus.GameOver
                )
            }
            _events.trySend(GameEvent.GameOver(state.level))
            return
        }

        val eng = engine ?: return

        // Terapkan tilt yang tersimpan secara terpisah
        val offsetX = prefs.getFloat("offset_x", 0f)
        val offsetY = prefs.getFloat("offset_y", 0f)
        val sensitivity = prefs.getFloat("sensitivity", 1.0f)
        val ax = (currentTiltX - offsetX) * sensitivity
        val ay = (currentTiltY - offsetY) * sensitivity
        eng.applyTilt(ax, ay)

        val result = eng.step(dtMs.toFloat())
        val ball = eng.ballState

        when (result) {
            is FrameResult.Normal -> {
                _uiState.update {
                    it.copy(
                        ballX = ball.x,
                        ballY = ball.y,
                        velX = ball.vx,
                        velY = ball.vy,
                        elapsedMs = newElapsed
                    )
                }
            }
            is FrameResult.HoleHit -> {
                val now = SystemClock.elapsedRealtime()
                // Cooldown 500ms agar bola tidak terkena penalti ganda beruntun
                if (now - lastHoleHitTime > 500L) {
                    lastHoleHitTime = now
                    vibrateShort()
                    val newPenalty = state.penaltyMs + GameConfig.PENALTY_MS
                    Log.d(TAG, "Bola masuk lubang! Penalti baru = $newPenalty ms")
                    _uiState.update {
                        it.copy(
                            ballX = ball.x,
                            ballY = ball.y,
                            velX = 0f,
                            velY = 0f,
                            elapsedMs = newElapsed,
                            penaltyMs = newPenalty,
                            message = "+3 detik"
                        )
                    }
                    showMessageBriefly()
                } else {
                    _uiState.update {
                        it.copy(
                            ballX = ball.x,
                            ballY = ball.y,
                            elapsedMs = newElapsed
                        )
                    }
                }
            }
            is FrameResult.Won -> {
                _uiState.update {
                    it.copy(
                        ballX = ball.x,
                        ballY = ball.y,
                        elapsedMs = newElapsed
                    )
                }
                finishLevel()
            }
        }
    }

    /** Menerima event tilt dari sensor dan menyimpan di variabel terpisah */
    fun onTilt(data: TiltData) {
        currentTiltX = data.x
        currentTiltY = data.y
    }

    fun onCompassData(data: CompassData) {
        currentAzimuth = data.azimuthDegrees
        val state = _uiState.value

        val ref = headingRef ?: currentAzimuth

        var diff = data.azimuthDegrees - ref
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f

        val relHeading = diff

        val now = SystemClock.elapsedRealtime()
        if (!data.isReliable) {
            if (unreliableStartTime == 0L) unreliableStartTime = now
        } else {
            unreliableStartTime = 0L
        }

        val showFallback = state.magnetState != SensorState.TERSEDIA ||
                (unreliableStartTime > 0L && (now - unreliableStartTime >= 5000L))

        var newGateOpen = state.gateOpen
        var newProgress = state.gateProgress

        if (state.hasGate && !state.gateOpen && state.status == GameStatus.Playing) {
            val gateData = engine?.level?.gateData
            if (gateData != null) {
                var angleToTarget = abs(relHeading - gateData.targetDegrees)
                if (angleToTarget > 180f) angleToTarget = 360f - angleToTarget

                if (angleToTarget <= gateData.toleranceDegrees) {
                    if (gateHoldStartTime == 0L) gateHoldStartTime = now
                    val heldMs = now - gateHoldStartTime
                    newProgress = (heldMs.toFloat() / gateData.holdMs).coerceIn(0f, 1f)

                    if (heldMs >= gateData.holdMs) {
                        newGateOpen = true
                        engine?.isGateOpen = true
                        vibrateShort()
                        Log.d(TAG, "Gerbang Kompas TERBUKA!")
                        _uiState.update {
                            it.copy(
                                gateOpen = true,
                                gateProgress = 1f,
                                message = "Gerbang terbuka!"
                            )
                        }
                        showMessageBriefly()
                    }
                } else {
                    gateHoldStartTime = 0L
                    newProgress = 0f
                }
            }
        }

        _uiState.update {
            it.copy(
                relativeHeading = relHeading,
                compassAzimuth = data.azimuthDegrees,
                gateOpen = newGateOpen,
                gateProgress = newProgress,
                showManualGateButton = showFallback
            )
        }
    }

    fun onMagnetometerState(sensorState: SensorState) {
        val showFallback = sensorState != SensorState.TERSEDIA
        _uiState.update {
            it.copy(
                magnetState = sensorState,
                showManualGateButton = showFallback
            )
        }
    }

    fun openGateManually() {
        if (_uiState.value.hasGate && !_uiState.value.gateOpen) {
            engine?.isGateOpen = true
            Log.d(TAG, "Gerbang dibuka manual oleh tombol fallback")
            _uiState.update {
                it.copy(
                    gateOpen = true,
                    gateProgress = 1f,
                    message = "Gerbang dibuka manual!"
                )
            }
            showMessageBriefly()
        }
    }

    fun onProximity(isNear: Boolean) {
        _uiState.update { it.copy(isProximityNear = isNear) }

        val autoPauseEnabled = prefs.getBoolean("auto_pause_proximity", false)
        if (!autoPauseEnabled) return

        val state = _uiState.value
        if (isNear && (state.status == GameStatus.Playing || state.status == GameStatus.Countdown)) {
            if (state.status == GameStatus.Countdown) countdownJob?.cancel()
            Log.d(TAG, "Auto-pause proximity terpicu")
            _uiState.update {
                it.copy(
                    status = GameStatus.Paused,
                    pauseReason = PauseReason.PROXIMITY
                )
            }
        }
    }

    fun pauseManual() {
        if (_uiState.value.status == GameStatus.Playing) {
            Log.d(TAG, "Pause manual dipicu")
            _uiState.update {
                it.copy(
                    status = GameStatus.Paused,
                    pauseReason = PauseReason.MANUAL
                )
            }
        }
    }

    fun resume() {
        val state = _uiState.value
        if (state.status != GameStatus.Paused) return

        val autoPauseEnabled = prefs.getBoolean("auto_pause_proximity", false)
        if (autoPauseEnabled && state.isProximityNear) return

        Log.d(TAG, "Resume game -> Playing")
        _uiState.update {
            it.copy(
                status = GameStatus.Playing,
                pauseReason = PauseReason.NONE
            )
        }
    }

    fun restart() {
        Log.d(TAG, "Restart level ${_uiState.value.level}")
        startLevel(_uiState.value.level)
    }

    /**
     * Menyelesaikan level: menghitung skor, mengupdate state ke Won,
     * dan menyimpan ke SQLite dalam satu transaksi atomik.
     */
    fun finishLevel() {
        if (resultSaved) return
        resultSaved = true

        val state = _uiState.value
        val totalMs = state.elapsedMs + state.penaltyMs
        val score = GameEngine.calculateScore(totalMs)

        Log.d(TAG, "finishLevel: Level=${state.level}, totalMs=$totalMs, score=$score")

        // 1. Update state UI ke Won segera
        _uiState.update {
            it.copy(
                status = GameStatus.Won,
                lastScore = score
            )
        }
        _events.trySend(GameEvent.LevelWon(state.level))

        // 2. Simpan ke database di background thread
        viewModelScope.launch {
            try {
                val result = GameResult(
                    level = state.level,
                    score = score,
                    durationMs = state.elapsedMs,
                    penaltyMs = state.penaltyMs,
                    completedAt = System.currentTimeMillis()
                )
                val isNewRecord = repository.saveResultAndProgress(result)
                _uiState.update {
                    it.copy(isNewRecord = isNewRecord)
                }
                Log.d(TAG, "finishLevel DB save selesai: isNewRecord=$isNewRecord")
            } catch (e: Exception) {
                Log.e(TAG, "finishLevel DB save error: ${e.message}", e)
            }
        }
    }

    fun onAppBackground() {
        val state = _uiState.value
        if (state.status == GameStatus.Playing || state.status == GameStatus.Countdown) {
            if (state.status == GameStatus.Countdown) countdownJob?.cancel()
            Log.d(TAG, "onAppBackground: Game dipause")
            _uiState.update {
                it.copy(
                    status = GameStatus.Paused,
                    pauseReason = PauseReason.MANUAL
                )
            }
        }
    }

    private fun vibrateShort() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        }
    }

    private fun showMessageBriefly() {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            delay(1_500)
            _uiState.update { it.copy(message = "") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        messageJob?.cancel()
    }
}
