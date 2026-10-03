package com.yindun.shouhu.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.yindun.shouhu.MainActivity
import com.yindun.shouhu.R
import com.yindun.shouhu.YinDunApplication
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.CallLogEntity
import com.yindun.shouhu.domain.engine.FraudDetectionEngine
import com.yindun.shouhu.util.SpeechManager
import kotlinx.coroutines.*

/**
 * 电话状态监听服务
 * 监听来电状态，检测诈骗电话
 */
class PhoneStateService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val fraudEngine by lazy { FraudDetectionEngine.getInstance(this) }

    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var outgoingCallReceiver: BroadcastReceiver? = null

    private var currentUserId: Long = -1
    private var currentPhoneNumber: String? = null
    private var callStartTime: Long = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification("来电防护已启动"))
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentUserId = intent.getLongExtra(EXTRA_USER_ID, -1)
                startListening()
            }
            ACTION_STOP -> stopListening()
            ACTION_BLOCK_CALL -> blockCurrentCall()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
        serviceScope.cancel()
    }

    /**
     * 开始监听电话状态
     */
    private fun startListening() {
        // 注册电话状态监听
        @Suppress("DEPRECATION")
        phoneStateListener = object : PhoneStateListener() {
            @Deprecated("Deprecated in Java")
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                handleCallStateChanged(state, phoneNumber)
            }
        }

        @Suppress("DEPRECATION")
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        // 注册外拨电话广播
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_NEW_OUTGOING_CALL)
        }
        outgoingCallReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val number = intent?.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                if (number != null) {
                    checkOutgoingCall(number)
                }
            }
        }
        registerReceiver(outgoingCallReceiver, filter)

        updateNotification("正在监听来电...")
    }

    /**
     * 停止监听
     */
    private fun stopListening() {
        @Suppress("DEPRECATION")
        phoneStateListener?.let {
            telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE)
        }
        phoneStateListener = null

        outgoingCallReceiver?.let {
            unregisterReceiver(it)
        }
        outgoingCallReceiver = null
    }

    /**
     * 处理电话状态变化
     */
    private fun handleCallStateChanged(state: Int, phoneNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                // 来电响铃
                phoneNumber?.let {
                    currentPhoneNumber = it
                    onIncomingCall(it)
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // 通话中
                callStartTime = System.currentTimeMillis()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // 通话结束
                if (callStartTime > 0 && currentPhoneNumber != null) {
                    val duration = ((System.currentTimeMillis() - callStartTime) / 1000).toInt()
                    onCallEnded(currentPhoneNumber!!, duration)
                    currentPhoneNumber = null
                    callStartTime = 0
                }
            }
        }
    }

    /**
     * 处理来电
     */
    private fun onIncomingCall(phoneNumber: String) {
        serviceScope.launch {
            try {
                // 检查是否在白名单
                val isWhitelisted = database.whitelistDao()
                    .isInWhitelist(currentUserId, phoneNumber) != null

                if (isWhitelisted) {
                    sendNotification(
                        "来电提醒",
                        "白名单联系人来电：$phoneNumber",
                        isHighPriority = false
                    )
                    return@launch
                }

                // 查询号码风险
                val phoneRisk = database.phoneRiskDao().getPhoneRisk(phoneNumber)
                val riskLevel = phoneRisk?.let { calculateRiskLevel(it.reportCount) } ?: 0

                // 根据风险等级处理
                when {
                    riskLevel >= 8 -> {
                        // 高风险：立即警告
                        sendRiskAlert(
                            phoneNumber,
                            riskLevel,
                            "高风险诈骗电话",
                            "该号码被多人举报为诈骗电话，请勿接听！"
                        )
                        // 播放语音警告
                        playVoiceWarning("警告！该号码疑似诈骗电话，请勿透露个人信息！")
                    }
                    riskLevel >= 5 -> {
                        // 中风险：弹窗提醒
                        sendRiskAlert(
                            phoneNumber,
                            riskLevel,
                            "可疑来电提醒",
                            "该号码存在风险，请注意防范"
                        )
                    }
                    riskLevel >= 2 -> {
                        // 低风险：轻微提醒
                        sendNotification(
                            "陌生来电",
                            "检测到陌生号码：$phoneNumber，请注意防范",
                            isHighPriority = false
                        )
                    }
                }

                // 记录来电日志
                saveCallLog(phoneNumber, riskLevel)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 处理通话结束
     */
    private fun onCallEnded(phoneNumber: String, duration: Int) {
        serviceScope.launch {
            try {
                // 更新通话日志
                val existingLog = database.callLogDao().getLastCallByNumber(phoneNumber)
                if (existingLog != null) {
                    database.callLogDao().updateCallLog(
                        existingLog.copy(duration = duration)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 处理外拨电话
     */
    private fun checkOutgoingCall(phoneNumber: String) {
        serviceScope.launch {
            try {
                // 检查外拨电话号码是否是诈骗号码
                val phoneRisk = database.phoneRiskDao().getPhoneRisk(phoneNumber)
                if (phoneRisk != null && phoneRisk.reportCount >= 10) {
                    sendRiskAlert(
                        phoneNumber,
                        8,
                        "警告：您正在拨打诈骗号码",
                        "该号码被多人举报为诈骗电话，请确认是否继续拨打！"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 保存通话记录
     */
    private suspend fun saveCallLog(phoneNumber: String, riskLevel: Int) {
        try {
            val callLog = CallLogEntity(
                userId = currentUserId,
                phoneNumber = phoneNumber,
                callTime = System.currentTimeMillis(),
                duration = 0,
                riskLevel = riskLevel,
                riskReason = if (riskLevel > 0) "系统检测" else "",
                isWhitelisted = false
            )
            database.callLogDao().insertCallLog(callLog)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 计算风险等级
     */
    private fun calculateRiskLevel(reportCount: Int): Int {
        return when {
            reportCount >= 100 -> 10
            reportCount >= 50 -> 9
            reportCount >= 20 -> 8
            reportCount >= 10 -> 7
            reportCount >= 5 -> 6
            reportCount >= 3 -> 5
            reportCount >= 2 -> 4
            reportCount >= 1 -> 3
            else -> 0
        }
    }

    /**
     * 发送风险警报
     */
    private fun sendRiskAlert(
        phoneNumber: String,
        riskLevel: Int,
        title: String,
        message: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_call_alert", true)
            putExtra("phone_number", phoneNumber)
            putExtra("risk_level", riskLevel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            phoneNumber.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, YinDunApplication.RISK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()

        notificationManager.notify(NOTIFICATION_ID + phoneNumber.hashCode(), notification)
    }

    /**
     * 发送普通通知
     */
    private fun sendNotification(title: String, message: String, isHighPriority: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, YinDunApplication.CASE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                if (isHighPriority) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }

    /**
     * 播放语音警告
     */
    private fun playVoiceWarning(message: String) {
        try {
            val speechManager = YinDunApplication.speechManager
            speechManager.speak(message, "fraud_warning")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 阻断当前通话
     */
    private fun blockCurrentCall() {
        try {
            // 通过反射调用endCall (需要系统权限或特殊处理)
            // 实际实现需要根据Android版本适配
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            // 这里需要特殊权限，实际项目中需要使用其他方式
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            .setContentTitle("银盾守护 - 来电防护")
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
        const val NOTIFICATION_ID = 3001
        const val ACTION_START = "com.yindun.shouhu.PHONE_START"
        const val ACTION_STOP = "com.yindun.shouhu.PHONE_STOP"
        const val ACTION_BLOCK_CALL = "com.yindun.shouhu.BLOCK_CALL"
        const val EXTRA_USER_ID = "user_id"

        fun start(context: Context, userId: Long) {
            val intent = Intent(context, PhoneStateService::class.java).apply {
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
            val intent = Intent(context, PhoneStateService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun blockCall(context: Context) {
            val intent = Intent(context, PhoneStateService::class.java).apply {
                action = ACTION_BLOCK_CALL
            }
            context.startService(intent)
        }
    }
}
