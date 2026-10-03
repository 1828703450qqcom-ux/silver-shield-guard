package com.yindun.shouhu.domain.engine

import android.content.Context
import com.yindun.shouhu.util.ai.OnnxRuntimeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 诈骗话术识别引擎
 * 基于AI模型识别诈骗话术特征
 */
class FraudDetectionEngine(private val context: Context) {

    private val onnxManager = OnnxRuntimeManager.getInstance(context)
    private val session by lazy { loadModel() }

    // 诈骗类型标签
    private val fraudLabels = listOf(
        "health_product",      // 保健品诈骗
        "investment",          // 投资理财诈骗
        "impersonate",         // 冒充公检法
        "lottery",             // 中奖诈骗
        "family_emergency",    // 冒充亲属紧急情况
        "customer_service",    // 冒充客服
        "loan_fraud",          // 贷款诈骗
        "refund_fraud",        // 退款诈骗
        "prize_fraud",         // 领奖诈骗
        "normal"               // 正常通话
    )

    // 中文标签映射
    private val labelChineseMap = mapOf(
        "health_product" to "保健品诈骗",
        "investment" to "投资理财诈骗",
        "impersonate" to "冒充公检法",
        "lottery" to "中奖诈骗",
        "family_emergency" to "冒充亲属紧急情况",
        "customer_service" to "冒充客服",
        "loan_fraud" to "贷款诈骗",
        "refund_fraud" to "退款诈骗",
        "prize_fraud" to "领奖诈骗",
        "normal" to "正常通话"
    )

    // 关键词库
    private val keywordMap = mapOf(
        "health_product" to listOf(
            "免费体检", "健康讲座", "特效药", "祖传秘方", "包治百病",
            "保健品", "养生", "延年益寿", "老中医", "疗程", "根治"
        ),
        "investment" to listOf(
            "高额回报", "稳赚不赔", "投资理财", "内幕消息", "原始股",
            "虚拟货币", "区块链", "炒外汇", "保本保息", "日赚万元"
        ),
        "impersonate" to listOf(
            "公检法", "安全账户", "涉嫌洗钱", "逮捕令", "配合调查",
            "资金冻结", "转账到安全账户", "案件编号", "保密", "不能告诉任何人"
        ),
        "lottery" to listOf(
            "中奖", "恭喜中奖", "领奖", "交税", "手续费", "保证金",
            "奖品", "抽奖", "幸运用户", "特等奖"
        ),
        "family_emergency" to listOf(
            "紧急情况", "出事了", "住院", "手术", "急需用钱",
            "先转账", "不要告诉别人", "快打钱", "救人"
        ),
        "customer_service" to listOf(
            "退款", "理赔", "订单异常", "账号异常", "验证身份",
            "银行卡号", "验证码", "转账", "刷单"
        ),
        "loan_fraud" to listOf(
            "低息贷款", "无抵押", "秒到账", "贷款额度", "先交费",
            "解冻费", "保证金", "工本费", "征信"
        ),
        "refund_fraud" to listOf(
            "淘宝退款", "京东退款", "快递丢失", "双倍赔偿", "先转账",
            "刷单返现", "好评返现"
        ),
        "prize_fraud" to listOf(
            "中奖通知", "领取奖品", "缴纳个税", "公证费", "领奖码",
            "奖品已送出", "限时领取"
        )
    )

    // 高风险短语
    private val highRiskPhrases = listOf(
        "安全账户", "转账", "汇款", "验证码", "密码",
        "银行卡号", "身份证号", "不能告诉任何人", "保密",
        "马上转账", "立刻汇款", "限时", "过期作废"
    )

    private fun loadModel() {
        try {
            // 尝试加载模型，如果不存在则使用规则引擎
            // 实际项目中会加载ONNX模型
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 分析文本内容，检测是否为诈骗
     * @param text 待分析文本
     * @param isCallText 是否是通话转文字
     * @return 分析结果
     */
    suspend fun analyzeText(
        text: String,
        isCallText: Boolean = false
    ): FraudAnalysisResult = withContext(Dispatchers.Default) {
        try {
            // 1. 基于规则的关键词匹配
            val ruleBasedResult = analyzeByRules(text)

            // 2. 基于AI模型的深度分析
            val aiResult = analyzeByAI(text)

            // 3. 综合两种结果
            combineResults(ruleBasedResult, aiResult, isCallText)
        } catch (e: Exception) {
            // 降级到纯规则引擎
            analyzeByRules(text)
        }
    }

    /**
     * 基于规则分析文本
     */
    private fun analyzeByRules(text: String): FraudAnalysisResult {
        val matchedKeywords = mutableMapOf<String, MutableList<String>>()
        val riskScores = mutableMapOf<String, Float>()

        // 对每种诈骗类型进行关键词匹配
        for ((type, keywords) in keywordMap) {
            val matched = keywords.filter { text.contains(it) }
            if (matched.isNotEmpty()) {
                matchedKeywords[type] = matched.toMutableList()
                // 基础分数：匹配关键词数量 / 总关键词数量
                riskScores[type] = matched.size.toFloat() / keywords.size
            }
        }

        // 检查高风险短语
        val highRiskMatches = highRiskPhrases.filter { text.contains(it) }
        val highRiskBonus = highRiskMatches.size * 0.15f

        // 找出最高风险的类型
        val maxEntry = riskScores.maxByOrNull { it.value }

        return if (maxEntry != null && maxEntry.value > 0.1f) {
            val riskLevel = calculateRiskLevel(maxEntry.value + highRiskBonus)
            FraudAnalysisResult(
                isFraud = riskLevel >= 5,
                fraudType = maxEntry.key,
                fraudTypeChinese = labelChineseMap[maxEntry.key] ?: maxEntry.key,
                riskScore = (maxEntry.value + highRiskBonus).coerceIn(0f, 1f),
                riskLevel = riskLevel,
                matchedKeywords = matchedKeywords[maxEntry.key] ?: emptyList(),
                highRiskPhrases = highRiskMatches,
                confidence = calculateConfidence(matchedKeywords[maxEntry.key]?.size ?: 0)
            )
        } else {
            FraudAnalysisResult(
                isFraud = false,
                fraudType = "normal",
                fraudTypeChinese = "正常通话",
                riskScore = 0f,
                riskLevel = 0,
                matchedKeywords = emptyList(),
                highRiskPhrases = highRiskMatches,
                confidence = 0.9f
            )
        }
    }

    /**
     * 基于AI模型分析文本
     */
    private suspend fun analyzeByAI(text: String): FraudAnalysisResult? {
        return try {
            // 文本预处理和向量化
            val inputVector = preprocessText(text)
            val session = onnxManager.loadModelFromAssets(
                "models/fraud_detector.onnx",
                "fraud_detector"
            )

            val inputName = session.inputNames?.iterator()?.next()
            val result = onnxManager.runInference(
                session = session,
                inputName = inputName ?: "input",
                inputData = inputVector,
                shape = longArrayOf(1, inputVector.size.toLong())
            )

            // 解析模型输出
            parseModelOutput(result)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 文本预处理：将文本转换为模型输入向量
     */
    private fun preprocessText(text: String): FloatArray {
        // 简单的文本向量化实现
        // 实际项目中会使用更复杂的NLP处理
        val vocab = buildVocabulary()
        val vector = FloatArray(128)

        text.forEachIndexed { index, char ->
            if (index < vector.size) {
                vector[index] = vocab[char]?.toFloat() ?: 0f
            }
        }

        // 归一化
        val maxVal = vector.maxOrNull() ?: 1f
        if (maxVal > 0) {
            return vector.map { it / maxVal }.toFloatArray()
        }
        return vector
    }

    /**
     * 构建简单词典
     */
    private fun buildVocabulary(): Map<Char, Int> {
        val vocab = mutableMapOf<Char, Int>()
        // 添加常用字符
        ('a'..'z').forEachIndexed { i, c -> vocab[c] = i + 1 }
        ('A'..'Z').forEachIndexed { i, c -> vocab[c] = i + 27 }
        ('0'..'9').forEachIndexed { i, c -> vocab[c] = i + 53 }
        // 添加中文字符
        val commonChars = "的一是不了人我在有他这中大来上个国到说们为子和你地出会也时要就可以生"
        commonChars.forEachIndexed { i, c -> vocab[c] = i + 63 }
        return vocab
    }

    /**
     * 解析模型输出
     */
    private fun parseModelOutput(output: FloatArray): FraudAnalysisResult? {
        if (output.size < fraudLabels.size) return null

        // 获取最高概率的类别
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: return null
        val maxProb = output[maxIndex]

        return if (maxIndex < fraudLabels.size) {
            val fraudType = fraudLabels[maxIndex]
            FraudAnalysisResult(
                isFraud = fraudType != "normal" && maxProb > 0.5f,
                fraudType = fraudType,
                fraudTypeChinese = labelChineseMap[fraudType] ?: fraudType,
                riskScore = maxProb,
                riskLevel = if (maxProb > 0.8f) 9 else if (maxProb > 0.6f) 7 else 5,
                matchedKeywords = emptyList(),
                highRiskPhrases = emptyList(),
                confidence = maxProb
            )
        } else null
    }

    /**
     * 综合规则和AI分析结果
     */
    private fun combineResults(
        ruleResult: FraudAnalysisResult,
        aiResult: FraudAnalysisResult?,
        isCallText: Boolean
    ): FraudAnalysisResult {
        if (aiResult == null) return ruleResult

        // 如果两种方法都识别为诈骗
        if (ruleResult.isFraud && aiResult.isFraud) {
            // 取更高的风险等级
            val combinedRiskScore = (ruleResult.riskScore + aiResult.riskScore) / 2
            val combinedRiskLevel = maxOf(ruleResult.riskLevel, aiResult.riskLevel)

            return ruleResult.copy(
                riskScore = combinedRiskScore.coerceIn(0f, 1f),
                riskLevel = combinedRiskLevel,
                confidence = (ruleResult.confidence + aiResult.confidence) / 2,
                matchedKeywords = (ruleResult.matchedKeywords + aiResult.matchedKeywords).distinct(),
                highRiskPhrases = (ruleResult.highRiskPhrases + aiResult.highRiskPhrases).distinct()
            )
        }

        // 如果只有一种方法识别为诈骗，降低置信度
        if (ruleResult.isFraud || aiResult.isFraud) {
            val fraudResult = if (ruleResult.isFraud) ruleResult else aiResult
            return fraudResult.copy(
                confidence = fraudResult.confidence * 0.7f
            )
        }

        return ruleResult
    }

    /**
     * 计算风险等级 (0-10)
     */
    private fun calculateRiskLevel(score: Float): Int {
        return when {
            score >= 0.9f -> 10
            score >= 0.8f -> 9
            score >= 0.7f -> 8
            score >= 0.6f -> 7
            score >= 0.5f -> 6
            score >= 0.4f -> 5
            score >= 0.3f -> 4
            score >= 0.2f -> 3
            score >= 0.1f -> 2
            score > 0f -> 1
            else -> 0
        }
    }

    /**
     * 计算置信度
     */
    private fun calculateConfidence(matchCount: Int): Float {
        return when {
            matchCount >= 5 -> 0.95f
            matchCount >= 4 -> 0.9f
            matchCount >= 3 -> 0.85f
            matchCount >= 2 -> 0.75f
            matchCount >= 1 -> 0.65f
            else -> 0.5f
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        onnxManager.releaseSession("fraud_detector")
    }

    companion object {
        @Volatile
        private var instance: FraudDetectionEngine? = null

        fun getInstance(context: Context): FraudDetectionEngine {
            return instance ?: synchronized(this) {
                instance ?: FraudDetectionEngine(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 诈骗分析结果
 */
data class FraudAnalysisResult(
    val isFraud: Boolean,
    val fraudType: String,
    val fraudTypeChinese: String,
    val riskScore: Float,
    val riskLevel: Int,
    val matchedKeywords: List<String>,
    val highRiskPhrases: List<String>,
    val confidence: Float
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
            else -> "安全"
        }
    }

    /**
     * 获取建议操作
     */
    fun getRecommendedAction(): String {
        return when {
            riskLevel >= 9 -> "立即挂断电话，不要透露任何信息，联系家人或拨打96110"
            riskLevel >= 7 -> "建议挂断电话，如有疑问可拨打官方客服核实"
            riskLevel >= 5 -> "请保持警惕，不要进行转账操作"
            riskLevel >= 3 -> "注意保护个人信息"
            else -> "正常通话"
        }
    }

    /**
     * 转换为JSON
     */
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("isFraud", isFraud)
            put("fraudType", fraudType)
            put("fraudTypeChinese", fraudTypeChinese)
            put("riskScore", riskScore)
            put("riskLevel", riskLevel)
            put("matchedKeywords", JSONArray(matchedKeywords))
            put("highRiskPhrases", JSONArray(highRiskPhrases))
            put("confidence", confidence)
        }
    }
}
