package com.example.tiltball.game

import kotlin.math.*

/**
 * Konfigurasi fisika game — disetel untuk gameplay yang responsif, stabil,
 * dan mudah dikendalikan oleh pemain kasual.
 */
object GameConfig {
    const val COLS = 9
    const val ROWS = 14

    const val BALL_RADIUS = 0.35f        // Radius bola (satuan sel)
    const val HOLE_CATCH_RADIUS = 0.36f  // Jarak tangkap lubang yang adil (tidak mudah tersangkut jika hanya menyenggol ujung)
    const val FINISH_RADIUS = 0.50f      // Deteksi finish yang memuaskan

    const val FORCE_SCALE = 6.2f         // Respons kemiringan yang pas tanpa perlu memiringkan HP terlalu curam
    const val DAMPING_PER_FRAME = 0.980f // Gesekan lantai: bola berhenti mantap saat HP didatarkan
    const val MAX_SPEED = 8.5f           // Batas kecepatan maksimal agar bola tetap terkendali
    const val RESTITUTION = 0.28f        // Pantulan elastis dinding yang lembut
    const val MAX_DT_MS = 32f            // Batas delta time (ms)
    const val MAX_STEP_DISTANCE = 0.18f  // Sub-step threshold untuk pencegahan tembus dinding 100%

    const val PENALTY_MS = 3_000L        // Penalti per lubang (ms)
}

data class BallState(
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f
)

/**
 * GameEngine: simulasi fisika 2D bola dan tabrakan.
 * Termasuk penanganan Gerbang Kompas (berperilaku seperti dinding jika tertutup,
 * dan menjadi lantai bebas jika isGateOpen = true).
 */
class GameEngine(val level: LevelData) {

    private var accelX = 0f
    private var accelY = 0f

    var ballState: BallState = BallState(
        x = level.startCol + 0.5f,
        y = level.startRow + 0.5f
    )
        private set

    /** Status apakah gerbang kompas pada level ini sudah terbuka. */
    var isGateOpen: Boolean = false

    fun applyTilt(ax: Float, ay: Float) {
        accelX = ax * GameConfig.FORCE_SCALE
        accelY = ay * GameConfig.FORCE_SCALE
    }

    fun step(dtMs: Float): FrameResult {
        val dt = minOf(dtMs, GameConfig.MAX_DT_MS) / 1000f
        val damping = GameConfig.DAMPING_PER_FRAME.pow(dt * 60f)

        var vx = ballState.vx + accelX * dt
        var vy = ballState.vy + accelY * dt

        vx *= damping
        vy *= damping

        val speed = sqrt(vx * vx + vy * vy)
        if (speed > GameConfig.MAX_SPEED) {
            val scale = GameConfig.MAX_SPEED / speed
            vx *= scale
            vy *= scale
        }

        val moveX = vx * dt
        val moveY = vy * dt
        val maxMove = maxOf(abs(moveX), abs(moveY))
        val steps = maxOf(1, ceil(maxMove / GameConfig.MAX_STEP_DISTANCE).toInt())
        val subDt = dt / steps
        val subVx = vx
        val subVy = vy

        var cx = ballState.x
        var cy = ballState.y
        var curVx = vx
        var curVy = vy

        repeat(steps) {
            cx += subVx * subDt
            cy += subVy * subDt

            val (nx, ny, nvx, nvy) = resolveWallCollisions(cx, cy, curVx, curVy)
            cx = nx; cy = ny; curVx = nvx; curVy = nvy
        }

        ballState = BallState(cx, cy, curVx, curVy)

        val holeHit = findHoleAt(cx, cy)
        if (holeHit != null) {
            resetToStart()
            return FrameResult.HoleHit
        }

        val fdx = cx - (level.finishCol + 0.5f)
        val fdy = cy - (level.finishRow + 0.5f)
        if (sqrt(fdx * fdx + fdy * fdy) < GameConfig.FINISH_RADIUS) {
            return FrameResult.Won
        }

        return FrameResult.Normal
    }

    fun resetToStart() {
        ballState = BallState(
            x = level.startCol + 0.5f,
            y = level.startRow + 0.5f
        )
        accelX = 0f
        accelY = 0f
    }

    private fun resolveWallCollisions(
        cx: Float, cy: Float, vx: Float, vy: Float
    ): FloatArray {
        var nx = cx; var ny = cy; var nvx = vx; var nvy = vy

        val colMin = maxOf(0, floor(cx - GameConfig.BALL_RADIUS - 1).toInt())
        val colMax = minOf(GameConfig.COLS - 1, ceil(cx + GameConfig.BALL_RADIUS + 1).toInt())
        val rowMin = maxOf(0, floor(cy - GameConfig.BALL_RADIUS - 1).toInt())
        val rowMax = minOf(GameConfig.ROWS - 1, ceil(cy + GameConfig.BALL_RADIUS + 1).toInt())

        for (row in rowMin..rowMax) {
            for (col in colMin..colMax) {
                val cell = level.grid[row][col]

                // Sel adalah rintangan padat jika WALL atau GATE yang masih tertutup
                val isObstacle = cell == CellType.WALL || (cell == CellType.GATE && !isGateOpen)
                if (!isObstacle) continue

                val cellLeft = col.toFloat()
                val cellRight = col + 1f
                val cellTop = row.toFloat()
                val cellBottom = row + 1f

                val closestX = nx.coerceIn(cellLeft, cellRight)
                val closestY = ny.coerceIn(cellTop, cellBottom)

                val dx = nx - closestX
                val dy = ny - closestY
                val dist = sqrt(dx * dx + dy * dy)

                if (dist < GameConfig.BALL_RADIUS && dist > 0f) {
                    val normalX = dx / dist
                    val normalY = dy / dist
                    val penetration = GameConfig.BALL_RADIUS - dist

                    nx += normalX * penetration
                    ny += normalY * penetration

                    val dotV = nvx * normalX + nvy * normalY
                    if (dotV < 0f) {
                        nvx -= (1f + GameConfig.RESTITUTION) * dotV * normalX
                        nvy -= (1f + GameConfig.RESTITUTION) * dotV * normalY
                    }
                } else if (dist == 0f) {
                    ny -= GameConfig.BALL_RADIUS
                    nvy = 0f
                }
            }
        }

        nx = nx.coerceIn(GameConfig.BALL_RADIUS, GameConfig.COLS - GameConfig.BALL_RADIUS)
        ny = ny.coerceIn(GameConfig.BALL_RADIUS, GameConfig.ROWS - GameConfig.BALL_RADIUS)
        if (nx <= GameConfig.BALL_RADIUS || nx >= GameConfig.COLS - GameConfig.BALL_RADIUS) nvx = -nvx * GameConfig.RESTITUTION
        if (ny <= GameConfig.BALL_RADIUS || ny >= GameConfig.ROWS - GameConfig.BALL_RADIUS) nvy = -nvy * GameConfig.RESTITUTION

        return floatArrayOf(nx, ny, nvx, nvy)
    }

    private fun findHoleAt(cx: Float, cy: Float): Pair<Int, Int>? {
        val colMin = maxOf(0, floor(cx - 1).toInt())
        val colMax = minOf(GameConfig.COLS - 1, ceil(cx + 1).toInt())
        val rowMin = maxOf(0, floor(cy - 1).toInt())
        val rowMax = minOf(GameConfig.ROWS - 1, ceil(cy + 1).toInt())

        for (row in rowMin..rowMax) {
            for (col in colMin..colMax) {
                if (level.grid[row][col] != CellType.HOLE) continue
                val dx = cx - (col + 0.5f)
                val dy = cy - (row + 0.5f)
                if (sqrt(dx * dx + dy * dy) < GameConfig.HOLE_CATCH_RADIUS) {
                    return row to col
                }
            }
        }
        return null
    }

    companion object {
        fun calculateScore(totalMs: Long): Int {
            val seconds = totalMs / 1000.0
            return maxOf(0, (1000 - seconds * 10).toInt())
        }
    }
}

sealed class FrameResult {
    object Normal : FrameResult()
    object HoleHit : FrameResult()
    object Won : FrameResult()
}

private fun Float.pow(exp: Float): Float = Math.pow(this.toDouble(), exp.toDouble()).toFloat()
