# Complete Source Code Reference

This document contains all source code files ready for the Android Board Game Score Tracker app.

## 📋 Files to Create (After Running setup_complete.py)

### 1. Database Layer
**File**: `app/src/main/java/com/example/boardgamescoretracker/data/db/Entities.kt`
```kotlin
package com.example.boardgamescoretracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val gameId: Int = 0,
    val gameName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null
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
```

### 2. Database Access Objects (DAOs)
**File**: `app/src/main/java/com/example/boardgamescoretracker/data/db/Daos.kt`
```kotlin
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
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("SELECT * FROM games WHERE finishedAt IS NULL ORDER BY createdAt DESC LIMIT 1")
    fun getCurrentGame(): Flow<GameEntity?>

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("UPDATE games SET finishedAt = :finishedAt WHERE gameId = :gameId")
    suspend fun finishGame(gameId: Int, finishedAt: Long)
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
}
```

### 3. Room Database Configuration
**File**: `app/src/main/java/com/example/boardgamescoretracker/data/db/ScoreTrackerDatabase.kt`
```kotlin
package com.example.boardgamescoretracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameEntity::class, PlayerEntity::class, ScoreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScoreTrackerDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var instance: ScoreTrackerDatabase? = null

        fun getInstance(context: Context): ScoreTrackerDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ScoreTrackerDatabase::class.java,
                    "score_tracker.db"
                ).build().also { instance = it }
            }
        }
    }
}
```

### 4. Data Repositories
**File**: `app/src/main/java/com/example/boardgamescoretracker/data/repository/Repositories.kt`
[View Repositories.kt in main response]

### 5. ViewModels
**File**: `app/src/main/java/com/example/boardgamescoretracker/viewmodel/ViewModels.kt`
[View ViewModels.kt in main response]

### 6. UI Theme
**File**: `app/src/main/java/com/example/boardgamescoretracker/ui/theme/Theme.kt`
[View Theme.kt in main response]

### 7. Compose Screens
**File**: `app/src/main/java/com/example/boardgamescoretracker/ui/screens/Screens.kt`
[View Screens.kt in main response]

### 8. MainActivity
**File**: `app/src/main/java/com/example/boardgamescoretracker/MainActivity.kt`
[View MainActivity.kt in main response]

## 🚀 Installation Steps

1. **Run Setup**:
   ```bash
   cd H:\Projects\agents-android-app-development
   python setup_complete.py
   ```

2. **Create Source Files**: Copy each source code section above to the corresponding file

3. **Build Project**:
   ```bash
   ./gradlew build
   ```

4. **Run on Emulator**:
   ```bash
   ./gradlew installDebug
   ```

## ✅ Checklist

- [ ] Run setup_complete.py
- [ ] Create all source files (8 files total)
- [ ] Verify build.gradle.kts is in app/ directory
- [ ] Run ./gradlew build
- [ ] Test on emulator
- [ ] Track board game scores!
