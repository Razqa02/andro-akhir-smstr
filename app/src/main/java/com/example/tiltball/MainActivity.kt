package com.example.tiltball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.tiltball.game.LevelValidator
import com.example.tiltball.ui.navigation.AppNavGraph
import com.example.tiltball.ui.theme.Background
import com.example.tiltball.ui.theme.TIltBallTheme
import com.example.tiltball.viewmodel.ViewModelFactory

/**
 * Single Activity untuk seluruh aplikasi Tilt Ball.
 * Mengatur Edge-to-Edge, inisialisasi ViewModelFactory, dan validasi level.
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Validasi semua 5 level dengan BFS saat startup untuk memastikan semuanya solvable
        LevelValidator.validateAll()

        // Inisialisasi factory untuk manual DI
        viewModelFactory = ViewModelFactory(application)

        setContent {
            TIltBallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        factory = viewModelFactory
                    )
                }
            }
        }
    }
}
