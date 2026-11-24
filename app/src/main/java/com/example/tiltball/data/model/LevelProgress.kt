package com.example.tiltball.data.model

/**
 * Model progres per level: status terkunci/terbuka dan rekor terbaik.
 */
data class LevelProgress(
    val level: Int,
    val unlocked: Boolean,
    val bestScore: Int,       // 0 jika belum pernah selesai
    val bestTimeMs: Long      // 0 jika belum pernah selesai
)
