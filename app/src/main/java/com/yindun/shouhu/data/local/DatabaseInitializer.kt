package com.yindun.shouhu.data.local

import android.content.Context
import com.yindun.shouhu.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 数据库初始化器
 * 负责初始化预置数据
 */
class DatabaseInitializer(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    /**
     * 初始化数据库
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            // 初始化风控规则
            initializeRiskRules()

            // 初始化诈骗话术库
            initializeFraudScripts()

            // 初始化风险号码库
            initializePhoneRiskDatabase()

            // 初始化系统设置
            initializeSystemSettings()

            android.util.Log.d("DatabaseInitializer", "数据库初始化完成")
        } catch (e: Exception) {
            android.util.Log.e("DatabaseInitializer", "数据库初始化失败", e)
        }
    }

    /**
     * 初始化风控规则
     */
    private suspend fun initializeRiskRules() {
        val rules = listOf(
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
    }

    /**
     * 初始化诈骗话术库
     */
    private suspend fun initializeFraudScripts() {
        val scripts = listOf(
            FraudScriptEntity(
                category = "health_product",
                title = "免费体检诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "您好，我们是XX健康管理中心的，现在有个免费体检活动，您有时间参加吗？", "keywords": ["免费体检"]},
                            {"role": "scammer", "text": "您的检查结果显示身体有严重问题，需要购买我们的特效药才能治愈。", "keywords": ["特效药"]},
                            {"role": "scammer", "text": "这个药效果很好，很多老人都治好了，您可以先付定金试试。", "keywords": ["定金"]},
                            {"role": "scammer", "text": "我们还可以提供上门服务，您只需要转账就可以。", "keywords": ["转账"]},
                            {"role": "scammer", "text": "名额有限，今天不买就没有了，您考虑一下。", "keywords": ["名额有限"]}
                        ],
                        "tips": "正规医疗机构不会在街头推销，体检应去正规医院。遇到推销保健品的情况，应先咨询家人或医生。",
                        "keywords": ["免费体检", "特效药", "定金", "转账", "名额有限"]
                    }
                """.trimIndent(),
                keywords = JSONArray(listOf("免费体检", "特效药", "定金", "转账", "名额有限")).toString(),
                riskLevel = 5,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "investment",
                title = "高额回报诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "您好，我这里是XX投资公司，我们有一个非常好的投资项目。", "keywords": ["投资项目"]},
                            {"role": "scammer", "text": "这个项目月收益30%，稳赚不赔，很多老客户都已经赚了几百万。", "keywords": ["高额回报", "稳赚不赔"]},
                            {"role": "scammer", "text": "您先投一点试试，保证能赚回来，我可以用人格担保。", "keywords": ["保证"]},
                            {"role": "scammer", "text": "这是内部消息，不能告诉别人，否则就失效了。", "keywords": ["内部消息", "保密"]},
                            {"role": "scammer", "text": "您今天转账的话，我们可以给您额外的优惠。", "keywords": ["转账"]}
                        ],
                        "tips": "投资有风险，承诺高回报的都是诈骗。不要相信'稳赚不赔'的投资，更不要把钱转给陌生人。",
                        "keywords": ["投资项目", "高额回报", "稳赚不赔", "内部消息", "保密", "转账"]
                    }
                """.trimIndent(),
                keywords = JSONArray(listOf("投资项目", "高额回报", "稳赚不赔", "内部消息", "保密", "转账")).toString(),
                riskLevel = 7,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "impersonate",
                title = "冒充警察诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "您好，我是XX公安局的张警官，您涉嫌一起洗钱案件。", "keywords": ["公安局", "涉嫌"]},
                            {"role": "scammer", "text": "您的账户已被冻结，需要配合我们调查。", "keywords": ["冻结", "配合调查"]},
                            {"role": "scammer", "text": "为了证明您的清白，请将资金转到我们的安全账户进行核查。", "keywords": ["安全账户"]},
                            {"role": "scammer", "text": "这个案件是保密的，不能告诉任何人，否则会影响调查。", "keywords": ["保密", "不能告诉别人"]},
                            {"role": "scammer", "text": "您必须在今天之内完成转账，否则将被逮捕。", "keywords": ["今天之内", "逮捕"]}
                        ],
                        "tips": "公检法不会电话办案，更不会要求转账到'安全账户'。遇到此类电话，应立即挂断并拨打110核实。",
                        "keywords": ["公安局", "涉嫌", "冻结", "配合调查", "安全账户", "保密", "逮捕"]
                    }
                """.trimIndent(),
                keywords = JSONArray(listOf("公安局", "涉嫌", "冻结", "配合调查", "安全账户", "保密", "逮捕")).toString(),
                riskLevel = 9,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "lottery",
                title = "中奖通知诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "恭喜您！您中了我们公司的特等奖，奖品价值100万元！", "keywords": ["中奖", "特等奖"]},
                            {"role": "scammer", "text": "请您先缴纳20%的个人所得税，我们才能发放奖品。", "keywords": ["个人所得税"]},
                            {"role": "scammer", "text": "缴税后我们立即把奖品和奖金发给您。", "keywords": ["缴税"]},
                            {"role": "scammer", "text": "这个奖品很抢手，如果您不及时领取就会作废。", "keywords": ["及时领取", "作废"]}
                        ],
                        "tips": "正规抽奖不会要求先交税或手续费。中奖后应该通过官方渠道核实，不要轻信电话或短信通知。",
                        "keywords": ["中奖", "特等奖", "个人所得税", "缴税", "及时领取"]
                    }
                """.trimIndent(),
                keywords = JSONArray(listOf("中奖", "特等奖", "个人所得税", "缴税", "及时领取")).toString(),
                riskLevel = 6,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "family_emergency",
                title = "冒充子女诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "妈，是我，我出事了，现在在医院。", "keywords": ["出事了", "医院"]},
                            {"role": "scammer", "text": "我开车撞人了，急需用钱赔偿，不然就要坐牢。", "keywords": ["急需用钱", "赔偿"]},
                            {"role": "scammer", "text": "您快点转账，先不要告诉别人，我不想让太多人知道。", "keywords": ["快点转账", "不要告诉别人"]},
                            {"role": "scammer", "text": "您把钱转到这个账户，我马上就能出来。", "keywords": ["转账"]},
                            {"role": "scammer", "text": "妈，您快点，时间来不及了。", "keywords": ["快点", "来不及了"]}
                        ],
                        "tips": "接到此类电话应先挂断，主动联系子女确认。不要在电话中透露家庭财务信息，更不要立即转账。",
                        "keywords": ["出事了", "急需用钱", "不要告诉别人", "转账", "快点"]
                    }
                """.trimIndent(),
                keywords = JSONArray(listOf("出事了", "急需用钱", "不要告诉别人", "转账", "快点")).toString(),
                riskLevel = 8,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "customer_service",
                title = "冒充客服诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "您好，我是XX电商平台的客服，您之前购买的商品有质量问题。", "keywords": ["质量问题"]},
                            {"role": "scammer", "text": "我们要给您退款，还需要赔偿您三倍金额。", "keywords": ["退款", "赔偿"]},
                            {"role": "scammer", "text": "请您提供银行卡号和验证码，我们马上给您转账。", "keywords": ["银行卡号", "验证码"]},
                            {"role": "scammer", "text": "验证码发给您了吗？请告诉我验证码。", "keywords": ["验证码"]},
                            {"role": "scammer", "text": "转账需要一点时间，请您稍等。", "keywords": ["转账"]}
                        ],
                        "tips": "正规客服不会索要验证码，退款应通过官方渠道进行。不要相信陌生来电的退款通知。",
                        "keywords": ["质量问题", "退款", "银行卡号", "验证码", "转账"]
                    }
                """.trimIndent(),
                keywords = JSONArray(listOf("质量问题", "退款", "银行卡号", "验证码", "转账")).toString(),
                riskLevel = 6,
                dialect = "mandarin"
            )
        )

        scripts.forEach { script ->
            database.fraudScriptDao().insertScript(script)
        }
    }

    /**
     * 初始化风险号码库
     */
    private suspend fun initializePhoneRiskDatabase() {
        // 预置一些已知的诈骗号码
        val riskPhones = listOf(
            PhoneRiskEntity(phoneNumber = "17051234567", riskType = "impersonate", reportCount = 156),
            PhoneRiskEntity(phoneNumber = "17198765432", riskType = "investment", reportCount = 89),
            PhoneRiskEntity(phoneNumber = "16211112222", riskType = "customer_service", reportCount = 67),
            PhoneRiskEntity(phoneNumber = "19233334444", riskType = "lottery", reportCount = 45),
            PhoneRiskEntity(phoneNumber = "14566667777", riskType = "health_product", reportCount = 34),
            PhoneRiskEntity(phoneNumber = "14788889999", riskType = "family_emergency", reportCount = 23)
        )

        riskPhones.forEach { phone ->
            database.phoneRiskDao().insertOrUpdate(phone)
        }
    }

    /**
     * 初始化系统设置
     */
    private suspend fun initializeSystemSettings() {
        // 创建默认用户
        val defaultUser = UserEntity(
            id = 1,
            name = "用户",
            phone = "13800138000",
            idNumber = "000000000000000000",
            isElderly = true
        )
        database.userDao().insertUser(defaultUser)

        // 创建默认积分账户
        val pointsAccount = PointsAccountEntity(
            userId = 1,
            balance = 0.0,
            totalEarned = 0.0,
            totalSpent = 0.0
        )
        database.pointsAccountDao().insertAccount(pointsAccount)
    }

    /**
     * 重新初始化数据库（清除所有数据）
     */
    suspend fun reinitialize() = withContext(Dispatchers.IO) {
        // 清除所有数据
        database.clearAllTables()
        // 重新初始化
        initialize()
    }

    companion object {
        @Volatile
        private var instance: DatabaseInitializer? = null

        fun getInstance(context: Context): DatabaseInitializer {
            return instance ?: synchronized(this) {
                instance ?: DatabaseInitializer(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
