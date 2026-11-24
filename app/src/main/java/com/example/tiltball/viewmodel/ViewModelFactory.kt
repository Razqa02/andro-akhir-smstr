package com.example.tiltball.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tiltball.data.local.GameDbHelper
import com.example.tiltball.data.repository.GameRepository
import com.example.tiltball.sensor.SensorController

/**
 * Factory untuk membuat semua ViewModel.
 * Dependency injection manual — tanpa Hilt/Dagger/Koin.
 *
 * Singleton sederhana: satu instance DbHelper dan Repository dibagi antar ViewModel.
 */
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    // Singleton dependency (dibuat sekali, dipakai semua ViewModel)
    private val dbHelper: GameDbHelper by lazy { GameDbHelper(application) }
    private val repository: GameRepository by lazy { GameRepository(dbHelper) }
    private val sensorController: SensorController by lazy { SensorController(application) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) ->
                GameViewModel(application, repository) as T

            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T

            modelClass.isAssignableFrom(SensorViewModel::class.java) ->
                SensorViewModel(application, sensorController) as T

            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
