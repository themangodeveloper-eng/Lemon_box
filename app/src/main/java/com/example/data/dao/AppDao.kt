package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.HistoryEntity
import com.example.data.entity.ScriptEntity
import com.example.data.entity.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM scripts ORDER BY isFavorite DESC, id ASC")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity): Long

    @Update
    suspend fun updateScript(script: ScriptEntity)

    @Delete
    suspend fun deleteScript(script: ScriptEntity)

    @Query("SELECT * FROM scripts WHERE id = :id LIMIT 1")
    suspend fun getScriptById(id: Long): ScriptEntity?

    @Query("UPDATE scripts SET lastRunTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastRun(id: Long, timestamp: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM command_history ORDER BY id DESC LIMIT 200")
    fun getRecentHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT command FROM command_history ORDER BY id ASC")
    suspend fun getAllCommands(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(history: HistoryEntity): Long

    @Query("DELETE FROM command_history")
    suspend fun clearHistory()
}

@Dao
interface SettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<SettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)
}
