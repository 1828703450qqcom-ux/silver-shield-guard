package com.yindun.shouhu.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yindun.shouhu.MainActivity
import com.yindun.shouhu.R
import com.yindun.shouhu.YinDunApplication
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.TransactionEntity
import com.yindun.shouhu.domain.engine.TransactionAnalysisResult
import com.yindun.shouhu.domain.engine.TransactionAnomalyEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * 财务监控服务
 * 实时监控交易异常并发送预警
 */
class FinancialMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val anomalyEngine by lazy { TransactionAnomalyEngine.getInstance(this) }

    private var monitoringJob: Job? = null
    private var currentUserId: Long = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification("财务监控已启动"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentUserId = intent.getLongExtra(EXTRA_USER_ID, -1)
                startMonitoring()
            }
            ACTION_STOP -> stopMonitoring()
            ACTION_CHECK_NOW -> serviceScope.launch { checkTransactionsNow() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
    }

    /**
     * 开始监控
     */
    private fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkTransactionsNow()
                    delay(CHECK_INTERVAL) // 每30秒检查一次
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(60_000) // 出错后等待1分钟再重试
                }
            }
        }

        updateNotification("正在监控交易安全...")
    }

    /**
     * 停止监控
     */
    private fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        updateNotification("监控已暂停")
    }

    /**
     * 立即检查交易
     */
    private suspend fun checkTransactionsNow() {
        if (currentUserId == -1L) return

        try {
            // 获取最近5分钟的交易
            val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000
            val recentTransactions = database.transactionDao()
                .getTransactionsByDateRange(currentUserId, fiveMinutesAgo, System.currentTimeMillis())

            // 分析每笔交易
            for (transaction in recentTransactions) {
                if (!transaction.isSuspicious) {
                    analyzeTransaction(transaction)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 分析单笔交易
     */
    private suspend fun analyzeTransaction(transaction: TransactionEntity) {
        try {
            // 获取该账户的近期交易用于上下文分析
            val recentTransactions = database.transactionDao()
                .getRecentTransactions(currentUserId, 50)
                .first()

            // 使用异常检测引擎分析
            val result = anomalyEngine.analyzeTransaction(transaction, recentTransactions)

            // 如果检测到异常，更新交易记录并发送通知
            if (result.isAnomaly) {
                // 更新交易标记
                val updatedTransaction = transaction.copy(
                    isSuspicious = true,
                    riskScore = (result.riskScore * 100).toInt()
                )
                database.transactionDao().updateTransaction(updatedTransaction)

                // 发送风险通知
                sendRiskNotification(transaction, result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 发送风险通知
     */
    private fun sendRiskNotification(transaction: TransactionEntity, result: TransactionAnalysisResult) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_financial_alert", true)
            putExtra("transaction_id", transaction.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            transaction.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            result.riskLevel >= 8 -> "高风险交易警报"
            result.riskLevel >= 6 -> "异常交易提醒"
            else -> "交易风险提示"
        }

        val message = "检测到${result.anomalyType}，金额¥${transaction.amount}，对手方：${transaction.counterparty}"

        val notification = NotificationCompat.Builder(this, YinDunApplication.RISK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (result.riskLevel >= 8) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID + transaction.id.toInt(), notification)
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, YinDunApplication.RISK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("银盾守护 - 财务监控")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.yindun.shouhu.FINANCIAL_START"
        const val ACTION_STOP = "com.yindun.shouhu.FINANCIAL_STOP"
        const val ACTION_CHECK_NOW = "com.yindun.shouhu.FINANCIAL_CHECK_NOW"
        const val EXTRA_USER_ID = "user_id"
        const val CHECK_INTERVAL = 30_000L // 30秒

        fun start(context: Context, userId: Long) {
            val intent = Intent(context, FinancialMonitorService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_USER_ID, userId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FinancialMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun checkNow(context: Context) {
            val intent = Intent(context, FinancialMonitorService::class.java).apply {
                action = ACTION_CHECK_NOW
            }
            context.startService(intent)
        }
    }
}
