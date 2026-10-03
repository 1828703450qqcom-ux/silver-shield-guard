package com.yindun.shouhu.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityEvent
import com.yindun.shouhu.di.AppModule
import com.yindun.shouhu.data.local.entity.CallLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallMonitoringService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentPhoneNumber: String? = null
    private var currentCallState = TelephonyManager.CALL_STATE_IDLE

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            handleAccessibilityEvent(it)
        }
    }

    override fun onInterrupt() {
        // 服务中断时的处理
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    private fun handleAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                // 处理通知事件（可能包含来电信息）
                handleNotificationEvent(event)
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // 处理窗口变化（来电界面）
                handleWindowChanged(event)
            }
        }
    }

    private fun handleNotificationEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val text = event.text?.joinToString("") ?: return

        // 检测是否是来电相关的通知
        if (packageName.contains("phone") || packageName.contains("dialer")) {
            // 提取电话号码
            val phoneNumber = extractPhoneNumber(text)
            if (phoneNumber != null) {
                checkPhoneNumberRisk(phoneNumber)
            }
        }
    }

    private fun handleWindowChanged(event: AccessibilityEvent) {
        val className = event.className?.toString() ?: return

        // 检测来电界面
        if (className.contains("InCallActivity") || className.contains("CallActivity")) {
            // 来电界面显示
            onIncomingCall()
        }
    }

    private fun extractPhoneNumber(text: String): String? {
        // 简单的电话号码提取逻辑
        val phonePattern = Regex("\\d{11}|\\d{3,4}-\\d{7,8}")
        return phonePattern.find(text)?.value
    }

    private fun onIncomingCall() {
        // 来电时的处理逻辑
        // 这里可以结合电话状态监听来获取更准确的信息
    }

    private fun checkPhoneNumberRisk(phoneNumber: String) {
        serviceScope.launch {
            try {
                val riskEntity = AppModule.callProtectionRepository.getPhoneRisk(phoneNumber)
                val riskLevel = riskEntity?.let {
                    calculateRiskLevel(it.reportCount)
                } ?: 0

                if (riskLevel >= 7) {
                    // 高风险，发送警告
                    sendRiskWarning(phoneNumber, riskLevel)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

    private fun sendRiskWarning(phoneNumber: String, riskLevel: Int) {
        // 发送风险警告通知
        val intent = Intent(this, BackgroundMonitorService::class.java).apply {
            action = "RISK_WARNING"
            putExtra("phone_number", phoneNumber)
            putExtra("risk_level", riskLevel)
        }
        startService(intent)
    }

    companion object {
        private var instance: CallMonitoringService? = null

        fun getInstance(): CallMonitoringService? = instance

        fun isRunning(): Boolean = instance != null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
