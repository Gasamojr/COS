package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.NotificationDao
import com.example.data.dao.ServiceOrderDao
import com.example.data.dao.StageHistoryDao
import com.example.data.model.AppNotification
import com.example.data.model.ServiceOrder
import com.example.data.model.StageHistory

@Database(
    entities = [
        ServiceOrder::class,
        StageHistory::class,
        AppNotification::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceOrderDao(): ServiceOrderDao
    abstract fun stageHistoryDao(): StageHistoryDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "controle_os_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
