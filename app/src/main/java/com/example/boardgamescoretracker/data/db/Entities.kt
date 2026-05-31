package com.example.boardgamescoretracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val gameId: Int = 0,
    val gameName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val winningScore: Int? = null,
    val currentRound: Int = 1,
    val maxRounds: Int? = null,
    val winnerNames: String? = null,
    val startingPlayerId: Int? = null,
    val gameType: String = "Generic",
    val gameConfig: String? = null // For Wingspan: "Green" or "Blue"
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val playerId: Int = 0,
    val playerName: String,
    val isActive: Boolean = true
)

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val scoreId: Int = 0,
    val gameId: Int,
    val playerId: Int,
    val score: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "round_scores",
    primaryKeys = ["gameId", "round", "playerId"]
)
data class RoundScoreEntity(
    val gameId: Int,
    val round: Int,
    val playerId: Int,
    val scoreIncrement: Int,
    val wasStartingPlayer: Boolean = false,
    val rawWingspanValue: Int? = null // Rank (1-4) or Item Count
)

@Entity(
    tableName = "category_scores",
    primaryKeys = ["gameId", "playerId", "categoryName"]
)
data class CategoryScoreEntity(
    val gameId: Int,
    val playerId: Int,
    val categoryName: String,
    val scoreValue: Int
)

data class PlayerScore(
    val playerId: Int,
    val playerName: String,
    val score: Int
)
