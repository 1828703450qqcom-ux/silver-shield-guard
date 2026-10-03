package com.yindun.shouhu.util

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.DatabaseInitializer
import com.yindun.shouhu.util.ai.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 应用启动管理器
 * 负责初始化所有组件
 */
class AppStartupManager(private val context: Context) {

    private val databaseInitializer by lazy { DatabaseInitializer.getInstance(context) }
    private val modelManager by lazy { ModelManager(context) }
    private val syncManager by lazy { SyncManager.getInstance(context) }

    /**
     * 初始化应用
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("StartupManager", "开始初始化应用...")

            // 1. 初始化数据库
            android.util.Log.d("StartupManager", "初始化数据库...")
            databaseInitializer.initialize()

            // 2. 初始化AI模型
            android.util.Log.d("StartupManager", "初始化AI模型...")
            modelManager.initializeModels()

            // 3. 同步话术库（如果网络可用）
            android.util.Log.d("StartupManager", "同步话术库...")
            if (syncManager.isNetworkAvailable()) {
                try {
                    syncManager.syncFraudScripts(0)
                } catch (e: Exception) {
                    android.util.Log.w("StartupManager", "话术库同步失败: ${e.message}")
                }
            }

            android.util.Log.d("StartupManager", "应用初始化完成")
        } catch (e: Exception) {
            android.util.Log.e("StartupManager", "应用初始化失败", e)
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        modelManager.release()
    }

    companion object {
        @Volatile
        private var instance: AppStartupManager? = null

        fun getInstance(context: Context): AppStartupManager {
            return instance ?: synchronized(this) {
                instance ?: AppStartupManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
