package com.chumakov123.casedocket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chumakov123.casedocket.data.local.dao.RecognitionTaskDao
import com.chumakov123.casedocket.data.local.entity.RecognitionTaskEntity

@Database(
    entities = [RecognitionTaskEntity::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun recognitionTaskDao(): RecognitionTaskDao

    companion object {
        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
        }
    }
}