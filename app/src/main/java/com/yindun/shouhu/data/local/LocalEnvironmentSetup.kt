package com.yindun.shouhu.data.local

import android.content.Context
import com.yindun.shouhu.data.local.entity.*
import com.yindun.shouhu.util.CryptoManager
import com.yindun.shouhu.util.VoiceResourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * 本地环境配置器
 * 确保所有必要的环境和数据都内置在应用中
 */
class LocalEnvironmentSetup(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }
    private val cryptoManager by lazy { CryptoManager.getInstance(context) }
    private val voiceResourceManager by lazy { VoiceResourceManager.getInstance(context) }

    /**
     * 完整的本地环境初始化
     */
    suspend fun setupCompleteEnvironment() = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("EnvironmentSetup", "开始配置本地环境...")

            // 1. 创建必要的目录结构
            createDirectoryStructure()

            // 2. 初始化数据库
            initializeDatabase()

            // 3. 预置用户数据
            presetUserData()

            // 4. 预置风控规则
            presetRiskRules()

            // 5. 预置诈骗话术库
            presetFraudScripts()

            // 6. 预置风险号码库
            presetPhoneRiskDatabase()

            // 7. 预置训练数据
            presetTrainingData()

            // 8. 初始化加密密钥
            initializeCryptoKeys()

            // 9. 创建本地配置文件
            createLocalConfig()

            // 10. 验证环境完整性
            verifyEnvironment()

            android.util.Log.d("EnvironmentSetup", "本地环境配置完成")
            true
        } catch (e: Exception) {
            android.util.Log.e("EnvironmentSetup", "环境配置失败", e)
            false
        }
    }

    /**
     * 创建目录结构
     */
    private fun createDirectoryStructure() {
        val directories = listOf(
            "models",
            "databases",
            "backups",
            "logs",
            "cache",
            "audio",
            "scripts",
            "evidence"
        )

        directories.forEach { dir ->
            File(context.filesDir, dir).mkdirs()
            File(context.cacheDir, dir).mkdirs()
        }
    }

    /**
     * 初始化数据库
     */
    private suspend fun initializeDatabase() {
        // 触发数据库创建
        database.openHelper.readableDatabase
        android.util.Log.d("EnvironmentSetup", "数据库初始化完成")
    }

    /**
     * 预置用户数据
     */
    private suspend fun presetUserData() {
        // 默认用户
        val defaultUser = UserEntity(
            id = 1,
            name = "用户",
            phone = "13800138000",
            idNumber = cryptoManager.encrypt("000000000000000000"),
            isElderly = true
        )
        database.userDao().insertUser(defaultUser)

        // 积分账户
        val pointsAccount = PointsAccountEntity(
            userId = 1,
            balance = 10.0, // 初始赠送10元
            totalEarned = 10.0,
            totalSpent = 0.0
        )
        database.pointsAccountDao().insertAccount(pointsAccount)

        android.util.Log.d("EnvironmentSetup", "用户数据预置完成")
    }

    /**
     * 预置风控规则
     */
    private suspend fun presetRiskRules() {
        val rules = listOf(
            // 大额转账规则
            RiskRuleEntity(
                ruleName = "large_single_transfer",
                ruleType = "amount_threshold",
                parameters = JSONObject().apply {
                    put("threshold", 10000)
                    put("operator", ">=")
                }.toString(),
                riskLevel = 7,
                isActive = true
            ),
            // 频繁小额转账规则
            RiskRuleEntity(
                ruleName = "frequent_small_transfer",
                ruleType = "frequency",
                parameters = JSONObject().apply {
                    put("minAmount", 100)
                    put("maxAmount", 1000)
                    put("count", 5)
                    put("timeWindowMinutes", 30)
                }.toString(),
                riskLevel = 8,
                isActive = true
            ),
            // 深夜交易规则
            RiskRuleEntity(
                ruleName = "night_transaction",
                ruleType = "time_based",
                parameters = JSONObject().apply {
                    put("startHour", 23)
                    put("endHour", 5)
                    put("minAmount", 500)
                }.toString(),
                riskLevel = 5,
                isActive = true
            ),
            // 向陌生人转账规则
            RiskRuleEntity(
                ruleName = "new_recipient_large_amount",
                ruleType = "pattern",
                parameters = JSONObject().apply {
                    put("threshold", 5000)
                    put("isNewRecipient", true)
                }.toString(),
                riskLevel = 8,
                isActive = true
            ),
            // 连续转账规则
            RiskRuleEntity(
                ruleName = "rapid_successive_transfers",
                ruleType = "frequency",
                parameters = JSONObject().apply {
                    put("count", 3)
                    put("timeWindowMinutes", 10)
                    put("sameAmount", true)
                }.toString(),
                riskLevel = 9,
                isActive = true
            ),
            // 高频交易规则
            RiskRuleEntity(
                ruleName = "high_frequency_transaction",
                ruleType = "frequency",
                parameters = JSONObject().apply {
                    put("count", 20)
                    put("timeWindowMinutes", 60)
                }.toString(),
                riskLevel = 7,
                isActive = true
            )
        )

        rules.forEach { rule ->
            database.riskRuleDao().insertRule(rule)
        }

        android.util.Log.d("EnvironmentSetup", "风控规则预置完成")
    }

    /**
     * 预置诈骗话术库
     */
    private suspend fun presetFraudScripts() {
        val scripts = listOf(
            createHealthProductScript(),
            createInvestmentScript(),
            createImpersonateScript(),
            createLotteryScript(),
            createFamilyEmergencyScript(),
            createCustomerServiceScript(),
            createLoanScript(),
            createRefundScript()
        )

        scripts.forEach { script ->
            database.fraudScriptDao().insertScript(script)
        }

        android.util.Log.d("EnvironmentSetup", "诈骗话术库预置完成")
    }

    /**
     * 预置风险号码库
     */
    private suspend fun presetPhoneRiskDatabase() {
        val riskPhones = listOf(
            PhoneRiskEntity(phoneNumber = "17051234567", riskType = "impersonate", reportCount = 156),
            PhoneRiskEntity(phoneNumber = "17198765432", riskType = "investment", reportCount = 89),
            PhoneRiskEntity(phoneNumber = "16211112222", riskType = "customer_service", reportCount = 67),
            PhoneRiskEntity(phoneNumber = "19233334444", riskType = "lottery", reportCount = 45),
            PhoneRiskEntity(phoneNumber = "14566667777", riskType = "health_product", reportCount = 34),
            PhoneRiskEntity(phoneNumber = "14788889999", riskType = "family_emergency", reportCount = 23),
            PhoneRiskEntity(phoneNumber = "13100001111", riskType = "loan_fraud", reportCount = 78),
            PhoneRiskEntity(phoneNumber = "13200002222", riskType = "refund_fraud", reportCount = 56),
            PhoneRiskEntity(phoneNumber = "15000003333", riskType = "impersonate", reportCount = 134),
            PhoneRiskEntity(phoneNumber = "15100004444", riskType = "investment", reportCount = 98)
        )

        riskPhones.forEach { phone ->
            database.phoneRiskDao().insertOrUpdate(phone)
        }

        android.util.Log.d("EnvironmentSetup", "风险号码库预置完成")
    }

    /**
     * 预置训练数据
     */
    private suspend fun presetTrainingData() {
        // 创建默认训练记录
        val trainingRecord = TrainingRecordEntity(
            userId = 1,
            scriptId = 1,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis(),
            score = 85,
            identifiedKeywords = JSONArray(listOf("免费体检", "特效药")).toString(),
            rewardAmount = 0.15,
            completed = true
        )
        database.trainingRecordDao().insertRecord(trainingRecord)

        android.util.Log.d("EnvironmentSetup", "训练数据预置完成")
    }

    /**
     * 初始化加密密钥
     */
    private fun initializeCryptoKeys() {
        // 确保密钥已生成
        try {
            cryptoManager.encrypt("test")
            android.util.Log.d("EnvironmentSetup", "加密密钥初始化完成")
        } catch (e: Exception) {
            android.util.Log.e("EnvironmentSetup", "加密密钥初始化失败", e)
        }
    }

    /**
     * 创建本地配置文件
     */
    private fun createLocalConfig() {
        try {
            val configFile = File(context.filesDir, "app_config.json")
            if (!configFile.exists()) {
                val config = JSONObject().apply {
                    put("version", "1.0.0")
                    put("firstRun", true)
                    put("lastSyncTime", 0)
                    put("modelVersion", 1)
                    put("dataVersion", 1)
                }
                configFile.writeText(config.toString())
            }
            android.util.Log.d("EnvironmentSetup", "本地配置文件创建完成")
        } catch (e: Exception) {
            android.util.Log.e("EnvironmentSetup", "配置文件创建失败", e)
        }
    }

    /**
     * 验证环境完整性
     */
    private fun verifyEnvironment(): Boolean {
        val checks = mutableListOf<Boolean>()

        // 检查数据库
        checks.add(database.openHelper.readableDatabase.isOpen)

        // 检查配置文件
        checks.add(File(context.filesDir, "app_config.json").exists())

        // 检查目录结构
        checks.add(File(context.filesDir, "models").exists())
        checks.add(File(context.filesDir, "backups").exists())

        val allPassed = checks.all { it }
        android.util.Log.d("EnvironmentSetup", "环境验证结果: $allPassed")
        return allPassed
    }

    // ==================== 话术内容生成方法 ====================

    private fun createHealthProductScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "health_product",
            title = "免费体检诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "您好，我们是XX健康管理中心，现在有免费体检活动", "keywords": ["免费体检"]},
                        {"role": "scammer", "text": "您的检查结果显示身体有严重问题，需要购买特效药", "keywords": ["特效药"]},
                        {"role": "scammer", "text": "这个药效果很好，您可以先付定金试试", "keywords": ["定金"]}
                    ],
                    "tips": "正规医疗机构不会在街头推销，体检应去正规医院",
                    "keywords": ["免费体检", "特效药", "定金"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("免费体检", "特效药", "定金")).toString(),
            riskLevel = 5,
            dialect = "mandarin"
        )
    }

    private fun createInvestmentScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "investment",
            title = "高额回报诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "我们有一个非常好的投资项目", "keywords": ["投资项目"]},
                        {"role": "scammer", "text": "月收益30%，稳赚不赔", "keywords": ["高额回报", "稳赚不赔"]},
                        {"role": "scammer", "text": "这是内部消息，不能告诉别人", "keywords": ["内部消息", "保密"]}
                    ],
                    "tips": "投资有风险，承诺高回报的都是诈骗",
                    "keywords": ["投资项目", "高额回报", "稳赚不赔", "内部消息"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("投资项目", "高额回报", "稳赚不赔", "内部消息")).toString(),
            riskLevel = 7,
            dialect = "mandarin"
        )
    }

    private fun createImpersonateScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "impersonate",
            title = "冒充警察诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "我是XX公安局的，您涉嫌洗钱案件", "keywords": ["公安局", "涉嫌"]},
                        {"role": "scammer", "text": "您的账户已被冻结，需要配合调查", "keywords": ["冻结", "配合调查"]},
                        {"role": "scammer", "text": "请将资金转到安全账户进行核查", "keywords": ["安全账户"]}
                    ],
                    "tips": "公检法不会电话办案，更不会要求转账到安全账户",
                    "keywords": ["公安局", "涉嫌", "冻结", "配合调查", "安全账户"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("公安局", "涉嫌", "冻结", "配合调查", "安全账户")).toString(),
            riskLevel = 9,
            dialect = "mandarin"
        )
    }

    private fun createLotteryScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "lottery",
            title = "中奖通知诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "恭喜您中了特等奖，奖品价值100万", "keywords": ["中奖", "特等奖"]},
                        {"role": "scammer", "text": "请您先缴纳20%的个人所得税", "keywords": ["个人所得税"]},
                        {"role": "scammer", "text": "缴税后我们立即发放奖品", "keywords": ["缴税"]}
                    ],
                    "tips": "正规抽奖不会要求先交税或手续费",
                    "keywords": ["中奖", "特等奖", "个人所得税", "缴税"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("中奖", "特等奖", "个人所得税", "缴税")).toString(),
            riskLevel = 6,
            dialect = "mandarin"
        )
    }

    private fun createFamilyEmergencyScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "family_emergency",
            title = "冒充子女诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "妈，是我，我出事了", "keywords": ["出事了"]},
                        {"role": "scammer", "text": "急需用钱赔偿，不然要坐牢", "keywords": ["急需用钱"]},
                        {"role": "scammer", "text": "您快点转账，先不要告诉别人", "keywords": ["快转账", "不要告诉别人"]}
                    ],
                    "tips": "接到此类电话应先挂断，主动联系子女确认",
                    "keywords": ["出事了", "急需用钱", "快转账", "不要告诉别人"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("出事了", "急需用钱", "快转账", "不要告诉别人")).toString(),
            riskLevel = 8,
            dialect = "mandarin"
        )
    }

    private fun createCustomerServiceScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "customer_service",
            title = "冒充客服诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "我是XX电商平台客服，您购买的商品有质量问题", "keywords": ["质量问题"]},
                        {"role": "scammer", "text": "我们要给您退款和三倍赔偿", "keywords": ["退款", "赔偿"]},
                        {"role": "scammer", "text": "请提供银行卡号和验证码", "keywords": ["银行卡号", "验证码"]}
                    ],
                    "tips": "正规客服不会索要验证码，退款应通过官方渠道",
                    "keywords": ["质量问题", "退款", "银行卡号", "验证码"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("质量问题", "退款", "银行卡号", "验证码")).toString(),
            riskLevel = 6,
            dialect = "mandarin"
        )
    }

    private fun createLoanScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "loan_fraud",
            title = "低息贷款诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "我们提供低息贷款，无需抵押", "keywords": ["低息贷款", "无抵押"]},
                        {"role": "scammer", "text": "审核通过后可以秒到账", "keywords": ["秒到账"]},
                        {"role": "scammer", "text": "您需要先缴纳保证金才能放款", "keywords": ["保证金"]}
                    ],
                    "tips": "正规贷款不会要求先交保证金",
                    "keywords": ["低息贷款", "无抵押", "秒到账", "保证金"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("低息贷款", "无抵押", "秒到账", "保证金")).toString(),
            riskLevel = 7,
            dialect = "mandarin"
        )
    }

    private fun createRefundScript(): FraudScriptEntity {
        return FraudScriptEntity(
            category = "refund_fraud",
            title = "电商退款诈骗",
            scriptContent = """
                {
                    "dialogue": [
                        {"role": "scammer", "text": "您之前购买的商品可以申请退款", "keywords": ["退款"]},
                        {"role": "scammer", "text": "我们支持刷单返现", "keywords": ["刷单返现"]},
                        {"role": "scammer", "text": "您只需要先转账，我们马上返现", "keywords": ["转账", "返现"]}
                    ],
                    "tips": "淘宝退款应通过官方渠道进行，不要相信刷单返现",
                    "keywords": ["退款", "刷单返现", "转账", "返现"]
                }
            """.trimIndent(),
            keywords = JSONArray(listOf("退款", "刷单返现", "转账", "返现")).toString(),
            riskLevel = 6,
            dialect = "mandarin"
        )
    }

    companion object {
        @Volatile
        private var instance: LocalEnvironmentSetup? = null

        fun getInstance(context: Context): LocalEnvironmentSetup {
            return instance ?: synchronized(this) {
                instance ?: LocalEnvironmentSetup(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
