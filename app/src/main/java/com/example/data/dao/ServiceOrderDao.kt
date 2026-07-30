package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ServiceOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceOrderDao {
    @Query("SELECT * FROM service_orders ORDER BY osNumber DESC")
    fun getAllOrders(): Flow<List<ServiceOrder>>

    @Query("SELECT * FROM service_orders WHERE osNumber = :osNumber LIMIT 1")
    suspend fun getOrderByNumber(osNumber: String): ServiceOrder?

    @Query("SELECT * FROM service_orders WHERE osNumber = :osNumber LIMIT 1")
    fun getOrderFlowByNumber(osNumber: String): Flow<ServiceOrder?>

    @Query("SELECT * FROM service_orders WHERE status = :status ORDER BY osNumber DESC")
    fun getOrdersByStatus(status: String): Flow<List<ServiceOrder>>

    @Query("""
        SELECT * FROM service_orders 
        WHERE osNumber LIKE '%' || :query || '%' 
        OR clientName LIKE '%' || :query || '%' 
        OR serviceDescription LIKE '%' || :query || '%'
        ORDER BY osNumber DESC
    """)
    fun searchOrders(query: String): Flow<List<ServiceOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: ServiceOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<ServiceOrder>)

    @Update
    suspend fun updateOrder(order: ServiceOrder)

    @Query("DELETE FROM service_orders WHERE osNumber = :osNumber")
    suspend fun deleteOrder(osNumber: String)

    @Query("DELETE FROM service_orders")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM service_orders")
    suspend fun countOrders(): Int
}
