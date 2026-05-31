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

    fun startNewGame(gameName: String, winningScore: Int? = null, maxRounds: Int? = null, gameType: String = "Generic", gameConfig: String? = null, playersToCopy: List<String>? = null, onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val gameId = gameRepository.createGame(gameName, winningScore, maxRounds, gameType, gameConfig)
            
            playersToCopy?.forEach { playerName ->
                val playerId = playerRepository.addPlayer(playerName)
                scoreRepository.initializeScore(gameId.toInt(), playerId.toInt())
            }

            onCreated(gameId.toInt())
        }
    }

    fun incrementRound(gameId: Int, playerScores: List<PlayerScore>, currentRound: Int, winningScore: Int?, maxRounds: Int?, startingPlayerId: Int?, wingspanInputs: Map<Int, Int>? = null, gameType: String = "Generic", wingspanConfig: String? = null) {
        viewModelScope.launch {
            val history = scoreRepository.getRoundHistory(gameId).first()
            val previousTotals = history.groupBy { it.playerId }.mapValues { entry -> entry.value.sumOf { it.scoreIncrement } }

            val roundScores = if (gameType == "Wingspan" && currentRound <= 4) {
                calculateWingspanRoundScores(gameId, currentRound, playerScores, wingspanInputs ?: emptyMap(), wingspanConfig ?: "Green", startingPlayerId)
            } else if (gameType == "Wingspan" && currentRound in 5..9) {
                // For Wingspan categories, use inputs as direct increments
                playerScores.map { 
                    val increment = wingspanInputs?.get(it.playerId) ?: 0
                    com.example.boardgamescoretracker.data.db.RoundScoreEntity(
                        gameId = gameId,
                        round = currentRound,
                        playerId = it.playerId,
                        scoreIncrement = increment,
                        wasStartingPlayer = it.playerId == startingPlayerId,
                        rawWingspanValue = increment
                    )
                }
            } else {
                playerScores.map { 
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
            }
            scoreRepository.saveRoundScores(roundScores)
            
            // Recalculate/Update main scores table
            roundScores.forEach { rs ->
                val total = (previousTotals[rs.playerId] ?: 0) + rs.scoreIncrement
                scoreRepository.updateScore(gameId, rs.playerId, total)
            }

            val nextRound = currentRound + 1
            gameRepository.incrementRound(gameId)

            // Check if game should end
            if (gameType == "Generic") {
                val playersWhoMetScore = if (winningScore != null) playerScores.filter { it.score >= winningScore } else emptyList()
                val roundsExceeded = maxRounds != null && nextRound > maxRounds

                if (playersWhoMetScore.isNotEmpty() || roundsExceeded) {
                    finishGameInternal(gameId)
                }
            } else if (gameType == "Wingspan" && nextRound > 9) { // 4 goals + 5 categories
                // Wingspan categories are finished, but we don't auto-finish anymore
                // to allow the user to click "Finish Game" after reviewing the final tally.
            }
        }
    }

    fun finishWingspanGame(gameId: Int) {
        viewModelScope.launch {
            finishGameInternal(gameId)
        }
    }

    private suspend fun finishGameInternal(gameId: Int) {
        val history = scoreRepository.getRoundHistory(gameId).first()
        val finalScores = history.groupBy { it.playerId }.mapValues { e -> e.value.sumOf { it.scoreIncrement } }
        val maxScore = finalScores.values.maxOrNull() ?: 0
        val winningPlayerIds = finalScores.filter { it.value == maxScore }.keys
        
        val allPlayers = scoreRepository.getPlayersWithScoresForGame(gameId).first()
        val winnersStr = allPlayers.filter { it.playerId in winningPlayerIds }.joinToString(", ") { it.playerName }
        
        gameRepository.setWinners(gameId, winnersStr)
        gameRepository.finishGame(gameId)
    }

    private fun calculateWingspanRoundScores(gameId: Int, round: Int, players: List<PlayerScore>, inputs: Map<Int, Int>, config: String, startingPlayerId: Int?): List<com.example.boardgamescoretracker.data.db.RoundScoreEntity> {
        return if (config == "Green") {
            // Competitive: Rank input (1-4, 0 for none)
            val roundPoints = when (round) {
                1 -> listOf(4, 1, 0, 0)
                2 -> listOf(5, 2, 1, 0)
                3 -> listOf(6, 3, 2, 0)
                4 -> listOf(7, 4, 3, 0)
                else -> listOf(0, 0, 0, 0)
            }
            
            // Map player to their selected rank
            val playerRanks = players.associate { it.playerId to (inputs[it.playerId] ?: 0) }
            
            players.map { player ->
                val rank = playerRanks[player.playerId] ?: 0
                var points = 0
                if (rank in 1..4) {
                    val playersAtSameRank = players.count { playerRanks[it.playerId] == rank }
                    val playersAtHigherRanks = players.count { r -> (playerRanks[r.playerId] ?: 0) in 1 until rank }
                    
                    // Sum points from 'rank' to 'rank + playersAtSameRank - 1'
                    val startIndex = playersAtHigherRanks
                    val endIndex = (startIndex + playersAtSameRank - 1).coerceAtMost(3)
                    
                    if (startIndex <= 3) {
                        val totalPointsForTie = roundPoints.slice(startIndex..endIndex).sum()
                        points = totalPointsForTie / playersAtSameRank
                    }
                }
                
                com.example.boardgamescoretracker.data.db.RoundScoreEntity(
                    gameId = gameId,
                    round = round,
                    playerId = player.playerId,
                    scoreIncrement = points,
                    wasStartingPlayer = player.playerId == startingPlayerId,
                    rawWingspanValue = rank
                )
            }
        } else {
            // Blue: Item count input (points = count, max 5)
            players.map { player ->
                val count = inputs[player.playerId] ?: 0
                val points = count.coerceAtMost(5)
                com.example.boardgamescoretracker.data.db.RoundScoreEntity(
                    gameId = gameId,
                    round = round,
                    playerId = player.playerId,
                    scoreIncrement = points,
                    wasStartingPlayer = player.playerId == startingPlayerId,
                    rawWingspanValue = count
                )
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
                sourceGame.maxRounds,
                sourceGame.gameType,
                sourceGame.gameConfig
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

    fun updateWingspanRawValue(gameId: Int, round: Int, playerId: Int, value: Int) {
        viewModelScope.launch {
            // We store it in round_scores with 0 increment for now, just to persist the raw value
            // Actually, we should probably update an existing record if it exists
            val history = scoreRepository.getRoundHistory(gameId).first()
            val existing = history.find { it.round == round && it.playerId == playerId }
            
            scoreRepository.saveRoundScores(listOf(
                com.example.boardgamescoretracker.data.db.RoundScoreEntity(
                    gameId = gameId,
                    round = round,
                    playerId = playerId,
                    scoreIncrement = existing?.scoreIncrement ?: 0,
                    wasStartingPlayer = existing?.wasStartingPlayer ?: false,
                    rawWingspanValue = value
                )
            ))
        }
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

    fun getCategoryScores(gameId: Int): Flow<List<com.example.boardgamescoretracker.data.db.CategoryScoreEntity>> =
        scoreRepository.getCategoryScores(gameId)

    fun updateCategoryScore(gameId: Int, playerId: Int, category: String, value: Int) {
        viewModelScope.launch {
            scoreRepository.updateCategoryScore(gameId, playerId, category, value)
            
            val updatedCategories = scoreRepository.getCategoryScores(gameId).first()
            val playerTotal = updatedCategories.filter { it.playerId == playerId }.sumOf { it.scoreValue }
            scoreRepository.updateScore(gameId, playerId, playerTotal)
        }
    }

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
