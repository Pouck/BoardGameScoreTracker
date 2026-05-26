package com.example.boardgamescoretracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT * FROM games WHERE gameId = :gameId")
    fun getGameByIdFlow(gameId: Int): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE gameId = :gameId")
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("SELECT * FROM games WHERE finishedAt IS NULL ORDER BY createdAt DESC LIMIT 1")
    fun getCurrentGame(): Flow<GameEntity?>

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("UPDATE games SET finishedAt = :finishedAt WHERE gameId = :gameId")
    suspend fun finishGame(gameId: Int, finishedAt: Long)

    @Query("UPDATE games SET winnerNames = :winnerNames WHERE gameId = :gameId")
    suspend fun setWinners(gameId: Int, winnerNames: String?)

    @Query("UPDATE games SET startingPlayerId = :playerId WHERE gameId = :gameId")
    suspend fun setStartingPlayer(gameId: Int, playerId: Int?)
}

@Dao
interface PlayerDao {
    @Insert
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Query("SELECT * FROM players WHERE playerId = :playerId")
    suspend fun getPlayerById(playerId: Int): PlayerEntity?

    @Query("SELECT * FROM players WHERE isActive = 1 ORDER BY playerName ASC")
    fun getActivePlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY playerName ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("UPDATE players SET isActive = 0 WHERE playerId = :playerId")
    suspend fun deactivatePlayer(playerId: Int)

    @Query("UPDATE players SET isActive = 1 WHERE playerId = :playerId")
    suspend fun activatePlayer(playerId: Int)
}

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(score: ScoreEntity): Long

    @Update
    suspend fun updateScore(score: ScoreEntity)

    @Delete
    suspend fun deleteScore(score: ScoreEntity)

    @Query("SELECT * FROM scores WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun getScore(gameId: Int, playerId: Int): ScoreEntity?

    @Query("SELECT * FROM scores WHERE gameId = :gameId ORDER BY score DESC")
    fun getGameScores(gameId: Int): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM scores WHERE playerId = :playerId ORDER BY updatedAt DESC")
    fun getPlayerScores(playerId: Int): Flow<List<ScoreEntity>>

    @Query("UPDATE scores SET score = :newScore, updatedAt = :timestamp WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun updatePlayerScore(gameId: Int, playerId: Int, newScore: Int, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM scores WHERE gameId = :gameId")
    suspend fun deleteGameScores(gameId: Int)

    @Query("""
        SELECT players.playerId, players.playerName, scores.score 
        FROM players 
        JOIN scores ON players.playerId = scores.playerId 
        WHERE scores.gameId = :gameId
    """)
    fun getPlayersWithScoresForGame(gameId: Int): Flow<List<PlayerScore>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertRoundScores(roundScores: List<RoundScoreEntity>)

    @Query("SELECT * FROM round_scores WHERE gameId = :gameId ORDER BY round ASC, playerId ASC")
    fun getRoundHistoryForGame(gameId: Int): Flow<List<RoundScoreEntity>>

    @Query("DELETE FROM round_scores WHERE gameId = :gameId")
    suspend fun deleteRoundScores(gameId: Int)

    @Query("DELETE FROM round_scores WHERE gameId = :gameId AND round = :round")
    suspend fun deleteRound(gameId: Int, round: Int)

    @Query("UPDATE games SET currentRound = currentRound - 1 WHERE gameId = :gameId AND currentRound > 1")
    suspend fun decrementRound(gameId: Int)
}
