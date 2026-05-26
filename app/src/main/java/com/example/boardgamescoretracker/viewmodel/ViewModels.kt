package com.example.boardgamescoretracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.boardgamescoretracker.data.db.GameEntity
import com.example.boardgamescoretracker.data.db.PlayerEntity
import com.example.boardgamescoretracker.data.db.ScoreEntity
import com.example.boardgamescoretracker.data.db.PlayerScore
import com.example.boardgamescoretracker.data.repository.GameRepository
import com.example.boardgamescoretracker.data.repository.PlayerRepository
import com.example.boardgamescoretracker.data.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    val allGames: Flow<List<GameEntity>> = gameRepository.getAllGames()
    val currentGame: Flow<GameEntity?> = gameRepository.getCurrentGame()

    fun getGameById(gameId: Int): Flow<GameEntity?> = gameRepository.getGameById(gameId)

    fun startNewGame(gameName: String, winningScore: Int? = null, maxRounds: Int? = null, playersToCopy: List<String>? = null, onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val gameId = gameRepository.createGame(gameName, winningScore, maxRounds)
            
            playersToCopy?.forEach { playerName ->
                val playerId = playerRepository.addPlayer(playerName)
                scoreRepository.initializeScore(gameId.toInt(), playerId.toInt())
            }

            onCreated(gameId.toInt())
        }
    }

    fun incrementRound(gameId: Int, playerScores: List<PlayerScore>, currentRound: Int, winningScore: Int?, maxRounds: Int?, startingPlayerId: Int?) {
        viewModelScope.launch {
            val history = scoreRepository.getRoundHistory(gameId).first()
            val previousTotals = history.groupBy { it.playerId }.mapValues { entry -> entry.value.sumOf { it.scoreIncrement } }

            val roundScores = playerScores.map { 
                val previousTotal = previousTotals[it.playerId] ?: 0
                val increment = it.score - previousTotal
                com.example.boardgamescoretracker.data.db.RoundScoreEntity(
                    gameId = gameId,
                    round = currentRound,
                    playerId = it.playerId,
                    scoreIncrement = increment,
                    wasStartingPlayer = it.playerId == startingPlayerId
                )
            }
            scoreRepository.saveRoundScores(roundScores)
            
            val nextRound = currentRound + 1
            gameRepository.incrementRound(gameId)

            // Check if game should end due to score or max rounds
            val playersWhoMetScore = if (winningScore != null) playerScores.filter { it.score >= winningScore } else emptyList()
            val roundsExceeded = maxRounds != null && nextRound > maxRounds

            if (playersWhoMetScore.isNotEmpty() || roundsExceeded) {
                val winners = if (playersWhoMetScore.isNotEmpty()) {
                    val highestMet = playersWhoMetScore.maxOf { it.score }
                    playersWhoMetScore.filter { it.score == highestMet }
                } else {
                    val highestScore = playerScores.maxOfOrNull { it.score } ?: 0
                    playerScores.filter { it.score == highestScore }
                }
                
                val winnersStr = winners.joinToString(", ") { it.playerName }
                gameRepository.setWinners(gameId, winnersStr)
                gameRepository.finishGame(gameId)
            }
        }
    }

    fun undoLastRound(gameId: Int, lastRound: Int) {
        viewModelScope.launch {
            gameRepository.undoRound(gameId, lastRound)
            // Recalculate totals
            val history = scoreRepository.getRoundHistory(gameId).first()
            val players = scoreRepository.getPlayersWithScoresForGame(gameId).first()
            players.forEach { player ->
                val newTotal = history.filter { it.playerId == player.playerId }.sumOf { it.scoreIncrement }
                scoreRepository.updateScore(gameId, player.playerId, newTotal)
            }
        }
    }

    fun duplicateGame(sourceGame: GameEntity, onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val newGameId = gameRepository.createGame(
                sourceGame.gameName,
                sourceGame.winningScore,
                sourceGame.maxRounds
            )

            val players = scoreRepository.getPlayersWithScoresForGame(sourceGame.gameId).first()
            players.forEach { player ->
                scoreRepository.initializeScore(newGameId.toInt(), player.playerId)
            }
            onCreated(newGameId.toInt())
        }
    }

    fun finishCurrentGame(gameId: Int, playerScores: List<PlayerScore>, winningScore: Int?, maxRounds: Int?) {
        viewModelScope.launch {
            val game = gameRepository.getGameByIdOneShot(gameId)
            val playersWhoMetScore = if (winningScore != null) playerScores.filter { it.score >= winningScore } else emptyList()
            val roundsExceeded = maxRounds != null && game?.currentRound?.let { it > maxRounds } == true

            if (playersWhoMetScore.isNotEmpty() || roundsExceeded) {
                val winners = if (playersWhoMetScore.isNotEmpty()) {
                    val highestMet = playersWhoMetScore.maxOf { it.score }
                    playersWhoMetScore.filter { it.score == highestMet }
                } else {
                    val highestScore = playerScores.maxOfOrNull { it.score } ?: 0
                    playerScores.filter { it.score == highestScore }
                }
                val winnersStr = winners.joinToString(", ") { it.playerName }
                gameRepository.setWinners(gameId, winnersStr)
            }
            gameRepository.finishGame(gameId)
        }
    }

    fun setStartingPlayer(gameId: Int, playerId: Int?) {
        viewModelScope.launch {
            gameRepository.setStartingPlayer(gameId, playerId)
        }
    }

    fun pickRandomStartingPlayer(gameId: Int, players: List<PlayerScore>) {
        if (players.isEmpty()) return
        val randomPlayer = players.random()
        setStartingPlayer(gameId, randomPlayer.playerId)
    }

    fun deleteGame(gameId: Int) {
        viewModelScope.launch {
            gameRepository.deleteGame(gameId)
        }
    }
}

class PlayerViewModel(
    private val playerRepository: PlayerRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    val activePlayers: Flow<List<PlayerEntity>> = playerRepository.getActivePlayers()

    fun getPlayersWithScoresForGame(gameId: Int): Flow<List<PlayerScore>> =
        scoreRepository.getPlayersWithScoresForGame(gameId)

    fun getRoundHistory(gameId: Int): Flow<List<com.example.boardgamescoretracker.data.db.RoundScoreEntity>> =
        scoreRepository.getRoundHistory(gameId)

    fun addPlayerToGame(gameId: Int, playerName: String) {
        viewModelScope.launch {
            val playerId = playerRepository.addPlayer(playerName)
            scoreRepository.initializeScore(gameId, playerId.toInt())
        }
    }

    fun addPlayer(playerName: String) {
        viewModelScope.launch {
            playerRepository.addPlayer(playerName)
        }
    }

    fun removePlayer(playerId: Int) {
        viewModelScope.launch {
            playerRepository.removePlayer(playerId)
        }
    }

    fun restorePlayer(playerId: Int) {
        viewModelScope.launch {
            playerRepository.restorePlayer(playerId)
        }
    }

    fun updateScore(gameId: Int, playerId: Int, newScore: Int) {
        viewModelScope.launch {
            scoreRepository.updateScore(gameId, playerId, newScore)
        }
    }

    fun updateRoundScore(gameId: Int, round: Int, playerId: Int, newIncrement: Int) {
        viewModelScope.launch {
            val history = scoreRepository.getRoundHistory(gameId).first()
            val existingEntry = history.find { it.round == round && it.playerId == playerId }
            
            scoreRepository.saveRoundScores(listOf(
                com.example.boardgamescoretracker.data.db.RoundScoreEntity(
                    gameId = gameId,
                    round = round,
                    playerId = playerId,
                    scoreIncrement = newIncrement,
                    wasStartingPlayer = existingEntry?.wasStartingPlayer ?: false
                )
            ))
            // Recalculate total for this player
            val updatedHistory = scoreRepository.getRoundHistory(gameId).first()
            val newTotal = updatedHistory.filter { it.playerId == playerId }.sumOf { it.scoreIncrement }
            scoreRepository.updateScore(gameId, playerId, newTotal)
        }
    }

    fun initializeScore(gameId: Int, playerId: Int) {
        viewModelScope.launch {
            scoreRepository.initializeScore(gameId, playerId)
        }
    }
}

class GameViewModelFactory(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private val scoreRepository: ScoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(gameRepository, playerRepository, scoreRepository) as T
    }
}

class PlayerViewModelFactory(
    private val playerRepository: PlayerRepository,
    private val scoreRepository: ScoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(playerRepository, scoreRepository) as T
    }
}
