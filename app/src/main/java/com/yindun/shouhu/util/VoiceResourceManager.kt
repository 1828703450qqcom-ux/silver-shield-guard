package com.yindun.shouhu.util

import android.content.Context

/**
 * 语音资源管理器
 * 管理内置的TTS语音资源
 */
class VoiceResourceManager(private val context: Context) {

    /**
     * 预置的语音提示文本
     */
    val voicePrompts = mapOf(
        // 通用提示
        "welcome" to "欢迎使用银盾守护，您的安全我们守护",
        "start_monitoring" to "已开始监控，正在保护您的安全",
        "stop_monitoring" to "监控已停止",
        "permission_required" to "需要权限才能继续使用此功能",

        // 风险提醒
        "risk_low" to "注意，检测到陌生来电，请谨慎接听",
        "risk_medium" to "警告，该号码存在风险，请注意防范",
        "risk_high" to "警告！该号码疑似诈骗电话，请勿透露个人信息",
        "risk_critical" to "紧急警告！该号码是高风险诈骗号码，请立即挂断",

        // 财务监控
        "transaction_safe" to "交易安全，金额正常",
        "transaction_warning" to "注意，检测到异常交易，请确认是否为本人操作",
        "transaction_danger" to "警告，检测到可疑交易，建议暂停操作",
        "large_transfer" to "检测到大额转账，请确认收款方信息",

        // 训练相关
        "training_start" to "训练开始，请仔细听诈骗话术",
        "training_round" to "第{}回合，请注意识别关键词",
        "training_complete" to "训练完成，做得很好",
        "training_reward" to "恭喜，获得应用内模拟积分%s分",
        "keyword_detected" to "识别到关键词：{}",
        "keyword_missed" to "注意，遗漏了关键词：{}",

        // 案件相关
        "case_submitted" to "材料已在本机保存，请通过官方渠道核实提交方式",
        "case_processing" to "请通过官方渠道查询处理进度",
        "case_resolved" to "请通过官方渠道核实处理结果",

        // 系统提示
        "network_error" to "网络连接失败，正在使用离线模式",
        "model_loading" to "正在加载AI模型，请稍候",
        "sync_complete" to "数据同步完成",
        "backup_complete" to "数据备份完成",

        // 来电防护
        "incoming_call" to "来电提醒",
        "outgoing_warning" to "您正在拨打的号码存在风险，请确认",
        "call_blocked" to "来电已拦截，疑似诈骗电话",

        // 操作确认
        "confirm_delete" to "确认要删除吗？此操作不可恢复",
        "confirm_submit" to "确认提交吗？",
        "confirm_bind" to "确认绑定该账户吗？",
        "confirm_unbind" to "确认解绑该账户吗？",

        // 错误提示
        "error_general" to "操作失败，请稍后重试",
        "error_network" to "网络错误，请检查网络连接",
        "error_permission" to "权限不足，请在设置中授权",
        "error_model" to "AI模型加载失败，使用规则引擎"
    )

    /**
     * 诈骗类型名称映射
     */
    val fraudTypeNames = mapOf(
        "health_product" to "保健品诈骗",
        "investment" to "投资理财诈骗",
        "impersonate" to "冒充公检法",
        "lottery" to "中奖诈骗",
        "family_emergency" to "冒充亲属紧急情况",
        "customer_service" to "冒充客服",
        "loan_fraud" to "贷款诈骗",
        "refund_fraud" to "退款诈骗",
        "prize_fraud" to "领奖诈骗",
        "normal" to "正常"
    )

    /**
     * 风险等级名称
     */
    val riskLevelNames = mapOf(
        0 to "安全",
        1 to "极低风险",
        2 to "低风险",
        3 to "低风险",
        4 to "中低风险",
        5 to "中风险",
        6 to "中高风险",
        7 to "高风险",
        8 to "高风险",
        9 to "极高风险",
        10 to "危险"
    )

    /**
     * 训练反馈语音
     */
    val trainingFeedbacks = mapOf(
        "excellent" to "太棒了！您成功识别了所有诈骗关键词，防骗意识很强！",
        "good" to "做得很好！您识别了大部分诈骗关键词，继续保持警惕！",
        "fair" to "还不错，但有些关键词没有识别出来，建议加强学习。",
        "poor" to "需要加油了，建议多练习几次，提高防骗意识。"
    )

    /**
     * 诈骗话术关键词警示
     */
    val keywordWarnings = mapOf(
        "免费体检" to "正规医疗机构不会在街头推销免费体检",
        "特效药" to "药品需要医生处方，不要相信所谓的特效药",
        "定金" to "正规交易不会要求先付定金",
        "高额回报" to "投资有风险，承诺高回报的都是诈骗",
        "稳赚不赔" to "没有稳赚不赔的投资，这是诈骗",
        "内部消息" to "利用内部消息炒股是违法行为",
        "安全账户" to "没有所谓的安全账户，这是冒充公检法诈骗",
        "配合调查" to "公检法不会电话办案，更不会要求转账",
        "不能告诉别人" to "骗子害怕你告诉别人，所以要求保密",
        "中奖" to "没有参加过抽奖却中奖，这是诈骗",
        "个人所得税" to "中奖不会要求先交税",
        "出事了" to "接到亲属出事的电话，应先挂断确认",
        "急需用钱" to "不要在电话中透露家庭财务信息",
        "快转账" to "不要向陌生账户转账",
        "验证码" to "验证码是银行安全措施，不要告诉任何人",
        "银行卡号" to "不要向陌生人透露银行卡号",
        "退款" to "退款应通过官方渠道，不要相信陌生来电",
        "保证金" to "正规贷款不会要求先交保证金"
    )

    /**
     * 获取语音提示文本
     */
    fun getPrompt(key: String, vararg args: Any): String {
        val template = voicePrompts[key] ?: return key
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            template
        }
    }

    /**
     * 获取诈骗类型中文名
     */
    fun getFraudTypeName(type: String): String {
        return fraudTypeNames[type] ?: type
    }

    /**
     * 获取风险等级名称
     */
    fun getRiskLevelName(level: Int): String {
        return riskLevelNames[level] ?: "未知"
    }

    /**
     * 获取关键词警示
     */
    fun getKeywordWarning(keyword: String): String {
        return keywordWarnings[keyword] ?: "注意防范此关键词"
    }

    /**
     * 获取训练反馈
     */
    fun getTrainingFeedback(level: String): String {
        return trainingFeedbacks[level] ?: "训练完成"
    }

    /**
     * 生成来电风险播报文本
     */
    fun generateCallAlertText(phoneNumber: String, riskLevel: Int, fraudType: String? = null): String {
        val riskName = getRiskLevelName(riskLevel)
        val typeName = fraudType?.let { getFraudTypeName(it) } ?: ""

        return when {
            riskLevel >= 8 -> {
                "紧急警告！检测到高风险诈骗电话，号码${phoneNumber}${
                    if (typeName.isNotEmpty()) "，疑似${typeName}" else ""
                }，请勿接听！"
            }
            riskLevel >= 6 -> {
                "警告，检测到可疑电话，号码${phoneNumber}${
                    if (typeName.isNotEmpty()) "，可能涉及${typeName}" else ""
                }，请谨慎接听。"
            }
            riskLevel >= 4 -> {
                "注意，检测到陌生号码${phoneNumber}，请注意防范。"
            }
            else -> {
                "来电号码${phoneNumber}，风险等级：${riskName}"
            }
        }
    }

    /**
     * 生成交易风险播报文本
     */
    fun generateTransactionAlertText(
        amount: Double,
        counterparty: String,
        riskLevel: Int,
        anomalyType: String? = null
    ): String {
        val riskName = getRiskLevelName(riskLevel)

        return when {
            riskLevel >= 8 -> {
                "紧急警告！检测到高风险交易：金额${amount}元，收款方${counterparty}${
                    if (!anomalyType.isNullOrEmpty()) "，异常类型：${anomalyType}" else ""
                }，建议立即停止操作！"
            }
            riskLevel >= 6 -> {
                "警告，检测到异常交易：金额${amount}元，收款方${counterparty}，请确认是否为本人操作。"
            }
            riskLevel >= 4 -> {
                "注意，交易金额${amount}元，请确认收款方信息。"
            }
            else -> {
                "交易安全，金额${amount}元，收款方${counterparty}"
            }
        }
    }

    /**
     * 生成训练结果播报文本
     */
    fun generateTrainingResultText(score: Int, reward: Double): String {
        val feedbackLevel = when {
            score >= 90 -> "excellent"
            score >= 70 -> "good"
            score >= 50 -> "fair"
            else -> "poor"
        }

        val feedback = getTrainingFeedback(feedbackLevel)
        val rewardText = if (reward > 0) "，获得应用内模拟积分${reward}分" else ""

        return "训练完成，得分${score}分。${feedback}${rewardText}"
    }

    companion object {
        @Volatile
        private var instance: VoiceResourceManager? = null

        fun getInstance(context: Context): VoiceResourceManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceResourceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
