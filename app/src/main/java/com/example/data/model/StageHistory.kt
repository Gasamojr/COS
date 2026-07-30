package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stage_history")
data class StageHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val osNumber: String,
    val fromStage: String,
    val toStage: String,
    val user: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
