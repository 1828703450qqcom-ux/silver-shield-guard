package com.yindun.shouhu.domain.engine

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.RiskRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 风控规则管理器
 * 管理和执行各种风控规则
 */
class RiskRuleManager(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    // 默认规则配置
    private val defaultRules = listOf(
        DefaultRiskRule(
            name = "large_amount_transfer",
            description = "大额转账",
            type = RuleType.AMOUNT_THRESHOLD,
            params = JSONObject().apply {
                put("threshold", 10000)
                put("operator", ">=")
            },
            riskLevel = 7
        ),
        DefaultRiskRule(
            name = "frequent_small_transfer",
            description = "频繁小额转账",
            type = RuleType.FREQUENCY,
            params = JSONObject().apply {
                put("minAmount", 100)
                put("maxAmount", 1000)
                put("count", 5)
                put("timeWindowMinutes", 30)
            },
            riskLevel = 8
        ),
        DefaultRiskRule(
            name = "night_transaction",
            description = "深夜交易",
            type = RuleType.TIME_BASED,
            params = JSONObject().apply {
                put("startHour", 23)
                put("endHour", 5)
                put("minAmount", 500)
            },
            riskLevel = 5
        ),
        DefaultRiskRule(
            name = "new_recipient_large_amount",
            description = "向新收款方大额转账",
            type = RuleType.PATTERN,
            params = JSONObject().apply {
                put("threshold", 5000)
                put("isNewRecipient", true)
            },
            riskLevel = 8
        ),
        DefaultRiskRule(
            name = "rapid_successive_transfers",
            description = "连续快速转账",
            type = RuleType.FREQUENCY,
            params = JSONObject().apply {
                put("count", 3)
                put("timeWindowMinutes", 10)
                put("sameAmount", true)
            },
            riskLevel = 9
        ),
        DefaultRiskRule(
            name = "high_frequency_transaction",
            description = "高频交易",
            type = RuleType.FREQUENCY,
            params = JSONObject().apply {
                put("count", 20)
                put("timeWindowMinutes", 60)
            },
            riskLevel = 7
        )
    )

    /**
     * 初始化默认规则
     */
    suspend fun initDefaultRules() = withContext(Dispatchers.IO) {
        try {
            val existingRules = database.riskRuleDao().getActiveRules()
            if (existingRules.isEmpty()) {
                defaultRules.forEach { defaultRule ->
                    val entity = RiskRuleEntity(
                        ruleName = defaultRule.name,
                        ruleType = defaultRule.type.value,
                        parameters = defaultRule.params.toString(),
                        riskLevel = defaultRule.riskLevel,
                        isActive = true
                    )
                    database.riskRuleDao().insertRule(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取所有活跃规则
     */
    suspend fun getActiveRules(): List<RiskRuleEntity> = withContext(Dispatchers.IO) {
        database.riskRuleDao().getActiveRules()
    }

    /**
     * 添加自定义规则
     */
    suspend fun addCustomRule(
        name: String,
        description: String,
        type: RuleType,
        params: JSONObject,
        riskLevel: Int
    ): Long = withContext(Dispatchers.IO) {
        val entity = RiskRuleEntity(
            ruleName = name,
            ruleType = type.value,
            parameters = params.toString(),
            riskLevel = riskLevel.coerceIn(1, 10),
            isActive = true
        )
        database.riskRuleDao().insertRule(entity)
    }

    /**
     * 更新规则
     */
    suspend fun updateRule(rule: RiskRuleEntity) = withContext(Dispatchers.IO) {
        database.riskRuleDao().updateRule(rule)
    }

    /**
     * 禁用规则
     */
    suspend fun disableRule(ruleId: Long) = withContext(Dispatchers.IO) {
        val rule = database.riskRuleDao().getActiveRules().find { it.id == ruleId }
        rule?.let {
            database.riskRuleDao().updateRule(it.copy(isActive = false))
        }
    }

    /**
     * 删除规则
     */
    suspend fun deleteRule(rule: RiskRuleEntity) = withContext(Dispatchers.IO) {
        database.riskRuleDao().deleteRule(rule)
    }

    /**
     * 评估交易风险
     */
    suspend fun evaluateTransaction(
        amount: Double,
        counterparty: String,
        transactionTime: Long,
        recentTransactions: List<TransactionSummary>
    ): RuleEvaluationResult = withContext(Dispatchers.Default) {
        val activeRules = database.riskRuleDao().getActiveRules()
        val triggeredRules = mutableListOf<TriggeredRule>()

        for (rule in activeRules) {
            try {
                val params = JSONObject(rule.parameters)
                val isTriggered = when (RuleType.fromValue(rule.ruleType)) {
                    RuleType.AMOUNT_THRESHOLD -> evaluateAmountRule(amount, params)
                    RuleType.FREQUENCY -> evaluateFrequencyRule(amount, recentTransactions, params)
                    RuleType.TIME_BASED -> evaluateTimeRule(amount, transactionTime, params)
                    RuleType.PATTERN -> evaluatePatternRule(amount, counterparty, recentTransactions, params)
                    null -> false
                }

                if (isTriggered) {
                    triggeredRules.add(
                        TriggeredRule(
                            ruleId = rule.id,
                            ruleName = rule.ruleName,
                            description = rule.ruleName,
                            riskLevel = rule.riskLevel
                        )
                    )
                }
            } catch (e: Exception) {
                // 规则解析失败，跳过
            }
        }

        val maxRiskLevel = triggeredRules.maxOfOrNull { it.riskLevel } ?: 0
        val riskScore = calculateRiskScore(triggeredRules)

        RuleEvaluationResult(
            isAnomaly = triggeredRules.isNotEmpty(),
            riskLevel = maxRiskLevel,
            riskScore = riskScore,
            triggeredRules = triggeredRules
        )
    }

    /**
     * 评估金额规则
     */
    private fun evaluateAmountRule(amount: Double, params: JSONObject): Boolean {
        val threshold = params.getDouble("threshold")
        val operator = params.optString("operator", ">=")

        return when (operator) {
            ">=" -> amount >= threshold
            ">" -> amount > threshold
            "==" -> amount == threshold
            "<=" -> amount <= threshold
            "<" -> amount < threshold
            else -> false
        }
    }

    /**
     * 评估频率规则
     */
    private fun evaluateFrequencyRule(
        amount: Double,
        recentTransactions: List<TransactionSummary>,
        params: JSONObject
    ): Boolean {
        val requiredCount = params.getInt("count")
        val timeWindowMinutes = params.getInt("timeWindowMinutes")
        val minAmount = params.optDouble("minAmount", 0.0)
        val maxAmount = params.optDouble("maxAmount", Double.MAX_VALUE)
        val sameAmount = params.optBoolean("sameAmount", false)

        val timeWindow = timeWindowMinutes * 60 * 1000L
        val cutoffTime = System.currentTimeMillis() - timeWindow

        val matchingTransactions = recentTransactions.filter { tx ->
            tx.timestamp >= cutoffTime &&
                    tx.amount >= minAmount &&
                    tx.amount <= maxAmount &&
                    (!sameAmount || tx.amount == amount)
        }

        return matchingTransactions.size >= requiredCount
    }

    /**
     * 评估时间规则
     */
    private fun evaluateTimeRule(amount: Double, transactionTime: Long, params: JSONObject): Boolean {
        val startHour = params.getInt("startHour")
        val endHour = params.getInt("endHour")
        val minAmount = params.optDouble("minAmount", 0.0)

        if (amount < minAmount) return false

        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = transactionTime
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        return if (startHour > endHour) {
            // 跨越午夜的情况，如 23:00 - 5:00
            hour >= startHour || hour < endHour
        } else {
            hour in startHour until endHour
        }
    }

    /**
     * 评估模式规则
     */
    private fun evaluatePatternRule(
        amount: Double,
        counterparty: String,
        recentTransactions: List<TransactionSummary>,
        params: JSONObject
    ): Boolean {
        val threshold = params.optDouble("threshold", 0.0)
        val isNewRecipient = params.optBoolean("isNewRecipient", false)

        if (amount < threshold) return false

        if (isNewRecipient) {
            // 检查是否是新的收款方
            val knownRecipients = recentTransactions.map { it.counterparty }.toSet()
            return counterparty !in knownRecipients
        }

        return false
    }

    /**
     * 计算风险分数
     */
    private fun calculateRiskScore(triggeredRules: List<TriggeredRule>): Float {
        if (triggeredRules.isEmpty()) return 0f

        val totalWeight = triggeredRules.sumOf { it.riskLevel }.toFloat()
        val maxPossibleWeight = triggeredRules.size * 10f

        return (totalWeight / maxPossibleWeight).coerceIn(0f, 1f)
    }

    companion object {
        @Volatile
        private var instance: RiskRuleManager? = null

        fun getInstance(context: Context): RiskRuleManager {
            return instance ?: synchronized(this) {
                instance ?: RiskRuleManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 规则类型
 */
enum class RuleType(val value: String) {
    AMOUNT_THRESHOLD("amount_threshold"),
    FREQUENCY("frequency"),
    TIME_BASED("time_based"),
    PATTERN("pattern");

    companion object {
        fun fromValue(value: String): RuleType? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * 默认风控规则
 */
data class DefaultRiskRule(
    val name: String,
    val description: String,
    val type: RuleType,
    val params: JSONObject,
    val riskLevel: Int
)

/**
 * 交易摘要（用于规则评估）
 */
data class TransactionSummary(
    val amount: Double,
    val counterparty: String,
    val timestamp: Long
)

/**
 * 触发的规则
 */
data class TriggeredRule(
    val ruleId: Long,
    val ruleName: String,
    val description: String,
    val riskLevel: Int
)

/**
 * 规则评估结果
 */
data class RuleEvaluationResult(
    val isAnomaly: Boolean,
    val riskLevel: Int,
    val riskScore: Float,
    val triggeredRules: List<TriggeredRule>
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("isAnomaly", isAnomaly)
            put("riskLevel", riskLevel)
            put("riskScore", riskScore)
            put("triggeredRules", JSONArray(triggeredRules.map { it.ruleName }))
        }
    }
}
