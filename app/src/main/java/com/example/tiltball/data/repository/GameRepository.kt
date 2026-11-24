package com.example.tiltball.data.repository

import com.example.tiltball.data.local.GameDbHelper
import com.example.tiltball.data.model.GameResult
import com.example.tiltball.data.model.LevelProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository membungkus GameDbHelper.
 * Semua fungsi berjalan di Dispatchers.IO, aman dipanggil dari ViewModel.
 */
class GameRepository(private val db: GameDbHelper) {

    suspend fun insertResult(result: GameResult): Long =
        withContext(Dispatchers.IO) { db.insertResult(result) }

    suspend fun getAllResults(): List<GameResult> =
        withContext(Dispatchers.IO) { db.getAllResults() }

    suspend fun getHighestScore(): Int =
        withContext(Dispatchers.IO) { db.getHighestScore() }

    suspend fun getHighestScoreByLevel(level: Int): Int =
        withContext(Dispatchers.IO) { db.getHighestScoreByLevel(level) }

    suspend fun deleteResult(id: Long) =
        withContext(Dispatchers.IO) { db.deleteResult(id) }

    suspend fun deleteAllResults() =
        withContext(Dispatchers.IO) { db.deleteAllResults() }

    suspend fun getAllProgress(): List<LevelProgress> =
        withContext(Dispatchers.IO) { db.getAllProgress() }

    suspend fun unlockLevel(level: Int) =
        withContext(Dispatchers.IO) { db.unlockLevel(level) }

    suspend fun updateBest(level: Int, score: Int, timeMs: Long) =
        withContext(Dispatchers.IO) { db.updateBest(level, score, timeMs) }

    /**
     * Simpan hasil + update best + buka level berikutnya dalam satu transaksi.
     * Kembalikan true jika skor ini lebih tinggi dari rekor sebelumnya.
     */
    suspend fun saveResultAndProgress(result: GameResult): Boolean =
        withContext(Dispatchers.IO) { db.saveResultAndProgress(result) }

    suspend fun resetProgress() =
        withContext(Dispatchers.IO) { db.resetProgress() }
}
