package com.yindun.shouhu.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yindun.shouhu.MainActivity
import com.yindun.shouhu.R
import com.yindun.shouhu.YinDunApplication

/**
 * 通知管理器
 * 统一管理应用内各种通知
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * 发送风险警报通知
     */
    fun sendRiskAlert(
        title: String,
        message: String,
        riskLevel: Int,
        extras: Map<String, Any> = emptyMap()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extras.forEach { (key, value) ->
                when (value) {
                    is String -> putExtra(key, value)
                    is Int -> putExtra(key, value)
                    is Long -> putExtra(key, value)
                    is Boolean -> putExtra(key, value)
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when {
            riskLevel >= 8 -> NotificationCompat.PRIORITY_HIGH
            riskLevel >= 5 -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(context, YinDunApplication.RISK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(getVibrationPattern(riskLevel))
            .setLights(getLightColor(riskLevel), 500, 500)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    /**
     * 发送案件进度通知
     */
    fun sendCaseProgressNotification(
        caseNumber: String,
        status: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_case_progress", true)
            putExtra("case_number", caseNumber)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            caseNumber.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, YinDunApplication.CASE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("案件进度更新 - $caseNumber")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            CASE_NOTIFICATION_BASE + caseNumber.hashCode(),
            notification
        )
    }

    /**
     * 发送训练提醒通知
     */
    fun sendTrainingReminder(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_training", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, YinDunApplication.TRAINING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(TRAINING_NOTIFICATION_ID, notification)
    }

    /**
     * 发送系统通知
     */
    fun sendSystemNotification(
        title: String,
        message: String,
        channelId: String = YinDunApplication.CASE_CHANNEL_ID
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    /**
     * 取消指定通知
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * 获取振动模式
     */
    private fun getVibrationPattern(riskLevel: Int): LongArray {
        return when {
            riskLevel >= 8 -> longArrayOf(0, 1000, 500, 1000, 500, 1000) // 高风险：长振动
            riskLevel >= 5 -> longArrayOf(0, 500, 200, 500) // 中风险：中等振动
            else -> longArrayOf(0, 200) // 低风险：短振动
        }
    }

    /**
     * 获取灯光颜色
     */
    private fun getLightColor(riskLevel: Int): Int {
        return when {
            riskLevel >= 8 -> android.graphics.Color.RED
            riskLevel >= 5 -> android.graphics.Color.YELLOW
            else -> android.graphics.Color.GREEN
        }
    }

    companion object {
        private const val CASE_NOTIFICATION_BASE = 10000
        private const val TRAINING_NOTIFICATION_ID = 20000

        @Volatile
        private var instance: NotificationHelper? = null

        fun getInstance(context: Context): NotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: NotificationHelper(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
