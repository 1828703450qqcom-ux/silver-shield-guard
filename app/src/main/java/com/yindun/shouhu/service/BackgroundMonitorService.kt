package com.yindun.shouhu.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yindun.shouhu.MainActivity
import com.yindun.shouhu.R
import com.yindun.shouhu.YinDunApplication
import com.yindun.shouhu.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BackgroundMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isMonitoring = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification("银盾守护正在运行"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
            ACTION_RISK_WARNING -> {
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                val riskLevel = intent.getIntExtra(EXTRA_RISK_LEVEL, 0)
                handleRiskWarning(phoneNumber, riskLevel)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        updateNotification("正在监听陌生来电...")

        // 启动财务监控
        serviceScope.launch {
            startFinancialMonitoring()
        }
    }

    private fun stopMonitoring() {
        isMonitoring = false
        updateNotification("监控已暂停")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun startFinancialMonitoring() {
        // 财务监控逻辑
        // 定期检查交易记录，识别异常交易
        while (isMonitoring) {
            try {
                // TODO: 实现财务监控逻辑
                kotlinx.coroutines.delay(60_000) // 每分钟检查一次
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleRiskWarning(phoneNumber: String?, riskLevel: Int) {
        val message = when {
            riskLevel >= 9 -> "紧急警告：检测到高风险诈骗电话 $phoneNumber"
            riskLevel >= 7 -> "警告：疑似诈骗电话 $phoneNumber"
            riskLevel >= 5 -> "提醒：可疑来电 $phoneNumber"
            else -> "注意：陌生来电 $phoneNumber"
        }

        // 发送高优先级通知
        sendRiskNotification(phoneNumber ?: "未知号码", message, riskLevel)

        // 如果是高风险，播放语音警告
        if (riskLevel >= 7) {
            playVoiceWarning(riskLevel)
        }
    }

    private fun sendRiskNotification(phoneNumber: String, message: String, riskLevel: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("phone_number", phoneNumber)
            putExtra("risk_level", riskLevel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when {
            riskLevel >= 9 -> NotificationCompat.PRIORITY_MAX
            riskLevel >= 7 -> NotificationCompat.PRIORITY_HIGH
            riskLevel >= 5 -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(this, YinDunApplication.RISK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("风险提醒")
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID + riskLevel, notification)
    }

    private fun playVoiceWarning(riskLevel: Int) {
        // TODO: 实现语音警告播放
        // 使用TTS播放警告语音
    }

    private fun createNotification(contentText: String): Notification {
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
            .setContentTitle("银盾守护")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_MONITORING = "com.yindun.shouhu.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.yindun.shouhu.STOP_MONITORING"
        const val ACTION_RISK_WARNING = "com.yindun.shouhu.RISK_WARNING"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_RISK_LEVEL = "risk_level"

        fun start(context: android.content.Context) {
            val intent = Intent(context, BackgroundMonitorService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: android.content.Context) {
            val intent = Intent(context, BackgroundMonitorService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            context.startService(intent)
        }
    }
}
