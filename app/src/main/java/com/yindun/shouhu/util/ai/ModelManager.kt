package com.yindun.shouhu.util.ai

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer

/**
 * AI模型管理器
 * 负责加载和管理小型开源AI模型
 */
class ModelManager(private val context: Context) {

    private val ortEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private val sessionCache = mutableMapOf<String, OrtSession>()
    private val modelsDir by lazy {
        File(context.filesDir, "ai_models").also { it.mkdirs() }
    }

    /**
     * 模型配置
     */
    data class ModelConfig(
        val name: String,
        val assetPath: String,
        val version: Int,
        val description: String,
        val inputShape: LongArray,
        val inputName: String = "input"
    )

    companion object {
        // 预定义模型配置
        val FRAUD_DETECTOR = ModelConfig(
            name = "fraud_detector",
            assetPath = "models/fraud_detector.onnx",
            version = 1,
            description = "诈骗话术识别模型",
            inputShape = longArrayOf(1, 128)
        )

        val TRANSACTION_ANOMALY = ModelConfig(
            name = "transaction_anomaly",
            assetPath = "models/transaction_anomaly.onnx",
            version = 1,
            description = "交易异常检测模型",
            inputShape = longArrayOf(1, 7)
        )

        val TEXT_CLASSIFIER = ModelConfig(
            name = "text_classifier",
            assetPath = "models/text_classifier.onnx",
            version = 1,
            description = "文本分类模型",
            inputShape = longArrayOf(1, 64)
        )
    }

    /**
     * 初始化模型
     */
    suspend fun initializeModels() = withContext(Dispatchers.IO) {
        try {
            // 尝试加载模型，如果不存在则使用规则引擎
            loadModelIfAvailable(FRAUD_DETECTOR)
            loadModelIfAvailable(TRANSACTION_ANOMALY)
            loadModelIfAvailable(TEXT_CLASSIFIER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 如果模型文件存在则加载
     */
    private suspend fun loadModelIfAvailable(config: ModelConfig) = withContext(Dispatchers.IO) {
        try {
            // 检查assets中是否有模型文件
            val assetExists = try {
                context.assets.open(config.assetPath).use { true }
            } catch (e: Exception) {
                false
            }

            if (assetExists) {
                loadModelFromAssets(config)
            } else {
                // 创建占位模型
                createPlaceholderModel(config)
            }
        } catch (e: Exception) {
            android.util.Log.w("ModelManager", "无法加载模型 ${config.name}: ${e.message}")
        }
    }

    /**
     * 从assets加载模型
     */
    private suspend fun loadModelFromAssets(config: ModelConfig) = withContext(Dispatchers.IO) {
        val internalFile = File(modelsDir, "${config.name}_v${config.version}.onnx")

        if (!internalFile.exists()) {
            // 从assets解压模型
            context.assets.open(config.assetPath).use { input ->
                FileOutputStream(internalFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        // 创建会话
        val session = ortEnvironment.createSession(internalFile.absolutePath)
        sessionCache[config.name] = session
        android.util.Log.d("ModelManager", "模型加载成功: ${config.name}")
    }

    /**
     * 创建占位模型（当真实模型不存在时使用规则引擎）
     */
    private fun createPlaceholderModel(config: ModelConfig) {
        android.util.Log.d("ModelManager", "使用规则引擎替代: ${config.name}")
    }

    /**
     * 获取模型会话
     */
    fun getModelSession(config: ModelConfig): OrtSession? {
        return sessionCache[config.name]
    }

    /**
     * 诈骗话术检测
     */
    suspend fun detectFraudText(text: String): FraudDetectionResult = withContext(Dispatchers.Default) {
        try {
            val session = sessionCache[FRAUD_DETECTOR.name]
            if (session != null) {
                // 使用AI模型检测
                detectWithAI(text, session)
            } else {
                // 使用规则引擎检测
                detectWithRules(text)
            }
        } catch (e: Exception) {
            detectWithRules(text)
        }
    }

    /**
     * 使用AI模型检测
     */
    private suspend fun detectWithAI(text: String, session: OrtSession): FraudDetectionResult = withContext(Dispatchers.Default) {
        // 文本向量化
        val inputVector = textToVector(text)
        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(inputVector),
            FRAUD_DETECTOR.inputShape
        )

        val results = session.run(mapOf(FRAUD_DETECTOR.inputName to inputTensor))
        val output = results[0].value as? FloatArray ?: FloatArray(10)

        // 解析结果
        val labels = listOf(
            "health_product", "investment", "impersonate", "lottery",
            "family_emergency", "customer_service", "loan_fraud",
            "refund_fraud", "prize_fraud", "normal"
        )

        val maxIndex = output.indices.maxByOrNull { output[it] } ?: 9
        val confidence = output[maxIndex]

        FraudDetectionResult(
            isFraud = maxIndex < 9 && confidence > 0.5f,
            fraudType = labels[maxIndex],
            confidence = confidence,
            riskScore = if (maxIndex < 9) confidence else 0f
        )
    }

    /**
     * 使用规则引擎检测
     */
    private fun detectWithRules(text: String): FraudDetectionResult {
        val keywords = mapOf(
            "health_product" to listOf("免费体检", "特效药", "定金", "健康讲座"),
            "investment" to listOf("高额回报", "稳赚不赔", "投资项目", "内幕消息"),
            "impersonate" to listOf("公安局", "安全账户", "涉嫌", "配合调查"),
            "lottery" to listOf("中奖", "特等奖", "个人所得税", "领奖"),
            "family_emergency" to listOf("出事了", "急需用钱", "不要告诉别人", "快转账"),
            "customer_service" to listOf("退款", "验证码", "银行卡号", "订单异常")
        )

        val matchedScores = mutableMapOf<String, Float>()

        for ((type, typeKeywords) in keywords) {
            val matchedCount = typeKeywords.count { text.contains(it) }
            if (matchedCount > 0) {
                matchedScores[type] = matchedCount.toFloat() / typeKeywords.size
            }
        }

        val maxEntry = matchedScores.maxByOrNull { it.value }

        return if (maxEntry != null && maxEntry.value > 0.2f) {
            FraudDetectionResult(
                isFraud = true,
                fraudType = maxEntry.key,
                confidence = maxEntry.value,
                riskScore = (maxEntry.value * 10).toInt().coerceIn(1, 10).toFloat()
            )
        } else {
            FraudDetectionResult(
                isFraud = false,
                fraudType = "normal",
                confidence = 0.9f,
                riskScore = 0f
            )
        }
    }

    /**
     * 交易异常检测
     */
    suspend fun detectTransactionAnomaly(
        amount: Double,
        hour: Int,
        dayOfWeek: Int,
        recentCount: Int,
        avgAmount: Double,
        isKnownRecipient: Boolean,
        frequentSmallCount: Int
    ): AnomalyDetectionResult = withContext(Dispatchers.Default) {
        try {
            val session = sessionCache[TRANSACTION_ANOMALY.name]
            if (session != null) {
                detectTransactionWithAI(
                    amount, hour, dayOfWeek, recentCount,
                    avgAmount, isKnownRecipient, frequentSmallCount, session
                )
            } else {
                detectTransactionWithRules(
                    amount, hour, dayOfWeek, recentCount,
                    avgAmount, isKnownRecipient, frequentSmallCount
                )
            }
        } catch (e: Exception) {
            detectTransactionWithRules(
                amount, hour, dayOfWeek, recentCount,
                avgAmount, isKnownRecipient, frequentSmallCount
            )
        }
    }

    /**
     * 使用AI模型检测交易异常
     */
    private suspend fun detectTransactionWithAI(
        amount: Double, hour: Int, dayOfWeek: Int, recentCount: Int,
        avgAmount: Double, isKnownRecipient: Boolean, frequentSmallCount: Int,
        session: OrtSession
    ): AnomalyDetectionResult = withContext(Dispatchers.Default) {
        val features = floatArrayOf(
            amount.toFloat(),
            hour.toFloat(),
            dayOfWeek.toFloat(),
            recentCount.toFloat(),
            avgAmount.toFloat(),
            if (isKnownRecipient) 0f else 1f,
            frequentSmallCount.toFloat()
        ).map { it / 1000f }.toFloatArray() // 归一化

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(features),
            TRANSACTION_ANOMALY.inputShape
        )

        val results = session.run(mapOf(TRANSACTION_ANOMALY.inputName to inputTensor))
        val score = (results[0].value as? FloatArray)?.firstOrNull() ?: 0f

        AnomalyDetectionResult(
            isAnomaly = score > 0.5f,
            anomalyScore = score,
            riskLevel = (score * 10).toInt().coerceIn(0, 10)
        )
    }

    /**
     * 使用规则检测交易异常
     */
    private fun detectTransactionWithRules(
        amount: Double, hour: Int, dayOfWeek: Int, recentCount: Int,
        avgAmount: Double, isKnownRecipient: Boolean, frequentSmallCount: Int
    ): AnomalyDetectionResult {
        var riskScore = 0f

        // 大额交易
        if (amount >= 10000) riskScore += 0.3f
        else if (amount >= 5000) riskScore += 0.2f

        // 深夜交易
        if (hour >= 23 || hour < 5) riskScore += 0.2f

        // 高频交易
        if (recentCount >= 10) riskScore += 0.2f
        else if (recentCount >= 5) riskScore += 0.1f

        // 向陌生人转账
        if (!isKnownRecipient && amount >= 5000) riskScore += 0.2f

        // 频繁小额
        if (frequentSmallCount >= 5) riskScore += 0.15f

        // 金额异常（超过平均值3倍）
        if (avgAmount > 0 && amount > avgAmount * 3) riskScore += 0.15f

        return AnomalyDetectionResult(
            isAnomaly = riskScore > 0.4f,
            anomalyScore = riskScore.coerceIn(0f, 1f),
            riskLevel = (riskScore * 10).toInt().coerceIn(0, 10)
        )
    }

    /**
     * 文本向量化
     */
    private fun textToVector(text: String, size: Int = 128): FloatArray {
        val vector = FloatArray(size)
        text.forEachIndexed { index, char ->
            if (index < size) {
                vector[index] = char.code.toFloat() / 1000f
            }
        }
        return vector
    }

    /**
     * 释放资源
     */
    fun release() {
        sessionCache.values.forEach { it.close() }
        sessionCache.clear()
    }
}

/**
 * 诈骗检测结果
 */
data class FraudDetectionResult(
    val isFraud: Boolean,
    val fraudType: String,
    val confidence: Float,
    val riskScore: Float
) {
    fun getFraudTypeDisplayName(): String {
        return when (fraudType) {
            "health_product" -> "保健品诈骗"
            "investment" -> "投资理财诈骗"
            "impersonate" -> "冒充公检法"
            "lottery" -> "中奖诈骗"
            "family_emergency" -> "冒充亲属紧急情况"
            "customer_service" -> "冒充客服"
            "loan_fraud" -> "贷款诈骗"
            "refund_fraud" -> "退款诈骗"
            "prize_fraud" -> "领奖诈骗"
            else -> "正常"
        }
    }

    fun getRiskDescription(): String {
        return when {
            riskScore >= 8 -> "极高风险，请立即挂断电话"
            riskScore >= 6 -> "高风险，请谨慎接听"
            riskScore >= 4 -> "中等风险，请注意防范"
            riskScore >= 2 -> "低风险"
            else -> "正常"
        }
    }
}

/**
 * 异常检测结果
 */
data class AnomalyDetectionResult(
    val isAnomaly: Boolean,
    val anomalyScore: Float,
    val riskLevel: Int
) {
    fun getRiskDescription(): String {
        return when {
            riskLevel >= 8 -> "高风险异常交易"
            riskLevel >= 6 -> "中风险异常交易"
            riskLevel >= 4 -> "低风险异常交易"
            else -> "正常交易"
        }
    }
}
