package com.example.tiltball

import com.example.tiltball.game.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test untuk GameEngine, Fisika Tabrakan, Gerbang Kompas, dan LevelValidator.
 */
class GameEngineTest {

    @Test
    fun levelValidator_allLevelsAreReachable() {
        // BFS memastikan setiap level 1 sampai 5 bisa diselesaikan (gerbang dianggap dapat dilewati)
        for (level in Levels.all) {
            val reachable = LevelValidator.isReachable(level)
            assertTrue("Level ${level.number} (${level.name}) harus dapat diselesaikan!", reachable)
        }
    }

    @Test
    fun gameEngine_ballDoesNotPenetrateWall() {
        val level1 = Levels.getLevel(1)!!
        val engine = GameEngine(level1)

        // Berikan gaya kemiringan ke kiri ekstrem selama 60 frame (1 detik)
        engine.applyTilt(-10f, 0f)
        repeat(60) {
            engine.step(16f)
        }

        // Bola harus tertahan di dalam batas arena (tidak boleh tembus dinding batas)
        val ball = engine.ballState
        assertTrue("Bola tidak boleh tembus batas kiri arena", ball.x >= GameConfig.BALL_RADIUS)
        assertTrue("Bola tidak boleh tembus batas kanan arena", ball.x <= (GameConfig.COLS - GameConfig.BALL_RADIUS))
    }

    @Test
    fun gameEngine_gateBlocksWhenClosed_passesWhenOpen() {
        // Level dengan gerbang tepat di sebelah kanan start
        val testGrid = listOf(
            "#########",
            "#SG.....#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#......F#",
            "#########"
        )
        val testLevel = parseLevel(88, "Test Gate", testGrid, 60_000L, GateData(90f))
        val engine = GameEngine(testLevel)

        // Kondisi 1: Gerbang tertutup -> bola tertahan
        engine.applyTilt(5f, 0f)
        repeat(30) { engine.step(16f) }
        assertTrue("Bola harus tertahan di depan gerbang tertutup", engine.ballState.x < 1.7f)

        // Kondisi 2: Buka gerbang -> bola dapat melintas
        engine.isGateOpen = true
        repeat(30) { engine.step(16f) }
        assertTrue("Bola harus berhasil melewati gerbang terbuka", engine.ballState.x > 1.7f)
    }

    @Test
    fun gameEngine_holeHit_resetsToStart() {
        val testGrid = listOf(
            "#########",
            "#SO.....#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#......F#",
            "#########"
        )
        val testLevel = parseLevel(99, "Test Hole", testGrid, 60_000L, null)
        val engine = GameEngine(testLevel)

        engine.applyTilt(5f, 0f)
        var hit = false
        repeat(50) {
            val result = engine.step(16f)
            if (result is FrameResult.HoleHit) {
                hit = true
            }
        }

        assertTrue("Bola harus terdeteksi masuk lubang", hit)
        assertEquals(testLevel.startCol + 0.5f, engine.ballState.x, 0.01f)
        assertEquals(testLevel.startRow + 0.5f, engine.ballState.y, 0.01f)
    }

    @Test
    fun gameEngine_scoreCalculation() {
        // 10 detik total: 1000 - 10*10 = 900 murni (tanpa pengali)
        val score = GameEngine.calculateScore(10_000L)
        assertEquals(900, score)

        // 50 detik total: 1000 - 50*10 = 500
        val score50s = GameEngine.calculateScore(50_000L)
        assertEquals(500, score50s)
    }
}
