package com.yindun.shouhu.domain.engine

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.FraudScriptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 话术引擎
 * 管理和执行诈骗模拟训练话术
 */
class ScriptEngine(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    /**
     * 获取所有话术分类
     */
    fun getAllCategories(): Flow<List<String>> {
        return database.fraudScriptDao().getAllCategories()
    }

    /**
     * 获取指定分类的话术
     */
    fun getScriptsByCategory(category: String): Flow<List<FraudScriptEntity>> {
        return database.fraudScriptDao().getScriptsByCategory(category)
    }

    /**
     * 获取指定方言的话术
     */
    fun getScriptsByDialect(dialect: String): Flow<List<FraudScriptEntity>> {
        return database.fraudScriptDao().getScriptsByDialect(dialect)
    }

    /**
     * 获取话术详情
     */
    suspend fun getScriptById(scriptId: Long): FraudScriptEntity? {
        return database.fraudScriptDao().getScriptById(scriptId)
    }

    /**
     * 解析话术内容
     */
    fun parseScriptContent(scriptContent: String): ScriptContent {
        return try {
            val json = JSONObject(scriptContent)
            val dialogueArray = json.getJSONArray("dialogue")

            val dialogue = mutableListOf<DialogueLine>()
            for (i in 0 until dialogueArray.length()) {
                val line = dialogueArray.getJSONObject(i)
                dialogue.add(
                    DialogueLine(
                        role = line.getString("role"),
                        text = line.getString("text"),
                        isQuestion = line.optBoolean("isQuestion", false),
                        keywords = line.optJSONArray("keywords")?.let { arr ->
                            (0 until arr.length()).map { arr.getString(it) }
                        } ?: emptyList()
                    )
                )
            }

            val keywords = json.optJSONArray("keywords")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()

            val tips = json.optString("tips", "")
            val warnings = json.optJSONArray("warnings")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()

            ScriptContent(
                dialogue = dialogue,
                keywords = keywords,
                tips = tips,
                warnings = warnings
            )
        } catch (e: Exception) {
            ScriptContent(emptyList(), emptyList(), "", emptyList())
        }
    }

    /**
     * 获取下一回合话术
     */
    fun getNextLine(scriptContent: ScriptContent, currentRound: Int): DialogueLine? {
        return if (currentRound < scriptContent.dialogue.size) {
            scriptContent.dialogue[currentRound]
        } else {
            null
        }
    }

    /**
     * 获取诈骗方话术列表
     */
    fun getScammerLines(scriptContent: ScriptContent): List<DialogueLine> {
        return scriptContent.dialogue.filter { it.role == "scammer" }
    }

    /**
     * 获取用户需要识别的关键词
     */
    fun getKeyPhrasesToIdentify(scriptContent: ScriptContent): List<String> {
        return scriptContent.keywords + scriptContent.dialogue
            .filter { it.isQuestion }
            .flatMap { it.keywords }
    }

    /**
     * 计算识别准确度
     */
    fun calculateIdentificationScore(
        identifiedKeywords: List<String>,
        expectedKeywords: List<String>
    ): IdentificationScore {
        if (expectedKeywords.isEmpty()) {
            return IdentificationScore(100, 0, 0, emptyList(), emptyList())
        }

        val correctlyIdentified = identifiedKeywords.filter { it in expectedKeywords }
        val missed = expectedKeywords.filter { it !in identifiedKeywords }
        val falsePositives = identifiedKeywords.filter { it !in expectedKeywords }

        val accuracy = if (expectedKeywords.isNotEmpty()) {
            (correctlyIdentified.size.toFloat() / expectedKeywords.size * 100).toInt()
        } else {
            100
        }

        return IdentificationScore(
            accuracy = accuracy.coerceIn(0, 100),
            correctlyIdentified = correctlyIdentified.size,
            totalExpected = expectedKeywords.size,
            missedKeywords = missed,
            falsePositives = falsePositives
        )
    }

    /**
     * 生成训练反馈
     */
    fun generateFeedback(
        score: IdentificationScore,
        scriptContent: ScriptContent
    ): TrainingFeedback {
        val overallScore = score.accuracy

        val performanceLevel = when {
            overallScore >= 90 -> PerformanceLevel.EXCELLENT
            overallScore >= 70 -> PerformanceLevel.GOOD
            overallScore >= 50 -> PerformanceLevel.FAIR
            else -> PerformanceLevel.POOR
        }

        val reward = calculateReward(overallScore)

        val feedbackMessage = when (performanceLevel) {
            PerformanceLevel.EXCELLENT -> "太棒了！您成功识别了所有诈骗关键词，防骗意识很强！"
            PerformanceLevel.GOOD -> "做得很好！您识别了大部分诈骗关键词，继续保持警惕！"
            PerformanceLevel.FAIR -> "还不错，但有些关键词没有识别出来，建议加强学习。"
            PerformanceLevel.POOR -> "需要加油了，建议多练习几次，提高防骗意识。"
        }

        val learningPoints = mutableListOf<String>()
        if (score.missedKeywords.isNotEmpty()) {
            learningPoints.add("需要注意的关键词：${score.missedKeywords.joinToString("、")}")
        }
        if (scriptContent.tips.isNotEmpty()) {
            learningPoints.add("防骗技巧：${scriptContent.tips}")
        }

        return TrainingFeedback(
            score = overallScore,
            performanceLevel = performanceLevel,
            reward = reward,
            feedbackMessage = feedbackMessage,
            learningPoints = learningPoints,
            missedKeywords = score.missedKeywords,
            tips = scriptContent.tips
        )
    }

    /**
     * 计算奖励积分
     */
    private fun calculateReward(score: Int): Double {
        return when {
            score >= 90 -> 0.2  // 优秀：0.2元
            score >= 70 -> 0.15  // 良好：0.15元
            score >= 50 -> 0.1  // 及格：0.1元
            else -> 0.05  // 不及格：0.05元
        }
    }

    /**
     * 保存训练记录
     */
    suspend fun saveTrainingRecord(
        userId: Long,
        scriptId: Long,
        score: Int,
        identifiedKeywords: List<String>,
        reward: Double
    ): Long = withContext(Dispatchers.IO) {
        val record = com.yindun.shouhu.data.local.entity.TrainingRecordEntity(
            userId = userId,
            scriptId = scriptId,
            startTime = System.currentTimeMillis() - 60000, // 假设训练时长1分钟
            endTime = System.currentTimeMillis(),
            score = score,
            identifiedKeywords = JSONArray(identifiedKeywords).toString(),
            rewardAmount = reward,
            completed = true
        )
        database.trainingRecordDao().insertRecord(record)
    }

    /**
     * 获取用户训练历史
     */
    fun getTrainingHistory(userId: Long): Flow<List<com.yindun.shouhu.data.local.entity.TrainingRecordEntity>> {
        return database.trainingRecordDao().getTrainingHistory(userId)
    }

    /**
     * 获取用户总奖励
     */
    suspend fun getTotalRewards(userId: Long): Double {
        return database.trainingRecordDao().getTotalRewards(userId) ?: 0.0
    }

    /**
     * 获取用户完成训练次数
     */
    suspend fun getCompletedCount(userId: Long): Int {
        return database.trainingRecordDao().getCompletedCount(userId)
    }

    /**
     * 获取随机话术（根据分类和难度）
     */
    suspend fun getRandomScript(
        category: String? = null,
        difficulty: Int? = null
    ): FraudScriptEntity? = withContext(Dispatchers.IO) {
        val scripts = if (category != null) {
            database.fraudScriptDao().getScriptsByCategory(category).first()
        } else {
            // 获取所有话术
            database.fraudScriptDao().getAllCategories().first().flatMap { cat ->
                database.fraudScriptDao().getScriptsByCategory(cat).first()
            }
        }

        val filtered = if (difficulty != null) {
            scripts.filter { it.riskLevel == difficulty }
        } else {
            scripts
        }

        filtered.randomOrNull()
    }

    companion object {
        @Volatile
        private var instance: ScriptEngine? = null

        fun getInstance(context: Context): ScriptEngine {
            return instance ?: synchronized(this) {
                instance ?: ScriptEngine(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 对话内容
 */
data class DialogueLine(
    val role: String,  // "scammer" 或 "user"
    val text: String,
    val isQuestion: Boolean = false,
    val keywords: List<String> = emptyList()
)

/**
 * 解析后的话术内容
 */
data class ScriptContent(
    val dialogue: List<DialogueLine>,
    val keywords: List<String>,
    val tips: String,
    val warnings: List<String>
)

/**
 * 识别得分
 */
data class IdentificationScore(
    val accuracy: Int,
    val correctlyIdentified: Int,
    val totalExpected: Int,
    val missedKeywords: List<String>,
    val falsePositives: List<String>
)

/**
 * 表现等级
 */
enum class PerformanceLevel {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

/**
 * 训练反馈
 */
data class TrainingFeedback(
    val score: Int,
    val performanceLevel: PerformanceLevel,
    val reward: Double,
    val feedbackMessage: String,
    val learningPoints: List<String>,
    val missedKeywords: List<String>,
    val tips: String
)
