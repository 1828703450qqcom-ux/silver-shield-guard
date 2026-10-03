package com.yindun.shouhu.domain.engine

import android.content.Context
import com.yindun.shouhu.data.local.entity.TransactionEntity
import com.yindun.shouhu.util.ai.OnnxRuntimeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 交易异常检测引擎
 * 基于规则和机器学习检测异常交易行为
 */
class TransactionAnomalyEngine(private val context: Context) {

    private val onnxManager = OnnxRuntimeManager.getInstance(context)

    // 默认风险规则
    private val defaultRules = listOf(
        // 单笔大额转账
        RiskRule(
            name = "large_single_transfer",
            description = "单笔大额转账",
            threshold = 10000.0,
            riskLevel = 6
        ),
        // 短时间内多次小额转账
        RiskRule(
            name = "frequent_small_transfers",
            description = "短时间内多次小额转账",
            threshold = 5.0,  // 5次以上
            timeWindowMinutes = 30,
            riskLevel = 7
        ),
        // 非常规时间交易
        RiskRule(
            name = "unusual_time_transaction",
            description = "非常规时间交易（深夜）",
            startHour = 23,
            endHour = 5,
            riskLevel = 4
        ),
        // 大额转给陌生人
        RiskRule(
            name = "large_transfer_to_stranger",
            description = "大额转给陌生账户",
            threshold = 5000.0,
            riskLevel = 8
        ),
        // 异常高频交易
        RiskRule(
            name = "high_frequency_transaction",
            description = "异常高频交易",
            threshold = 20.0,  // 20次以上
            timeWindowMinutes = 60,
            riskLevel = 6
        ),
        // 连续等额转账
        RiskRule(
            name = "consecutive_equal_transfers",
            description = "连续等额转账",
            consecutiveCount = 3,
            riskLevel = 8
        )
    )

    /**
     * 分析单笔交易
     * @param transaction 待分析交易
     * @param recentTransactions 近期交易记录（用于上下文分析）
     * @return 分析结果
     */
    suspend fun analyzeTransaction(
        transaction: TransactionEntity,
        recentTransactions: List<TransactionEntity> = emptyList()
    ): TransactionAnalysisResult = withContext(Dispatchers.Default) {
        try {
            // 1. 规则引擎分析
            val ruleResults = analyzeByRules(transaction, recentTransactions)

            // 2. 统计分析
            val statisticalResult = analyzeByStatistics(transaction, recentTransactions)

            // 3. AI模型分析（如果可用）
            val aiResult = analyzeByAI(transaction, recentTransactions)

            // 4. 综合结果
            combineResults(ruleResults, statisticalResult, aiResult)
        } catch (e: Exception) {
            // 降级到规则引擎
            val ruleResults = analyzeByRules(transaction, recentTransactions)
            TransactionAnalysisResult(
                isAnomaly = ruleResults.any { it.isTriggered },
                riskLevel = ruleResults.filter { it.isTriggered }.maxOfOrNull { it.rule.riskLevel } ?: 0,
                triggeredRules = ruleResults.filter { it.isTriggered }.map { it.rule.name },
                anomalyType = determineAnomalyType(ruleResults),
                riskScore = calculateOverallScore(ruleResults)
            )
        }
    }

    /**
     * 基于规则分析
     */
    private fun analyzeByRules(
        transaction: TransactionEntity,
        recentTransactions: List<TransactionEntity>
    ): List<RuleAnalysisResult> {
        val results = mutableListOf<RuleAnalysisResult>()
        val calendar = Calendar.getInstance()

        // 规则1: 单笔大额转账
        results.add(
            RuleAnalysisResult(
                rule = defaultRules[0],
                isTriggered = transaction.amount >= defaultRules[0].threshold,
                details = "交易金额: ${transaction.amount}"
            )
        )

        // 规则2: 短时间内多次小额转账
        val thirtyMinutesAgo = System.currentTimeMillis() - 30 * 60 * 1000
        val recentSmallTransfers = recentTransactions.filter {
            it.transactionTime >= thirtyMinutesAgo && it.amount < 1000
        }
        results.add(
            RuleAnalysisResult(
                rule = defaultRules[1],
                isTriggered = recentSmallTransfers.size >= defaultRules[1].threshold.toInt(),
                details = "30分钟内小额转账次数: ${recentSmallTransfers.size}"
            )
        )

        // 规则3: 非常规时间交易
        calendar.timeInMillis = transaction.transactionTime
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val isUnusualTime = hour >= 23 || hour < 5
        results.add(
            RuleAnalysisResult(
                rule = defaultRules[2],
                isTriggered = isUnusualTime,
                details = "交易时间: ${hour}:00"
            )
        )

        // 规则4: 大额转给陌生账户（简化判断：首次出现的对手方）
        val knownCounterparties = recentTransactions.map { it.counterparty }.distinct()
        val isStranger = transaction.counterparty !in knownCounterparties
        results.add(
            RuleAnalysisResult(
                rule = defaultRules[3],
                isTriggered = transaction.amount >= defaultRules[3].threshold && isStranger,
                details = "对手方: ${transaction.counterparty}, 是否首次: $isStranger"
            )
        )

        // 规则5: 异常高频交易
        val oneHourAgo = System.currentTimeMillis() - 60 * 60 * 1000
        val hourlyTransactions = recentTransactions.filter {
            it.transactionTime >= oneHourAgo
        }
        results.add(
            RuleAnalysisResult(
                rule = defaultRules[4],
                isTriggered = hourlyTransactions.size >= defaultRules[4].threshold.toInt(),
                details = "1小时内交易次数: ${hourlyTransactions.size}"
            )
        )

        // 规则6: 连续等额转账
        val lastThree = recentTransactions.take(3)
        val isConsecutiveEqual = lastThree.size >= 3 &&
                lastThree[0].amount == lastThree[1].amount &&
                lastThree[1].amount == lastThree[2].amount &&
                lastThree[0].counterparty == lastThree[1].counterparty &&
                lastThree[1].counterparty == lastThree[2].counterparty
        results.add(
            RuleAnalysisResult(
                rule = defaultRules[5],
                isTriggered = isConsecutiveEqual,
                details = "连续3笔等额转账给同一对手方"
            )
        )

        return results
    }

    /**
     * 统计分析
     */
    private fun analyzeByStatistics(
        transaction: TransactionEntity,
        recentTransactions: List<TransactionEntity>
    ): StatisticalAnalysis {
        if (recentTransactions.isEmpty()) {
            return StatisticalAnalysis(
                mean = transaction.amount,
                stdDev = 0.0,
                deviationScore = 0f,
                isOutlier = false
            )
        }

        // 计算均值和标准差
        val amounts = recentTransactions.map { it.amount }
        val mean = amounts.average()
        val variance = amounts.map { (it - mean) * (it - mean) }.average()
        val stdDev = Math.sqrt(variance)

        // 计算偏离分数
        val deviationScore = if (stdDev > 0) {
            ((transaction.amount - mean) / stdDev).toFloat()
        } else {
            0f
        }

        // 判断是否为异常值（超过2个标准差）
        val isOutlier = Math.abs(deviationScore) > 2f

        return StatisticalAnalysis(
            mean = mean,
            stdDev = stdDev,
            deviationScore = deviationScore,
            isOutlier = isOutlier
        )
    }

    /**
     * AI模型分析
     */
    private suspend fun analyzeByAI(
        transaction: TransactionEntity,
        recentTransactions: List<TransactionEntity>
    ): Float? {
        return try {
            // 构建特征向量
            val features = extractFeatures(transaction, recentTransactions)
            val session = onnxManager.loadModelFromAssets(
                "models/transaction_anomaly.onnx",
                "transaction_anomaly"
            )

            val inputName = session.inputNames?.iterator()?.next()
            val result = onnxManager.runInference(
                session = session,
                inputName = inputName ?: "input",
                inputData = features,
                shape = longArrayOf(1, features.size.toLong())
            )

            // 返回异常分数
            result.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 提取交易特征
     */
    private fun extractFeatures(
        transaction: TransactionEntity,
        recentTransactions: List<TransactionEntity>
    ): FloatArray {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = transaction.transactionTime

        return floatArrayOf(
            transaction.amount.toFloat(),
            calendar.get(Calendar.HOUR_OF_DAY).toFloat(),
            calendar.get(Calendar.DAY_OF_WEEK).toFloat(),
            recentTransactions.size.toFloat(),
            recentTransactions.filter { it.isSuspicious }.size.toFloat(),
            recentTransactions.map { it.amount }.average().toFloat(),
            (transaction.amount / recentTransactions.map { it.amount }.average().coerceAtLeast(1.0)).toFloat()
        ).also {
            // 归一化
            val maxVal = it.maxOrNull() ?: 1f
            if (maxVal > 0) {
                for (i in it.indices) {
                    it[i] = it[i] / maxVal
                }
            }
        }
    }

    /**
     * 综合分析结果
     */
    private fun combineResults(
        ruleResults: List<RuleAnalysisResult>,
        statisticalResult: StatisticalAnalysis,
        aiScore: Float?
    ): TransactionAnalysisResult {
        val triggeredRules = ruleResults.filter { it.isTriggered }
        val ruleRiskLevel = triggeredRules.maxOfOrNull { it.rule.riskLevel } ?: 0

        // 统计异常增加风险等级
        val statisticalBonus = if (statisticalResult.isOutlier) 2 else 0

        // AI分数转换为风险等级
        val aiRiskLevel = when {
            aiScore == null -> 0
            aiScore > 0.8f -> 9
            aiScore > 0.6f -> 7
            aiScore > 0.4f -> 5
            aiScore > 0.2f -> 3
            else -> 1
        }

        // 综合风险等级
        val combinedRiskLevel = maxOf(
            ruleRiskLevel,
            statisticalBonus,
            aiRiskLevel
        ).coerceIn(0, 10)

        return TransactionAnalysisResult(
            isAnomaly = triggeredRules.isNotEmpty() || statisticalResult.isOutlier || (aiScore ?: 0f) > 0.5f,
            riskLevel = combinedRiskLevel,
            triggeredRules = triggeredRules.map { it.rule.name },
            anomalyType = determineAnomalyType(triggeredRules),
            riskScore = calculateOverallScore(triggeredRules),
            statisticalDeviation = statisticalResult.deviationScore,
            aiScore = aiScore
        )
    }

    /**
     * 确定异常类型
     */
    private fun determineAnomalyType(ruleResults: List<RuleAnalysisResult>): String {
        val triggered = ruleResults.filter { it.isTriggered }.map { it.rule.name }
        return when {
            "large_single_transfer" in triggered -> "大额转账"
            "frequent_small_transfers" in triggered -> "频繁小额转账"
            "unusual_time_transaction" in triggered -> "非常规时间交易"
            "large_transfer_to_stranger" in triggered -> "向陌生人大额转账"
            "high_frequency_transaction" in triggered -> "高频交易"
            "consecutive_equal_transfers" in triggered -> "连续等额转账"
            else -> "其他异常"
        }
    }

    /**
     * 计算总体风险分数
     */
    private fun calculateOverallScore(ruleResults: List<RuleAnalysisResult>): Float {
        val triggered = ruleResults.filter { it.isTriggered }
        if (triggered.isEmpty()) return 0f

        val totalRisk = triggered.sumOf { it.rule.riskLevel }.toFloat()
        val maxPossibleRisk = triggered.size * 10f
        return (totalRisk / maxPossibleRisk).coerceIn(0f, 1f)
    }

    /**
     * 释放资源
     */
    fun release() {
        onnxManager.releaseSession("transaction_anomaly")
    }

    companion object {
        @Volatile
        private var instance: TransactionAnomalyEngine? = null

        fun getInstance(context: Context): TransactionAnomalyEngine {
            return instance ?: synchronized(this) {
                instance ?: TransactionAnomalyEngine(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 风险规则
 */
data class RiskRule(
    val name: String,
    val description: String,
    val threshold: Double = 0.0,
    val timeWindowMinutes: Int = 0,
    val startHour: Int = 0,
    val endHour: Int = 0,
    val consecutiveCount: Int = 0,
    val riskLevel: Int = 5
)

/**
 * 规则分析结果
 */
data class RuleAnalysisResult(
    val rule: RiskRule,
    val isTriggered: Boolean,
    val details: String
)

/**
 * 统计分析结果
 */
data class StatisticalAnalysis(
    val mean: Double,
    val stdDev: Double,
    val deviationScore: Float,
    val isOutlier: Boolean
)

/**
 * 交易分析结果
 */
data class TransactionAnalysisResult(
    val isAnomaly: Boolean,
    val riskLevel: Int,
    val triggeredRules: List<String>,
    val anomalyType: String,
    val riskScore: Float,
    val statisticalDeviation: Float? = null,
    val aiScore: Float? = null
) {
    /**
     * 获取风险等级描述
     */
    fun getRiskLevelDescription(): String {
        return when {
            riskLevel >= 9 -> "极高风险"
            riskLevel >= 7 -> "高风险"
            riskLevel >= 5 -> "中风险"
            riskLevel >= 3 -> "低风险"
            else -> "正常"
        }
    }

    /**
     * 获取建议操作
     */
    fun getRecommendedAction(): String {
        return when {
            riskLevel >= 9 -> "立即停止交易，联系银行确认"
            riskLevel >= 7 -> "建议暂停交易，核实收款方信息"
            riskLevel >= 5 -> "请注意交易安全，确认是否为本人操作"
            riskLevel >= 3 -> "交易基本正常，请留意后续账单"
            else -> "交易正常"
        }
    }

    /**
     * 转换为JSON
     */
    fun toJson(): org.json.JSONObject {
        return org.json.JSONObject().apply {
            put("isAnomaly", isAnomaly)
            put("riskLevel", riskLevel)
            put("triggeredRules", org.json.JSONArray(triggeredRules))
            put("anomalyType", anomalyType)
            put("riskScore", riskScore)
            put("statisticalDeviation", statisticalDeviation)
            put("aiScore", aiScore)
        }
    }
}
