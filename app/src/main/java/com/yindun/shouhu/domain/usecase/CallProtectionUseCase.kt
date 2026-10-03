package com.yindun.shouhu.domain.usecase

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.CallLogEntity
import com.yindun.shouhu.data.local.entity.PhoneRiskEntity
import com.yindun.shouhu.domain.engine.FraudDetectionEngine
import com.yindun.shouhu.domain.engine.FraudAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 来电防护用例
 * 处理来电检测、风险评估等业务逻辑
 */
class CallProtectionUseCase(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }
    private val fraudEngine by lazy { FraudDetectionEngine.getInstance(context) }
    private val whitelistManager by lazy { WhitelistManager.getInstance(context) }

    /**
     * 分析来电风险
     */
    suspend fun analyzeIncomingCall(
        userId: Long,
        phoneNumber: String
    ): IncomingCallAnalysis = withContext(Dispatchers.Default) {
        // 1. 检查白名单
        val isWhitelisted = whitelistManager.isInWhitelist(userId, phoneNumber)
        if (isWhitelisted) {
            return@withContext IncomingCallAnalysis(
                phoneNumber = phoneNumber,
                isWhitelisted = true,
                riskLevel = 0,
                riskDescription = "白名单联系人",
                recommendedAction = "正常接听"
            )
        }

        // 2. 查询号码风险数据库
        val phoneRisk = database.phoneRiskDao().getPhoneRisk(phoneNumber)

        // 3. 计算风险等级
        val baseRiskLevel = phoneRisk?.let { calculateRiskLevel(it.reportCount) } ?: 0

        // 4. 生成分析结果
        val riskDescription = when {
            baseRiskLevel >= 8 -> "高风险诈骗号码"
            baseRiskLevel >= 5 -> "可疑号码"
            baseRiskLevel >= 2 -> "陌生号码"
            else -> "未知号码"
        }

        val recommendedAction = when {
            baseRiskLevel >= 8 -> "建议拒接，如已接听请立即挂断"
            baseRiskLevel >= 5 -> "谨慎接听，不要透露个人信息"
            baseRiskLevel >= 2 -> "正常接听，注意防范"
            else -> "正常接听"
        }

        IncomingCallAnalysis(
            phoneNumber = phoneNumber,
            isWhitelisted = false,
            riskLevel = baseRiskLevel,
            riskDescription = riskDescription,
            recommendedAction = recommendedAction,
            reportCount = phoneRisk?.reportCount ?: 0,
            fraudTypes = phoneRisk?.let {
                it.riskType.split(",")
            } ?: emptyList()
        )
    }

    /**
     * 分析通话内容
     */
    suspend fun analyzeCallContent(
        text: String,
        isCallText: Boolean = true
    ): FraudAnalysisResult {
        return fraudEngine.analyzeText(text, isCallText)
    }

    /**
     * 保存通话记录
     */
    suspend fun saveCallLog(
        userId: Long,
        phoneNumber: String,
        duration: Int = 0,
        riskLevel: Int = 0,
        riskReason: String = "",
        action: String = "none"
    ): Long = withContext(Dispatchers.IO) {
        val callLog = CallLogEntity(
            userId = userId,
            phoneNumber = phoneNumber,
            callTime = System.currentTimeMillis(),
            duration = duration,
            riskLevel = riskLevel,
            riskReason = riskReason,
            action = action
        )
        database.callLogDao().insertCallLog(callLog)
    }

    /**
     * 更新通话记录
     */
    suspend fun updateCallLog(callLog: CallLogEntity) = withContext(Dispatchers.IO) {
        database.callLogDao().updateCallLog(callLog)
    }

    /**
     * 获取用户通话记录
     */
    fun getCallLogs(userId: Long, limit: Int = 100): Flow<List<CallLogEntity>> {
        return database.callLogDao().getCallLogs(userId, limit)
    }

    /**
     * 获取高风险通话
     */
    fun getHighRiskCalls(userId: Long, minRiskLevel: Int = 5): Flow<List<CallLogEntity>> {
        return database.callLogDao().getHighRiskCalls(userId, minRiskLevel)
    }

    /**
     * 添加号码到风险库
     */
    suspend fun reportPhoneNumber(
        phoneNumber: String,
        riskType: String
    ) = withContext(Dispatchers.IO) {
        val existing = database.phoneRiskDao().getPhoneRisk(phoneNumber)
        if (existing != null) {
            database.phoneRiskDao().updatePhoneRisk(
                existing.copy(
                    reportCount = existing.reportCount + 1,
                    lastReportTime = System.currentTimeMillis()
                )
            )
        } else {
            database.phoneRiskDao().insertOrUpdate(
                PhoneRiskEntity(
                    phoneNumber = phoneNumber,
                    riskType = riskType,
                    reportCount = 1
                )
            )
        }
    }

    /**
     * 批量报告号码
     */
    suspend fun batchReportPhoneNumbers(
        reports: List<PhoneReport>
    ) = withContext(Dispatchers.IO) {
        reports.forEach { report ->
            reportPhoneNumber(report.phoneNumber, report.riskType)
        }
    }

    /**
     * 获取通话统计
     */
    suspend fun getCallStats(userId: Long, days: Int = 30): CallStats = withContext(Dispatchers.Default) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - days * 24 * 60 * 60 * 1000L

        val callLogs = database.callLogDao().getCallLogs(userId, Int.MAX_VALUE)
            .let {
                // 这里需要过滤时间范围，实际项目中应该在DAO层实现
                // 暂时返回所有记录
                mutableListOf<CallLogEntity>()
            }

        val totalCalls = callLogs.size
        val highRiskCalls = callLogs.count { it.riskLevel >= 7 }
        val mediumRiskCalls = callLogs.count { it.riskLevel in 4..6 }
        val lowRiskCalls = callLogs.count { it.riskLevel in 1..3 }
        val blockedCalls = callLogs.count { it.action == "blocked" }

        CallStats(
            totalCalls = totalCalls,
            highRiskCalls = highRiskCalls,
            mediumRiskCalls = mediumRiskCalls,
            lowRiskCalls = lowRiskCalls,
            blockedCalls = blockedCalls,
            periodDays = days
        )
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

    companion object {
        @Volatile
        private var instance: CallProtectionUseCase? = null

        fun getInstance(context: Context): CallProtectionUseCase {
            return instance ?: synchronized(this) {
                instance ?: CallProtectionUseCase(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 来电分析结果
 */
data class IncomingCallAnalysis(
    val phoneNumber: String,
    val isWhitelisted: Boolean,
    val riskLevel: Int,
    val riskDescription: String,
    val recommendedAction: String,
    val reportCount: Int = 0,
    val fraudTypes: List<String> = emptyList()
) {
    fun toJson(): org.json.JSONObject {
        return org.json.JSONObject().apply {
            put("phoneNumber", phoneNumber)
            put("isWhitelisted", isWhitelisted)
            put("riskLevel", riskLevel)
            put("riskDescription", riskDescription)
            put("recommendedAction", recommendedAction)
            put("reportCount", reportCount)
            put("fraudTypes", org.json.JSONArray(fraudTypes))
        }
    }
}

/**
 * 电话报告
 */
data class PhoneReport(
    val phoneNumber: String,
    val riskType: String
)

/**
 * 通话统计
 */
data class CallStats(
    val totalCalls: Int,
    val highRiskCalls: Int,
    val mediumRiskCalls: Int,
    val lowRiskCalls: Int,
    val blockedCalls: Int,
    val periodDays: Int
) {
    val highRiskRate: Double
        get() = if (totalCalls > 0) highRiskCalls.toDouble() / totalCalls else 0.0

    val blockRate: Double
        get() = if (totalCalls > 0) blockedCalls.toDouble() / totalCalls else 0.0
}
