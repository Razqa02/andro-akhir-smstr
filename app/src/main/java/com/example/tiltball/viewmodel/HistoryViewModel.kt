package com.example.tiltball.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiltball.data.model.GameResult
import com.example.tiltball.data.model.LevelProgress
import com.example.tiltball.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val results: List<GameResult> = emptyList(),
    val progress: List<LevelProgress> = emptyList(),
    val highestScore: Int = 0,
    val isLoading: Boolean = true
)

/**
 * ViewModel untuk HistoryScreen dan sinkronisasi skor/progres level.
 */
class HistoryViewModel(private val repository: GameRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Muat ulang data terbaru dari SQLite */
    fun refresh() {
        viewModelScope.launch {
            try {
                val results = repository.getAllResults()
                val progress = repository.getAllProgress()
                val highest = repository.getHighestScore()
                _uiState.update {
                    it.copy(
                        results = results,
                        progress = progress,
                        highestScore = highest,
                        isLoading = false
                    )
                }
                Log.d("TiltBall", "HistoryViewModel refreshed: ${results.size} riwayat, highestScore=$highest")
            } catch (e: Exception) {
                Log.e("TiltBall", "HistoryViewModel refresh error: ${e.message}", e)
            }
        }
    }

    fun deleteResult(id: Long) {
        viewModelScope.launch {
            repository.deleteResult(id)
            refresh()
        }
    }

    fun deleteAllResults() {
        viewModelScope.launch {
            repository.deleteAllResults()
            refresh()
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            refresh()
        }
    }
}
