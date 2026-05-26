package com.example.boardgamescoretracker.data.repository

import com.example.boardgamescoretracker.data.db.GameDao
import com.example.boardgamescoretracker.data.db.GameEntity
import com.example.boardgamescoretracker.data.db.PlayerDao
import com.example.boardgamescoretracker.data.db.PlayerEntity
import com.example.boardgamescoretracker.data.db.ScoreDao
import com.example.boardgamescoretracker.data.db.ScoreEntity
import com.example.boardgamescoretracker.data.db.PlayerScore
import com.example.boardgamescoretracker.data.db.RoundScoreEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameDao: GameDao,
    private val scoreDao: ScoreDao
) {
    fun getAllGames(): Flow<List<GameEntity>> = gameDao.getAllGames()

    fun getCurrentGame(): Flow<GameEntity?> = gameDao.getCurrentGame()

    fun getGameById(gameId: Int): Flow<GameEntity?> = gameDao.getGameByIdFlow(gameId)

    suspend fun getGameByIdOneShot(gameId: Int): GameEntity? = gameDao.getGameById(gameId)

    suspend fun createGame(gameName: String, winningScore: Int? = null, maxRounds: Int? = null): Long {
        return gameDao.insertGame(GameEntity(gameName = gameName, winningScore = winningScore, maxRounds = maxRounds))
    }

    suspend fun incrementRound(gameId: Int) {
        gameDao.getGameById(gameId)?.let { game ->
            gameDao.updateGame(game.copy(currentRound = game.currentRound + 1))
        }
    }

    suspend fun undoRound(gameId: Int, round: Int) {
        scoreDao.decrementRound(gameId)
        scoreDao.deleteRound(gameId, round)
    }

    suspend fun finishGame(gameId: Int) {
        gameDao.finishGame(gameId, System.currentTimeMillis())
    }

    suspend fun setWinners(gameId: Int, winnerNames: String?) {
        gameDao.setWinners(gameId, winnerNames)
    }

    suspend fun setStartingPlayer(gameId: Int, playerId: Int?) {
        gameDao.setStartingPlayer(gameId, playerId)
    }

    suspend fun deleteGame(gameId: Int) {
        gameDao.getGameById(gameId)?.let { game ->
            scoreDao.deleteGameScores(gameId)
            scoreDao.deleteRoundScores(gameId)
            gameDao.deleteGame(game)
        }
    }
}

class PlayerRepository(
    private val playerDao: PlayerDao
) {
    fun getActivePlayers(): Flow<List<PlayerEntity>> =
        playerDao.getActivePlayers()

    fun getAllPlayers(): Flow<List<PlayerEntity>> =
        playerDao.getAllPlayers()

    suspend fun addPlayer(playerName: String): Long {
        return playerDao.insertPlayer(PlayerEntity(playerName = playerName))
    }

    suspend fun removePlayer(playerId: Int) {
        playerDao.deactivatePlayer(playerId)
    }

    suspend fun restorePlayer(playerId: Int) {
        playerDao.activatePlayer(playerId)
    }
}

class ScoreRepository(
    private val scoreDao: ScoreDao
) {
    fun getGameScores(gameId: Int) = scoreDao.getGameScores(gameId)

    fun getPlayersWithScoresForGame(gameId: Int): Flow<List<PlayerScore>> =
        scoreDao.getPlayersWithScoresForGame(gameId)

    fun getRoundHistory(gameId: Int): Flow<List<RoundScoreEntity>> =
        scoreDao.getRoundHistoryForGame(gameId)

    suspend fun updateScore(gameId: Int, playerId: Int, newScore: Int) {
        scoreDao.updatePlayerScore(gameId, playerId, newScore)
    }

    suspend fun saveRoundScores(roundScores: List<RoundScoreEntity>) {
        scoreDao.insertRoundScores(roundScores)
    }

    suspend fun initializeScore(gameId: Int, playerId: Int) {
        scoreDao.insertScore(
            ScoreEntity(
                gameId = gameId,
                playerId = playerId,
                score = 0
            )
        )
    }
}
