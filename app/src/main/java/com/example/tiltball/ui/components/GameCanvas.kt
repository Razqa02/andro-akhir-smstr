package com.example.tiltball.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.tiltball.game.CellType
import com.example.tiltball.game.GameConfig
import com.example.tiltball.game.LevelData
import com.example.tiltball.ui.theme.*
import kotlin.math.min

/**
 * Canvas arena labirin Tilt Ball yang modern, rapi, dan responsif.
 * Menyajikan visual beveled walls, glowing portal, energy gate, dan 3D specular ball.
 */
@Composable
fun GameCanvas(
    level: LevelData,
    ballX: Float,
    ballY: Float,
    isGateOpen: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        val cellW = canvasW / GameConfig.COLS
        val cellH = canvasH / GameConfig.ROWS
        val cellSize = min(cellW, cellH)

        val totalBoardW = cellSize * GameConfig.COLS
        val totalBoardH = cellSize * GameConfig.ROWS
        val offsetX = (canvasW - totalBoardW) / 2f
        val offsetY = (canvasH - totalBoardH) / 2f

        // ─── 1. Background Luar & Lantai Arena ────────────────────────────────
        drawRect(color = Background, size = Size(canvasW, canvasH))

        // Lantai papan permainan dengan bayangan lembut
        drawRect(
            color = FloorColor,
            topLeft = Offset(offsetX, offsetY),
            size = Size(totalBoardW, totalBoardH)
        )

        // Grid garis halus pada lantai
        for (r in 0..GameConfig.ROWS) {
            val y = offsetY + r * cellSize
            drawLine(
                color = FloorGridColor,
                start = Offset(offsetX, y),
                end = Offset(offsetX + totalBoardW, y),
                strokeWidth = 1f
            )
        }
        for (c in 0..GameConfig.COLS) {
            val x = offsetX + c * cellSize
            drawLine(
                color = FloorGridColor,
                start = Offset(x, offsetY),
                end = Offset(x, offsetY + totalBoardH),
                strokeWidth = 1f
            )
        }

        // ─── 2. Elemen Sel Grid ───────────────────────────────────────────────
        for (row in 0 until GameConfig.ROWS) {
            for (col in 0 until GameConfig.COLS) {
                val left = offsetX + col * cellSize
                val top = offsetY + row * cellSize
                when (level.grid[row][col]) {
                    CellType.WALL -> drawModernWall(left, top, cellSize)
                    CellType.HOLE -> drawModernHole(left, top, cellSize)
                    CellType.START -> drawModernStart(left, top, cellSize)
                    CellType.FINISH -> drawModernFinish(left, top, cellSize)
                    CellType.GATE -> drawModernGate(left, top, cellSize, isGateOpen)
                    CellType.FLOOR -> { /* lantai dasar */ }
                }
            }
        }

        // Border luar papan arena
        drawRect(
            color = WallBorderColor,
            topLeft = Offset(offsetX, offsetY),
            size = Size(totalBoardW, totalBoardH),
            style = Stroke(width = 3f)
        )

        // ─── 3. Render Bola 3D ────────────────────────────────────────────────
        val ballPxX = offsetX + ballX * cellSize
        val ballPxY = offsetY + ballY * cellSize
        val ballRadiusPx = GameConfig.BALL_RADIUS * cellSize

        // Bayangan lembut di bawah bola
        drawCircle(
            color = BallShadowColor,
            radius = ballRadiusPx * 1.15f,
            center = Offset(ballPxX + ballRadiusPx * 0.12f, ballPxY + ballRadiusPx * 0.18f)
        )

        // Badan bola dengan gradasi bola 3D
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BallHighlight,
                    BallColor,
                    Color(0xFF0369A1)
                ),
                center = Offset(ballPxX - ballRadiusPx * 0.32f, ballPxY - ballRadiusPx * 0.32f),
                radius = ballRadiusPx * 1.25f
            ),
            radius = ballRadiusPx,
            center = Offset(ballPxX, ballPxY)
        )

        // Kilap specular kecil di permukaan atas bola
        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = ballRadiusPx * 0.22f,
            center = Offset(ballPxX - ballRadiusPx * 0.35f, ballPxY - ballRadiusPx * 0.35f)
        )
    }
}

/** Gambar Dinding dengan efek bevel modern */
private fun DrawScope.drawModernWall(left: Float, top: Float, cellSize: Float) {
    val cr = CornerRadius(cellSize * 0.12f, cellSize * 0.12f)
    val rect = RoundRect(
        left = left + 0.5f,
        top = top + 0.5f,
        right = left + cellSize - 0.5f,
        bottom = top + cellSize - 0.5f,
        cornerRadius = cr
    )
    val path = Path().apply { addRoundRect(rect) }

    // Badan dinding solid
    drawPath(path = path, color = WallColor)

    // Inner bevel highlight
    drawRoundRect(
        color = WallBorderColor,
        topLeft = Offset(left + 1.5f, top + 1.5f),
        size = Size(cellSize - 3f, cellSize - 3f),
        cornerRadius = cr,
        style = Stroke(width = cellSize * 0.06f)
    )
}

/** Gambar Lubang rintangan dengan gradient kedalaman */
private fun DrawScope.drawModernHole(left: Float, top: Float, cellSize: Float) {
    val cx = left + cellSize / 2f
    val cy = top + cellSize / 2f
    val r = cellSize * GameConfig.HOLE_CATCH_RADIUS

    // Glow tepi lubang
    drawCircle(
        color = HoleRimColor.copy(alpha = 0.25f),
        radius = r * 1.15f,
        center = Offset(cx, cy)
    )

    // Pusat lubang hitam pekat dengan gradien
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(HoleColor, Color(0xFF1E0208)),
            center = Offset(cx, cy),
            radius = r
        ),
        radius = r,
        center = Offset(cx, cy)
    )

    // Tepi merah neon tegas
    drawCircle(
        color = HoleRimColor,
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = cellSize * 0.05f)
    )
}

/** Gambar Launchpad Start */
private fun DrawScope.drawModernStart(left: Float, top: Float, cellSize: Float) {
    val cx = left + cellSize / 2f
    val cy = top + cellSize / 2f
    val padSize = cellSize * 0.82f

    // Area hijau transparan
    drawRoundRect(
        color = StartColor.copy(alpha = 0.18f),
        topLeft = Offset(cx - padSize / 2f, cy - padSize / 2f),
        size = Size(padSize, padSize),
        cornerRadius = CornerRadius(cellSize * 0.16f, cellSize * 0.16f)
    )

    // Border neon hijau
    drawRoundRect(
        color = StartColor,
        topLeft = Offset(cx - padSize / 2f, cy - padSize / 2f),
        size = Size(padSize, padSize),
        cornerRadius = CornerRadius(cellSize * 0.16f, cellSize * 0.16f),
        style = Stroke(width = cellSize * 0.06f)
    )

    // Titik pusat
    drawCircle(
        color = StartColor,
        radius = cellSize * 0.12f,
        center = Offset(cx, cy)
    )
}

/** Gambar Titik Finish bersinar */
private fun DrawScope.drawModernFinish(left: Float, top: Float, cellSize: Float) {
    val cx = left + cellSize / 2f
    val cy = top + cellSize / 2f

    // Glow luar emas
    drawCircle(
        color = FinishGlowColor,
        radius = cellSize * 0.44f,
        center = Offset(cx, cy)
    )

    // Cincin emas berkilau
    drawCircle(
        color = FinishColor,
        radius = cellSize * 0.36f,
        center = Offset(cx, cy),
        style = Stroke(width = cellSize * 0.08f)
    )

    // Bintang / pusat target emas
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFEF08A), FinishColor),
            center = Offset(cx, cy),
            radius = cellSize * 0.22f
        ),
        radius = cellSize * 0.20f,
        center = Offset(cx, cy)
    )
}

/** Gambar Gerbang Kompas: Cyan neon bergembok jika terkunci, portal hijau terbuka jika unlock */
private fun DrawScope.drawModernGate(left: Float, top: Float, cellSize: Float, isOpen: Boolean) {
    val cx = left + cellSize / 2f
    val cy = top + cellSize / 2f

    if (!isOpen) {
        // Gerbang Tertutup: Penghalang energi cyan neon
        drawRoundRect(
            color = GateGlowColor,
            topLeft = Offset(left + 1f, top + 1f),
            size = Size(cellSize - 2f, cellSize - 2f),
            cornerRadius = CornerRadius(cellSize * 0.12f, cellSize * 0.12f)
        )

        drawRoundRect(
            color = GateColor,
            topLeft = Offset(left + 2f, top + 2f),
            size = Size(cellSize - 4f, cellSize - 4f),
            cornerRadius = CornerRadius(cellSize * 0.12f, cellSize * 0.12f),
            style = Stroke(width = cellSize * 0.08f)
        )

        // Kisi-kisi energi horizontal
        drawLine(
            color = GateColor.copy(alpha = 0.8f),
            start = Offset(left + cellSize * 0.2f, cy),
            end = Offset(left + cellSize * 0.8f, cy),
            strokeWidth = cellSize * 0.05f
        )

        // Ikon gembok mini
        val lockW = cellSize * 0.28f
        val lockH = cellSize * 0.22f
        drawRoundRect(
            color = GateColor,
            topLeft = Offset(cx - lockW / 2f, cy - lockH / 4f),
            size = Size(lockW, lockH),
            cornerRadius = CornerRadius(cellSize * 0.04f, cellSize * 0.04f)
        )
        drawCircle(
            color = GateColor,
            radius = lockW * 0.32f,
            center = Offset(cx, cy - lockH * 0.35f),
            style = Stroke(width = cellSize * 0.04f)
        )
    } else {
        // Gerbang Terbuka: Jalur bebas dengan garis hijau transparan
        drawRoundRect(
            color = Success.copy(alpha = 0.15f),
            topLeft = Offset(left + 2f, top + 2f),
            size = Size(cellSize - 4f, cellSize - 4f),
            cornerRadius = CornerRadius(cellSize * 0.12f, cellSize * 0.12f)
        )
        drawCircle(
            color = Success,
            radius = cellSize * 0.12f,
            center = Offset(cx, cy)
        )
    }
}
