package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = TYPE_INFO, // STAGE_CHANGE, WARNING_DEADLINE, OVERDUE, IMPORT_RESULT
    val osNumber: String? = null,
    val isRead: Boolean = false
) {
    companion object {
        const val TYPE_STAGE_CHANGE = "STAGE_CHANGE"
        const val TYPE_WARNING_DEADLINE = "WARNING_DEADLINE"
        const val TYPE_OVERDUE = "OVERDUE"
        const val TYPE_IMPORT_RESULT = "IMPORT_RESULT"
        const val TYPE_INFO = "INFO"
    }
}
