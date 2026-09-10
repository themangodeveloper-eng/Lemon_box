package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "bash" or "python"
    val code: String,
    val description: String,
    val isFavorite: Boolean = false,
    val lastRunTimestamp: Long = 0L
)

@Entity(tableName = "command_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
