package com.yindun.shouhu

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.yindun.shouhu.di.AppModule
import com.yindun.shouhu.domain.engine.FraudDetectionEngine
import com.yindun.shouhu.domain.engine.TransactionAnomalyEngine
import com.yindun.shouhu.service.SyncWorker
import com.yindun.shouhu.data.local.PreloadedDataManager
import com.yindun.shouhu.util.AppStartupManager
import com.yindun.shouhu.util.CryptoManager
import com.yindun.shouhu.util.SpeechManager
import com.yindun.shouhu.util.SyncManager
import com.yindun.shouhu.util.VoiceResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YinDunApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化各模块
        initNotificationChannels()
        initAIEngines()
        initSyncService()
        initAppStartup()
    }

    private fun initAIEngines() {
        // 初始化加密管理器
        cryptoManager = CryptoManager.getInstance(this)

        // 初始化语音管理器
        speechManager = SpeechManager.getInstance(this)

        // 初始化AI引擎（懒加载）
        fraudDetectionEngine = FraudDetectionEngine.getInstance(this)
        transactionAnomalyEngine = TransactionAnomalyEngine.getInstance(this)
    }

    private fun initSyncService() {
        // 启动定期同步任务
        SyncWorker.schedulePeriodicSync(this)

        // 立即执行一次同步
        SyncWorker.syncNow(this)
    }

    private fun initAppStartupManager() {
        startupManager = AppStartupManager.getInstance(this)
        applicationScope.launch {
            startupManager.initialize()
        }
    }

    private fun initAppStartup() {
        // 初始化应用启动管理器
        startupManager = AppStartupManager.getInstance(this)

        // 初始化语音资源管理器
        voiceResourceManager = VoiceResourceManager.getInstance(this)

        // 预加载所有数据到本地
        preloadedDataManager = PreloadedDataManager.getInstance(this)
        applicationScope.launch {
            startupManager.initialize()
            preloadedDataManager.preloadAllData()
        }
    }

    private fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // 风险提醒通道
            val riskChannel = NotificationChannel(
                RISK_CHANNEL_ID,
                "风险提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "财务风险和诈骗预警"
                enableVibration(true)
            }

            // 案件进度通道
            val caseChannel = NotificationChannel(
                CASE_CHANNEL_ID,
                "案件进度",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "报案案件进度更新"
            }

            // 训练通知通道
            val trainingChannel = NotificationChannel(
                TRAINING_CHANNEL_ID,
                "训练提醒",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "防骗训练提醒"
            }

            notificationManager.createNotificationChannels(
                listOf(riskChannel, caseChannel, trainingChannel)
            )
        }
    }

    companion object {
        const val RISK_CHANNEL_ID = "risk_alert"
        const val CASE_CHANNEL_ID = "case_progress"
        const val TRAINING_CHANNEL_ID = "training_reminder"

        lateinit var instance: YinDunApplication
            private set

        // 工具类实例
        lateinit var cryptoManager: CryptoManager
            private set

        lateinit var speechManager: SpeechManager
            private set

        // AI引擎实例
        lateinit var fraudDetectionEngine: FraudDetectionEngine
            private set

        lateinit var transactionAnomalyEngine: TransactionAnomalyEngine
            private set

        // 启动管理器
        lateinit var startupManager: AppStartupManager
            private set

        // 语音资源管理器
        lateinit var voiceResourceManager: VoiceResourceManager
            private set

        // 预加载数据管理器
        lateinit var preloadedDataManager: PreloadedDataManager
            private set
    }
}
