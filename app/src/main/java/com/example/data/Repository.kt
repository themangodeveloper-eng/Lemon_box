package com.example.data

import com.example.data.dao.HistoryDao
import com.example.data.dao.ScriptDao
import com.example.data.dao.SettingDao
import com.example.data.entity.HistoryEntity
import com.example.data.entity.ScriptEntity
import com.example.data.entity.SettingEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val scriptDao: ScriptDao,
    private val historyDao: HistoryDao,
    private val settingDao: SettingDao
) {
    val allScripts: Flow<List<ScriptEntity>> = scriptDao.getAllScripts()
    val recentHistory: Flow<List<HistoryEntity>> = historyDao.getRecentHistory()
    val allSettings: Flow<List<SettingEntity>> = settingDao.getAllSettings()

    suspend fun saveScript(script: ScriptEntity): Long = scriptDao.insertScript(script)
    suspend fun updateScript(script: ScriptEntity) = scriptDao.updateScript(script)
    suspend fun deleteScript(script: ScriptEntity) = scriptDao.deleteScript(script)
    suspend fun recordScriptRun(id: Long) = scriptDao.updateLastRun(id, System.currentTimeMillis())

    suspend fun addHistory(command: String) {
        if (command.isNotBlank()) {
            historyDao.insertCommand(HistoryEntity(command = command.trim()))
        }
    }
    suspend fun getAllHistoryCommands(): List<String> = historyDao.getAllCommands()
    suspend fun clearHistory() = historyDao.clearHistory()

    suspend fun getSetting(key: String): String? = settingDao.getSetting(key)
    suspend fun setSetting(key: String, value: String) = settingDao.setSetting(SettingEntity(key, value))
}
