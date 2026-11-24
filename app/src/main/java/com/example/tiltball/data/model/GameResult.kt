package com.example.tiltball.data.model

/**
 * Model hasil permainan satu sesi level.
 * Semua waktu dalam milidetik.
 */
data class GameResult(
    val id: Long = 0,
    val level: Int,
    val score: Int,
    val durationMs: Long,      // waktu bermain bersih
    val penaltyMs: Long,       // total penalti (jumlah lubang × 3000 ms)
    val completedAt: Long      // epoch millis saat level selesai
)
