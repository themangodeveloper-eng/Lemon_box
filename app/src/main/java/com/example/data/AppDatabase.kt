package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.HistoryDao
import com.example.data.dao.ScriptDao
import com.example.data.dao.SettingDao
import com.example.data.entity.HistoryEntity
import com.example.data.entity.ScriptEntity
import com.example.data.entity.SettingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ScriptEntity::class, HistoryEntity::class, SettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao
    abstract fun historyDao(): HistoryDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lemon_box_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            seedInitialData(INSTANCE)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase?) {
            val scriptDao = database?.scriptDao() ?: return
            val sampleScripts = listOf(
                ScriptEntity(
                    name = "quick_dev_build.sh",
                    type = "bash",
                    code = """
# Quick Dev Build & Diagnostic Script
echo "=== [lemon_box] Dev Pipeline Starting ==="
javac HelloWorld.java
java HelloWorld
git status
free -m
echo "=== Build & Health Check Finished ==="
                    """.trimIndent(),
                    description = "Compiles sample Java code, tests execution, checks git status and memory",
                    isFavorite = true
                ),
                ScriptEntity(
                    name = "git_auto_sync.sh",
                    type = "bash",
                    code = """
# Automated Git Stage and Commit
echo "Syncing repository..."
git add .
git commit -m "Automated dev sync $(date)"
git log -n 3 --oneline
echo "Repo state verified."
                    """.trimIndent(),
                    description = "Stages all changes, creates timestamped commit and prints top log",
                    isFavorite = true
                ),
                ScriptEntity(
                    name = "android_sdk_check.sh",
                    type = "bash",
                    code = """
# Android SDK & NDK Toolchain Inspector
echo "[SDK] Checking Android SDK components..."
sdkmanager --list
echo "[ADB] Checking ADB connectivity..."
adb devices
echo "[NDK] Testing C++ cross compiler ABI..."
ndk-build
                    """.trimIndent(),
                    description = "Inspects Android SDK components, ADB devices, and NDK toolchains",
                    isFavorite = false
                ),
                ScriptEntity(
                    name = "sys_health_bench.py",
                    type = "python",
                    code = """
# Python System Benchmark & Diagnostics
import math
import sys
import time

print(f"lemon_box Python Engine: {sys.version}")
print("Running CPU arithmetic benchmark...")
primes = [p for p in range(2, 200) if all(p % d != 0 for d in range(2, int(math.isqrt(p)) + 1))]
print(f"Discovered {len(primes)} primes up to 200: {primes[:10]}...")
print("Benchmark completed with 0 errors.")
                    """.trimIndent(),
                    description = "Runs arithmetic CPU benchmark and reports Python runtime version",
                    isFavorite = true
                )
            )

            for (script in sampleScripts) {
                scriptDao.insertScript(script)
            }
        }
    }
}
