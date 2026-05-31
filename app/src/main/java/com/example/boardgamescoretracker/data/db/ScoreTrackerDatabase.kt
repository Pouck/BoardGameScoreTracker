package com.example.boardgamescoretracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameEntity::class, PlayerEntity::class, ScoreEntity::class, RoundScoreEntity::class, CategoryScoreEntity::class],
    version = 11,
    exportSchema = false
)
abstract class ScoreTrackerDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun scoreDao(): ScoreDao
    abstract fun categoryScoreDao(): CategoryScoreDao

    companion object {
        @Volatile
        private var instance: ScoreTrackerDatabase? = null

        fun getInstance(context: Context): ScoreTrackerDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ScoreTrackerDatabase::class.java,
                    "score_tracker.db"
                ).fallbackToDestructiveMigration(true)
                .build().also { instance = it }
            }
        }
    }
}
