package com.example.boardgamescoretracker

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.boardgamescoretracker.data.db.ScoreTrackerDatabase
import com.example.boardgamescoretracker.data.repository.GameRepository
import com.example.boardgamescoretracker.data.repository.PlayerRepository
import com.example.boardgamescoretracker.data.repository.ScoreRepository
import com.example.boardgamescoretracker.ui.screens.GameListScreen
import com.example.boardgamescoretracker.ui.screens.ActiveGameScreen
import com.example.boardgamescoretracker.ui.screens.NewGameScreen
import com.example.boardgamescoretracker.ui.theme.BoardGameScoreTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BoardGameScoreTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BoardGameScoreTrackerApp()
                }
            }
        }
    }
}

@Composable
fun BoardGameScoreTrackerApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "game_list") {
        composable("game_list") {
            GameListScreen(navController = navController)
        }
        composable("new_game") {
            NewGameScreen(navController = navController)
        }
        composable("active_game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: return@composable
            ActiveGameScreen(gameId = gameId, navController = navController)
        }
    }
}

class ScoreTrackerApplication : Application() {
    lateinit var database: ScoreTrackerDatabase
    lateinit var gameRepository: GameRepository
    lateinit var playerRepository: PlayerRepository
    lateinit var scoreRepository: ScoreRepository

    override fun onCreate() {
        super.onCreate()
        database = ScoreTrackerDatabase.getInstance(this)
        gameRepository = GameRepository(database.gameDao(), database.scoreDao())
        playerRepository = PlayerRepository(database.playerDao())
        scoreRepository = ScoreRepository(database.scoreDao(), database.categoryScoreDao())
    }
}
