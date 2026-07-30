package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StageHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface StageHistoryDao {
    @Query("SELECT * FROM stage_history WHERE osNumber = :osNumber ORDER BY timestamp DESC")
    fun getHistoryForOrder(osNumber: String): Flow<List<StageHistory>>

    @Query("SELECT * FROM stage_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllRecentHistory(): Flow<List<StageHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: StageHistory)

    @Query("DELETE FROM stage_history WHERE osNumber = :osNumber")
    suspend fun deleteHistoryForOrder(osNumber: String)
}
