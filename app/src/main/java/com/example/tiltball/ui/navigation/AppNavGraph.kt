package com.example.tiltball.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tiltball.ui.screens.*
import com.example.tiltball.viewmodel.GameViewModel
import com.example.tiltball.viewmodel.HistoryViewModel
import com.example.tiltball.viewmodel.SensorViewModel
import com.example.tiltball.viewmodel.ViewModelFactory

/** Semua rute navigasi sebagai konstanta string. */
object Routes {
    const val MENU = "menu"
    const val LEVELS = "levels"
    const val GAME = "game/{level}"
    const val COMPLETE = "complete/{level}"
    const val GAME_OVER = "gameover/{level}"
    const val HISTORY = "history"
    const val HOW_TO_PLAY = "howto"
    const val ABOUT = "about"
    const val SENSORS = "sensors"

    fun game(level: Int) = "game/$level"
    fun complete(level: Int) = "complete/$level"
    fun gameOver(level: Int) = "gameover/$level"
}

/**
 * Graph navigasi utama aplikasi.
 * GameViewModel, HistoryViewModel, dan SensorViewModel dibuat SATU KALI di sini
 * dan dioper ke composable terkait agar seluruh layar membaca instance yang SAMA.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    factory: ViewModelFactory,
    modifier: Modifier = Modifier
) {
    // ─── Single ViewModel Instances di Scope NavGraph ────────────────────────
    val gameVm: GameViewModel = viewModel(factory = factory)
    val historyVm: HistoryViewModel = viewModel(factory = factory)
    val sensorVm: SensorViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Routes.MENU,
        modifier = modifier
    ) {
        // ─── Menu Utama ──────────────────────────────────────────────────────
        composable(Routes.MENU) {
            MainMenuScreen(
                historyVm = historyVm,
                onPlayClick = { navController.navigate(Routes.LEVELS) },
                onHighScoreClick = { navController.navigate(Routes.HISTORY) },
                onHowToPlayClick = { navController.navigate(Routes.HOW_TO_PLAY) },
                onAboutClick = { navController.navigate(Routes.ABOUT) },
                onSensorsClick = { navController.navigate(Routes.SENSORS) }
            )
        }

        // ─── Pilih Level ─────────────────────────────────────────────────────
        composable(Routes.LEVELS) {
            LevelSelectScreen(
                historyVm = historyVm,
                onLevelSelected = { level ->
                    gameVm.startLevel(level)
                    navController.navigate(Routes.game(level))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Game ────────────────────────────────────────────────────────────
        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            GameScreen(
                level = level,
                gameVm = gameVm,
                sensorController = sensorVm.sensorController,
                onLevelComplete = { lvl ->
                    navController.navigate(Routes.complete(lvl)) {
                        popUpTo(Routes.game(lvl)) { inclusive = true }
                    }
                },
                onGameOver = { lvl ->
                    navController.navigate(Routes.gameOver(lvl)) {
                        popUpTo(Routes.game(lvl)) { inclusive = true }
                    }
                },
                onExit = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                }
            )
        }

        // ─── Level Selesai ────────────────────────────────────────────────────
        composable(
            route = Routes.COMPLETE,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            LevelCompleteScreen(
                level = level,
                gameVm = gameVm,
                historyVm = historyVm,
                onNextLevel = { nextLvl ->
                    gameVm.startLevel(nextLvl)
                    navController.navigate(Routes.game(nextLvl)) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                },
                onReplay = {
                    gameVm.startLevel(level)
                    navController.navigate(Routes.game(level)) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                },
                onMenu = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                }
            )
        }

        // ─── Game Over ────────────────────────────────────────────────────────
        composable(
            route = Routes.GAME_OVER,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            GameOverScreen(
                level = level,
                onReplay = {
                    gameVm.startLevel(level)
                    navController.navigate(Routes.game(level)) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                },
                onMenu = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                }
            )
        }

        // ─── Riwayat ─────────────────────────────────────────────────────────
        composable(Routes.HISTORY) {
            HistoryScreen(
                historyVm = historyVm,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Cara Bermain ─────────────────────────────────────────────────────
        composable(Routes.HOW_TO_PLAY) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }

        // ─── Tentang ─────────────────────────────────────────────────────────
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        // ─── Tes Sensor ───────────────────────────────────────────────────────
        composable(Routes.SENSORS) {
            SensorScreen(
                factory = factory,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
