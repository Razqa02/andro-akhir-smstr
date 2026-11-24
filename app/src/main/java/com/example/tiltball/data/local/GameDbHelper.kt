package com.example.tiltball.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.tiltball.data.model.GameResult
import com.example.tiltball.data.model.LevelProgress

/**
 * SQLiteOpenHelper untuk Tilt Ball (Database Versi 2).
 * Operasi SQL manual murni tanpa ORM/Room.
 */
class GameDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "TiltBall"
        const val DATABASE_NAME = "tiltball.db"
        const val DATABASE_VERSION = 2

        const val TABLE_RESULTS = "game_results"
        const val COL_ID = "id"
        const val COL_LEVEL = "level"
        const val COL_SCORE = "score"
        const val COL_DURATION_MS = "duration_ms"
        const val COL_PENALTY_MS = "penalty_ms"
        const val COL_COMPLETED_AT = "completed_at"

        const val TABLE_PROGRESS = "level_progress"
        const val COL_UNLOCKED = "unlocked"
        const val COL_BEST_SCORE = "best_score"
        const val COL_BEST_TIME_MS = "best_time_ms"

        const val TOTAL_LEVELS = 5
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "GameDbHelper onCreate: membuat tabel game_results & level_progress")
        db.execSQL(
            """
            CREATE TABLE $TABLE_RESULTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LEVEL INTEGER NOT NULL,
                $COL_SCORE INTEGER NOT NULL,
                $COL_DURATION_MS INTEGER NOT NULL,
                $COL_PENALTY_MS INTEGER NOT NULL,
                $COL_COMPLETED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_PROGRESS (
                $COL_LEVEL INTEGER PRIMARY KEY,
                $COL_UNLOCKED INTEGER NOT NULL,
                $COL_BEST_SCORE INTEGER NOT NULL DEFAULT 0,
                $COL_BEST_TIME_MS INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_results_level ON $TABLE_RESULTS ($COL_LEVEL)")
        db.execSQL("CREATE INDEX idx_results_score ON $TABLE_RESULTS ($COL_SCORE)")

        seedLevelProgress(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "GameDbHelper onUpgrade dari versi $oldVersion ke $newVersion: drop & recreate")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESULTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROGRESS")
        onCreate(db)
    }

    private fun seedLevelProgress(db: SQLiteDatabase) {
        for (lvl in 1..TOTAL_LEVELS) {
            val cv = ContentValues().apply {
                put(COL_LEVEL, lvl)
                put(COL_UNLOCKED, if (lvl == 1) 1 else 0)
                put(COL_BEST_SCORE, 0)
                put(COL_BEST_TIME_MS, 0)
            }
            db.insert(TABLE_PROGRESS, null, cv)
        }
        Log.d(TAG, "Seed level_progress selesai: Level 1 terbuka, Level 2-5 terkunci")
    }

    // ─── Operasi game_results ───────────────────────────────────────────────

    fun insertResult(result: GameResult): Long {
        val cv = ContentValues().apply {
            put(COL_LEVEL, result.level)
            put(COL_SCORE, result.score)
            put(COL_DURATION_MS, result.durationMs)
            put(COL_PENALTY_MS, result.penaltyMs)
            put(COL_COMPLETED_AT, result.completedAt)
        }
        val rowId = writableDatabase.insert(TABLE_RESULTS, null, cv)
        if (rowId == -1L) {
            Log.e(TAG, "GAGAL insertResult untuk Level ${result.level}")
        } else {
            Log.d(TAG, "insertResult berhasil: rowId=$rowId, Level=${result.level}, Skor=${result.score}")
        }
        return rowId
    }

    fun getAllResults(): List<GameResult> {
        val list = mutableListOf<GameResult>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_RESULTS ORDER BY $COL_COMPLETED_AT DESC", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    GameResult(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        level = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LEVEL)),
                        score = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCORE)),
                        durationMs = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DURATION_MS)),
                        penaltyMs = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PENALTY_MS)),
                        completedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPLETED_AT))
                    )
                )
            }
        }
        Log.d(TAG, "getAllResults: ditemukan ${list.size} riwayat")
        return list
    }

    fun getHighestScore(): Int {
        readableDatabase.rawQuery(
            "SELECT MAX($COL_SCORE) FROM $TABLE_RESULTS", null
        ).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) return cursor.getInt(0)
        }
        return 0
    }

    fun getHighestScoreByLevel(level: Int): Int {
        readableDatabase.rawQuery(
            "SELECT MAX($COL_SCORE) FROM $TABLE_RESULTS WHERE $COL_LEVEL = ?",
            arrayOf(level.toString())
        ).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) return cursor.getInt(0)
        }
        return 0
    }

    fun deleteResult(id: Long) {
        val rows = writableDatabase.delete(TABLE_RESULTS, "$COL_ID = ?", arrayOf(id.toString()))
        Log.d(TAG, "deleteResult id=$id: $rows baris terhapus")
    }

    fun deleteAllResults() {
        val rows = writableDatabase.delete(TABLE_RESULTS, null, null)
        Log.d(TAG, "deleteAllResults: $rows baris terhapus")
    }

    // ─── Operasi level_progress ─────────────────────────────────────────────

    fun getAllProgress(): List<LevelProgress> {
        val list = mutableListOf<LevelProgress>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_PROGRESS ORDER BY $COL_LEVEL", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    LevelProgress(
                        level = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LEVEL)),
                        unlocked = cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNLOCKED)) == 1,
                        bestScore = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BEST_SCORE)),
                        bestTimeMs = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BEST_TIME_MS))
                    )
                )
            }
        }
        return list
    }

    fun unlockLevel(level: Int) {
        val cv = ContentValues().apply { put(COL_UNLOCKED, 1) }
        val rows = writableDatabase.update(TABLE_PROGRESS, cv, "$COL_LEVEL = ?", arrayOf(level.toString()))
        Log.d(TAG, "unlockLevel $level: $rows baris terupdate")
    }

    fun updateBest(level: Int, score: Int, timeMs: Long) {
        val current = getAllProgress().firstOrNull { it.level == level } ?: return
        val shouldUpdate = score > current.bestScore ||
                (score == current.bestScore && (current.bestTimeMs == 0L || timeMs < current.bestTimeMs))
        if (!shouldUpdate) return

        val cv = ContentValues().apply {
            put(COL_BEST_SCORE, score)
            put(COL_BEST_TIME_MS, timeMs)
        }
        writableDatabase.update(TABLE_PROGRESS, cv, "$COL_LEVEL = ?", arrayOf(level.toString()))
        Log.d(TAG, "updateBest Level $level: skor=$score, waktu=${timeMs}ms")
    }

    /**
     * Simpan hasil permainan + perbarui rekor + buka level berikutnya dalam SATU transaksi atomik.
     * Pengecekan oldBest dilakukan SEBELUM insert agar rekor baru terdeteksi akurat.
     */
    fun saveResultAndProgress(result: GameResult): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            // 1. Cek rekor lama SEBELUM insert
            val oldBest = getHighestScoreByLevel(result.level)
            val isNewRecord = result.score > oldBest

            // 2. Insert hasil permainan
            val cv = ContentValues().apply {
                put(COL_LEVEL, result.level)
                put(COL_SCORE, result.score)
                put(COL_DURATION_MS, result.durationMs)
                put(COL_PENALTY_MS, result.penaltyMs)
                put(COL_COMPLETED_AT, result.completedAt)
            }
            val rowId = db.insert(TABLE_RESULTS, null, cv)
            if (rowId == -1L) {
                Log.e(TAG, "saveResultAndProgress: GAGAL insert hasil ke tabel $TABLE_RESULTS")
            }

            // 3. Update best score & time jika lebih baik
            updateBest(result.level, result.score, result.durationMs + result.penaltyMs)

            // 4. Buka level berikutnya
            val nextLevel = result.level + 1
            if (nextLevel <= TOTAL_LEVELS) {
                val cvUnlock = ContentValues().apply { put(COL_UNLOCKED, 1) }
                db.update(TABLE_PROGRESS, cvUnlock, "$COL_LEVEL = ?", arrayOf(nextLevel.toString()))
                Log.d(TAG, "saveResultAndProgress: Level $nextLevel berhasil dibuka!")
            }

            db.setTransactionSuccessful()
            Log.d(TAG, "saveResultAndProgress SUKSES: Level=${result.level}, Skor=${result.score}, RekorBaru=$isNewRecord")
            isNewRecord
        } catch (e: Exception) {
            Log.e(TAG, "saveResultAndProgress ERROR: ${e.message}", e)
            false
        } finally {
            db.endTransaction()
        }
    }

    fun resetProgress() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (lvl in 1..TOTAL_LEVELS) {
                val cv = ContentValues().apply {
                    put(COL_UNLOCKED, if (lvl == 1) 1 else 0)
                    put(COL_BEST_SCORE, 0)
                    put(COL_BEST_TIME_MS, 0)
                }
                db.update(TABLE_PROGRESS, cv, "$COL_LEVEL = ?", arrayOf(lvl.toString()))
            }
            db.setTransactionSuccessful()
            Log.d(TAG, "resetProgress: Progres direset ke kondisi awal")
        } catch (e: Exception) {
            Log.e(TAG, "resetProgress ERROR: ${e.message}", e)
        } finally {
            db.endTransaction()
        }
    }
}
