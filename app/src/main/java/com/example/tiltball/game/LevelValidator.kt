package com.example.tiltball.game

import android.util.Log

/**
 * Validator BFS: membuktikan bahwa titik Finish bisa dicapai dari Start
 * melewati sel NON-dinding dan NON-lubang.
 *
 * Gerbang Kompas (CellType.GATE) dianggap dapat dilewati karena dapat dibuka
 * oleh pemain dengan memutar badan. Sel WALL dan HOLE tetap tidak bisa dilewati.
 */
object LevelValidator {

    private const val TAG = "LevelValidator"

    fun isReachable(level: LevelData): Boolean {
        val rows = level.grid.size
        val cols = level.grid[0].size
        val visited = Array(rows) { BooleanArray(cols) }
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(level.startRow to level.startCol)
        visited[level.startRow][level.startCol] = true

        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()

            if (r == level.finishRow && c == level.finishCol) return true

            for ((dr, dc) in directions) {
                val nr = r + dr
                val nc = c + dc
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue
                if (visited[nr][nc]) continue

                val cell = level.grid[nr][nc]
                // Hanya lewat FLOOR, START, FINISH, dan GATE — BUKAN WALL dan BUKAN HOLE
                if (cell == CellType.WALL || cell == CellType.HOLE) continue

                visited[nr][nc] = true
                queue.add(nr to nc)
            }
        }
        return false
    }

    fun validateAll() {
        Levels.all.forEach { level ->
            val ok = isReachable(level)
            if (ok) {
                Log.d(TAG, "Level ${level.number} '${level.name}': VALID ✓")
            } else {
                Log.e(TAG, "Level ${level.number} '${level.name}': TIDAK VALID — finish tidak bisa dicapai!")
            }
        }
    }
}
