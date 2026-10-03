package com.yindun.shouhu.domain.engine

import android.content.Context
import com.yindun.shouhu.data.local.entity.FraudScriptEntity
import com.yindun.shouhu.data.local.entity.PointsAccountEntity
import com.yindun.shouhu.data.local.entity.TrainingRecordEntity
import com.yindun.shouhu.util.SpeechManager
import com.yindun.shouhu.util.RecognitionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * 训练会话管理器
 * 管理一次完整的防骗训练流程
 */
class TrainingSession(private val context: Context) {

    private val scriptEngine by lazy { ScriptEngine.getInstance(context) }
    private val speechManager by lazy { SpeechManager.getInstance(context) }

    private val database by lazy {
        com.yindun.shouhu.data.local.AppDatabase.getDatabase(context)
    }

    private val _sessionState = MutableStateFlow<TrainingSessionState>(TrainingSessionState.Idle)
    val sessionState: StateFlow<TrainingSessionState> = _sessionState.asStateFlow()

    private val _currentRound = MutableStateFlow(0)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _dialogueHistory = MutableStateFlow<List<DialogueItem>>(emptyList())
    val dialogueHistory: StateFlow<List<DialogueItem>> = _dialogueHistory.asStateFlow()

    private val _identifiedKeywords = MutableStateFlow<List<String>>(emptyList())
    val identifiedKeywords: StateFlow<List<String>> = _identifiedKeywords.asStateFlow()

    private var currentScript: FraudScriptEntity? = null
    private var scriptContent: ScriptContent? = null
    private var userId: Long = -1
    private var startTime: Long = 0

    // 识别到的关键词缓存
    private val identifiedKeywordsCache = mutableSetOf<String>()

    /**
     * 开始训练会话
     */
    suspend fun startSession(
        userId: Long,
        scriptId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            this@TrainingSession.userId = userId
            this@TrainingSession.startTime = System.currentTimeMillis()

            // 加载话术
            val script = scriptEngine.getScriptById(scriptId)
            if (script == null) {
                _sessionState.value = TrainingSessionState.Error("话术加载失败")
                return@withContext false
            }

            currentScript = script
            scriptContent = scriptEngine.parseScriptContent(script.scriptContent)

            // 重置状态
            _currentRound.value = 0
            _dialogueHistory.value = emptyList()
            identifiedKeywordsCache.clear()
            _identifiedKeywords.value = emptyList()

            // 开始训练
            _sessionState.value = TrainingSessionState.Starting
            delay(1000)

            // 播放开场白
            val welcomeMessage = "欢迎开始${script.title}防骗训练。请仔细听诈骗分子的话术，识别其中的关键词。"
            speechManager.speak(welcomeMessage, "welcome")

            delay(3000)

            // 进入训练状态
            _sessionState.value = TrainingSessionState.InProgress
            startNextRound()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            _sessionState.value = TrainingSessionState.Error("会话启动失败: ${e.message}")
            false
        }
    }

    /**
     * 开始下一回合
     */
    private suspend fun startNextRound() {
        val content = scriptContent ?: return
        val round = _currentRound.value

        // 获取诈骗方下一句话
        val scammerLine = scriptEngine.getNextLine(content, round)
        if (scammerLine == null) {
            // 训练结束
            finishSession()
            return
        }

        // 更新状态
        _sessionState.value = TrainingSessionState.WaitingForUserInput(
            expectedKeywords = scammerLine.keywords,
            questionText = scammerLine.text
        )

        // 播放诈骗话术
        _dialogueHistory.value = _dialogueHistory.value + DialogueItem(
            role = "scammer",
            text = scammerLine.text,
            timestamp = System.currentTimeMillis()
        )

        speechManager.speak(scammerLine.text, "scammer_line_$round")

        // 等待用户识别关键词
        delay(1000)
        _sessionState.value = TrainingSessionState.ListeningForKeywords(
            expectedKeywords = scammerLine.keywords,
            timeLimit = 15000 // 15秒识别时间
        )
    }

    /**
     * 提交用户识别的关键词
     */
    suspend fun submitIdentifiedKeywords(
        keywords: List<String>
    ) = withContext(Dispatchers.Default) {
        // 记录识别的关键词
        identifiedKeywordsCache.addAll(keywords)
        _identifiedKeywords.value = identifiedKeywordsCache.toList()

        // 添加用户发言到对话历史
        val userText = if (keywords.isNotEmpty()) {
            "我识别到的关键词：${keywords.joinToString("、")}"
        } else {
            "未识别到关键词"
        }

        _dialogueHistory.value = _dialogueHistory.value + DialogueItem(
            role = "user",
            text = userText,
            timestamp = System.currentTimeMillis()
        )

        // 移动到下一回合
        _currentRound.value++
        delay(500)
        startNextRound()
    }

    /**
     * 结束会话
     */
    private suspend fun finishSession() {
        _sessionState.value = TrainingSessionState.Finishing

        // 计算得分
        val content = scriptContent ?: return
        val expectedKeywords = scriptEngine.getKeyPhrasesToIdentify(content)
        val identified = identifiedKeywordsCache.toList()

        val score = scriptEngine.calculateIdentificationScore(identified, expectedKeywords)
        val feedback = scriptEngine.generateFeedback(score, content)

        // 保存训练记录
        val recordId = scriptEngine.saveTrainingRecord(
            userId = userId,
            scriptId = currentScript?.id ?: 0,
            score = feedback.score,
            identifiedKeywords = identified,
            reward = feedback.reward
        )

        // 更新积分账户
        updatePointsAccount(feedback.reward)

        // 播放反馈语音
        speechManager.speak(feedback.feedbackMessage, "feedback")

        // 更新状态
        _sessionState.value = TrainingSessionState.Completed(
            recordId = recordId,
            score = feedback.score,
            performanceLevel = feedback.performanceLevel,
            reward = feedback.reward,
            feedback = feedback
        )
    }

    /**
     * 更新积分账户
     */
    private suspend fun updatePointsAccount(reward: Double) {
        try {
            val existingAccount = database.pointsAccountDao().getPointsAccount(userId)
            if (existingAccount != null) {
                database.pointsAccountDao().updateAccount(
                    existingAccount.copy(
                        balance = existingAccount.balance + reward,
                        totalEarned = existingAccount.totalEarned + reward,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                database.pointsAccountDao().insertAccount(
                    PointsAccountEntity(
                        userId = userId,
                        balance = reward,
                        totalEarned = reward,
                        totalSpent = 0.0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 暂停会话
     */
    fun pauseSession() {
        speechManager.stopSpeaking()
        _sessionState.value = TrainingSessionState.Paused
    }

    /**
     * 恢复会话
     */
    fun resumeSession() {
        if (_sessionState.value is TrainingSessionState.Paused) {
            _sessionState.value = TrainingSessionState.InProgress
            // 重新播放当前话术
            val currentDialogue = _dialogueHistory.value.lastOrNull()
            if (currentDialogue?.role == "scammer") {
                speechManager.speak(currentDialogue.text, "resume")
            }
        }
    }

    /**
     * 取消会话
     */
    fun cancelSession() {
        speechManager.stopSpeaking()
        identifiedKeywordsCache.clear()
        _sessionState.value = TrainingSessionState.Cancelled
    }

    /**
     * 释放资源
     */
    fun release() {
        speechManager.stopSpeaking()
    }
}

/**
 * 训练会话状态
 */
sealed class TrainingSessionState {
    data object Idle : TrainingSessionState()
    data object Starting : TrainingSessionState()
    data object InProgress : TrainingSessionState()
    data object Paused : TrainingSessionState()
    data object Finishing : TrainingSessionState()
    data object Cancelled : TrainingSessionState()

    data class WaitingForUserInput(
        val expectedKeywords: List<String>,
        val questionText: String
    ) : TrainingSessionState()

    data class ListeningForKeywords(
        val expectedKeywords: List<String>,
        val timeLimit: Long
    ) : TrainingSessionState()

    data class Completed(
        val recordId: Long,
        val score: Int,
        val performanceLevel: PerformanceLevel,
        val reward: Double,
        val feedback: TrainingFeedback
    ) : TrainingSessionState()

    data class Error(val message: String) : TrainingSessionState()
}

/**
 * 对话项
 */
data class DialogueItem(
    val role: String,  // "scammer" 或 "user"
    val text: String,
    val timestamp: Long
)
