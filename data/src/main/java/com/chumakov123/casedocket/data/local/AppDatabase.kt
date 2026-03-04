package com.chumakov123.casedocket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chumakov123.casedocket.data.local.dao.ConfirmedScheduleDao
import com.chumakov123.casedocket.data.local.dao.RecognitionTaskDao
import com.chumakov123.casedocket.data.local.entity.ConfirmedScheduleEntity
import com.chumakov123.casedocket.data.local.entity.RecognitionTaskEntity

@Database(
    entities = [RecognitionTaskEntity::class, ConfirmedScheduleEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recognitionTaskDao(): RecognitionTaskDao
    abstract fun confirmedScheduleDao(): ConfirmedScheduleDao

    companion object {
        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}