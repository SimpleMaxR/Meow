package com.hugo.imagepreviewer.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadRecordEntity::class, LocalImageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadRecordDao(): DownloadRecordDao
    abstract fun localImageDao(): LocalImageDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { instance = it }
            }
    }
}