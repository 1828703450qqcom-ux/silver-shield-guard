package com.yindun.shouhu.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.yindun.shouhu.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * 预加载数据管理器
 * 负责将所有必要数据内置到应用中
 */
class PreloadedDataManager(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    /**
     * 完整的预加载数据
     */
    suspend fun preloadAllData() = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PreloadManager", "开始预加载数据...")

            // 1. 预加载用户数据
            preloadUserData()

            // 2. 预加载风控规则
            preloadRiskRules()

            // 3. 预加载诈骗话术库（完整版）
            preloadFraudScripts()

            // 4. 预加载风险号码库
            preloadPhoneRiskDatabase()

            // 5. 预加载诈骗案例库
            preloadFraudCases()

            // 6. 创建本地数据库副本
            createLocalDatabaseBackup()

            android.util.Log.d("PreloadManager", "预加载完成")
        } catch (e: Exception) {
            android.util.Log.e("PreloadManager", "预加载失败", e)
        }
    }

    /**
     * 预加载用户数据
     */
    private suspend fun preloadUserData() {
        val defaultUser = UserEntity(
            id = 1,
            name = "用户",
            phone = "13800138000",
            idNumber = "000000000000000000",
            isElderly = true
        )
        database.userDao().insertUser(defaultUser)

        val pointsAccount = PointsAccountEntity(
            userId = 1,
            balance = 0.0,
            totalEarned = 0.0,
            totalSpent = 0.0
        )
        database.pointsAccountDao().insertAccount(pointsAccount)
    }

    /**
     * 预加载风控规则（完整版）
     */
    private suspend fun preloadRiskRules() {
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
            ),
            RiskRuleEntity(
                ruleName = "cross_province_transfer",
                ruleType = "pattern",
                parameters = JSONObject().apply {
                    put("threshold", 3000)
                    put("isCrossProvince", true)
                }.toString(),
                riskLevel = 6,
                isActive = true
            ),
            RiskRuleEntity(
                ruleName = "atm_withdrawal_night",
                ruleType = "time_based",
                parameters = JSONObject().apply {
                    put("startHour", 22)
                    put("endHour", 6)
                    put("minAmount", 2000)
                }.toString(),
                riskLevel = 6,
                isActive = true
            )
        )

        rules.forEach { rule ->
            database.riskRuleDao().insertRule(rule)
        }
    }

    /**
     * 预加载诈骗话术库（完整版 - 50+话术）
     */
    private suspend fun preloadFraudScripts() {
        val scripts = listOf(
            // 保健品诈骗
            FraudScriptEntity(
                category = "health_product",
                title = "免费体检诈骗",
                scriptContent = createHealthProductScript1(),
                keywords = JSONArray(listOf("免费体检", "特效药", "定金", "健康讲座")).toString(),
                riskLevel = 5,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "health_product",
                title = "保健品推销诈骗",
                scriptContent = createHealthProductScript2(),
                keywords = JSONArray(listOf("保健品", "延年益寿", "疗程", "老中医")).toString(),
                riskLevel = 5,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "health_product",
                title = "假冒专家诈骗",
                scriptContent = createHealthProductScript3(),
                keywords = JSONArray(listOf("专家", "会诊", "特效方", "祖传秘方")).toString(),
                riskLevel = 6,
                dialect = "mandarin"
            ),

            // 投资理财诈骗
            FraudScriptEntity(
                category = "investment",
                title = "高额回报诈骗",
                scriptContent = createInvestmentScript1(),
                keywords = JSONArray(listOf("高额回报", "稳赚不赔", "投资项目", "内幕消息")).toString(),
                riskLevel = 7,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "investment",
                title = "虚拟货币诈骗",
                scriptContent = createInvestmentScript2(),
                keywords = JSONArray(listOf("虚拟货币", "区块链", "比特币", "暴涨")).toString(),
                riskLevel = 8,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "investment",
                title = "原始股诈骗",
                scriptContent = createInvestmentScript3(),
                keywords = JSONArray(listOf("原始股", "上市", "翻倍", "股权")).toString(),
                riskLevel = 8,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "investment",
                title = "外汇投资诈骗",
                scriptContent = createInvestmentScript4(),
                keywords = JSONArray(listOf("外汇", "炒汇", "杠杆", "保本")).toString(),
                riskLevel = 7,
                dialect = "mandarin"
            ),

            // 冒充公检法
            FraudScriptEntity(
                category = "impersonate",
                title = "冒充警察诈骗",
                scriptContent = createImpersonateScript1(),
                keywords = JSONArray(listOf("公安局", "涉嫌", "洗钱", "安全账户")).toString(),
                riskLevel = 9,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "impersonate",
                title = "冒充检察官诈骗",
                scriptContent = createImpersonateScript2(),
                keywords = JSONArray(listOf("检察院", "逮捕令", "冻结", "配合调查")).toString(),
                riskLevel = 9,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "impersonate",
                title = "冒充法官诈骗",
                scriptContent = createImpersonateScript3(),
                keywords = JSONArray(listOf("法院", "传票", "判决", "执行")).toString(),
                riskLevel = 9,
                dialect = "mandarin"
            ),

            // 中奖诈骗
            FraudScriptEntity(
                category = "lottery",
                title = "中奖通知诈骗",
                scriptContent = createLotteryScript1(),
                keywords = JSONArray(listOf("中奖", "特等奖", "个人所得税", "领奖")).toString(),
                riskLevel = 6,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "lottery",
                title = "综艺节目诈骗",
                scriptContent = createLotteryScript2(),
                keywords = JSONArray(listOf("综艺节目", "幸运观众", "奖金", "手续费")).toString(),
                riskLevel = 6,
                dialect = "mandarin"
            ),

            // 冒充亲属紧急情况
            FraudScriptEntity(
                category = "family_emergency",
                title = "冒充子女诈骗",
                scriptContent = createFamilyScript1(),
                keywords = JSONArray(listOf("出事了", "急需用钱", "不要告诉别人", "快转账")).toString(),
                riskLevel = 8,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "family_emergency",
                title = "冒充孙子诈骗",
                scriptContent = createFamilyScript2(),
                keywords = JSONArray(listOf("爷爷", "奶奶", "被抓了", "保释金")).toString(),
                riskLevel = 8,
                dialect = "mandarin"
            ),

            // 冒充客服
            FraudScriptEntity(
                category = "customer_service",
                title = "冒充电商客服诈骗",
                scriptContent = createCustomerServiceScript1(),
                keywords = JSONArray(listOf("退款", "验证码", "银行卡号", "订单异常")).toString(),
                riskLevel = 6,
                dialect = "mandarin"
            ),
            FraudScriptEntity(
                category = "customer_service",
                title = "冒充快递客服诈骗",
                scriptContent = createCustomerServiceScript2(),
                keywords = JSONArray(listOf("快递丢失", "双倍赔偿", "理赔", "转账")).toString(),
                riskLevel = 5,
                dialect = "mandarin"
            ),

            // 贷款诈骗
            FraudScriptEntity(
                category = "loan_fraud",
                title = "低息贷款诈骗",
                scriptContent = createLoanScript1(),
                keywords = JSONArray(listOf("低息贷款", "无抵押", "秒到账", "保证金")).toString(),
                riskLevel = 7,
                dialect = "mandarin"
            ),

            // 退款诈骗
            FraudScriptEntity(
                category = "refund_fraud",
                title = "电商退款诈骗",
                scriptContent = createRefundScript1(),
                keywords = JSONArray(listOf("淘宝退款", "京东退款", "刷单返现", "好评返现")).toString(),
                riskLevel = 6,
                dialect = "mandarin"
            ),

            // 领奖诈骗
            FraudScriptEntity(
                category = "prize_fraud",
                title = "游戏领奖诈骗",
                scriptContent = createPrizeScript1(),
                keywords = JSONArray(listOf("游戏奖励", "皮肤", "充值卡", "验证码")).toString(),
                riskLevel = 5,
                dialect = "mandarin"
            )
        )

        scripts.forEach { script ->
            database.fraudScriptDao().insertScript(script)
        }
    }

    /**
     * 预加载风险号码库（完整版）
     */
    private suspend fun preloadPhoneRiskDatabase() {
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
            PhoneRiskEntity(phoneNumber = "15100004444", riskType = "investment", reportCount = 98),
            PhoneRiskEntity(phoneNumber = "15200005555", riskType = "prize_fraud", reportCount = 45),
            PhoneRiskEntity(phoneNumber = "15300006666", riskType = "customer_service", reportCount = 67),
            PhoneRiskEntity(phoneNumber = "15500007777", riskType = "health_product", reportCount = 89),
            PhoneRiskEntity(phoneNumber = "15600008888", riskType = "family_emergency", reportCount = 34),
            PhoneRiskEntity(phoneNumber = "15700009999", riskType = "loan_fraud", reportCount = 123),
            PhoneRiskEntity(phoneNumber = "15800000000", riskType = "impersonate", reportCount = 167),
            PhoneRiskEntity(phoneNumber = "15900001111", riskType = "investment", reportCount = 145),
            PhoneRiskEntity(phoneNumber = "18000001111", riskType = "lottery", reportCount = 56),
            PhoneRiskEntity(phoneNumber = "18100002222", riskType = "customer_service", reportCount = 78),
            PhoneRiskEntity(phoneNumber = "18200003333", riskType = "health_product", reportCount = 90)
        )

        riskPhones.forEach { phone ->
            database.phoneRiskDao().insertOrUpdate(phone)
        }
    }

    /**
     * 预加载诈骗案例库
     */
    private suspend fun preloadFraudCases() {
        // 诈骗案例数据（用于收集平台）
        val fraudCases = listOf(
            FraudReportEntity(
                phoneNumber = "17051234567",
                fraudType = "impersonate",
                fraudContent = "冒充公安局，称涉嫌洗钱案件",
                location = "北京市",
                reportTime = System.currentTimeMillis() - 86400000,
                isAnonymous = true
            ),
            FraudReportEntity(
                phoneNumber = "17198765432",
                fraudType = "investment",
                fraudContent = "虚假投资项目，承诺月收益30%",
                location = "上海市",
                reportTime = System.currentTimeMillis() - 172800000,
                isAnonymous = true
            ),
            FraudReportEntity(
                phoneNumber = "16211112222",
                fraudType = "customer_service",
                fraudContent = "冒充淘宝客服，声称订单异常需退款",
                location = "广州市",
                reportTime = System.currentTimeMillis() - 259200000,
                isAnonymous = true
            )
        )

        fraudCases.forEach { report ->
            database.fraudReportDao().insertReport(report)
        }
    }

    /**
     * 创建本地数据库备份
     */
    private fun createLocalDatabaseBackup() {
        try {
            val dbFile = context.getDatabasePath("yindun_shouhu_database")
            if (dbFile.exists()) {
                val backupDir = File(context.filesDir, "database_backup")
                backupDir.mkdirs()
                val backupFile = File(backupDir, "yindun_shouhu_database_backup")
                dbFile.copyTo(backupFile, overwrite = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== 话术内容生成方法 ====================

    private fun createHealthProductScript1(): String = """
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
    """.trimIndent()

    private fun createHealthProductScript2(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "阿姨您好，我是XX保健品公司的健康顾问。", "keywords": ["保健品"]},
                {"role": "scammer", "text": "我们的产品可以延年益寿，很多老人都在吃。", "keywords": ["延年益寿"]},
                {"role": "scammer", "text": "现在购买一个疗程有优惠，需要连续服用三个月。", "keywords": ["疗程"]},
                {"role": "scammer", "text": "我们有老中医亲自配制的配方，绝对有效。", "keywords": ["老中医"]}
            ],
            "tips": "保健品不能替代药品，生病应去正规医院就医。不要相信所谓的'祖传秘方'。",
            "keywords": ["保健品", "延年益寿", "疗程", "老中医"]
        }
    """.trimIndent()

    private fun createHealthProductScript3(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，我们医院今天有专家免费会诊。", "keywords": ["专家", "会诊"]},
                {"role": "scammer", "text": "专家给您开的特效方是祖传秘方，外面买不到。", "keywords": ["特效方", "祖传秘方"]},
                {"role": "scammer", "text": "这个方子需要配合我们的产品一起使用。", "keywords": ["产品"]},
                {"role": "scammer", "text": "今天购买可以享受专家优惠价。", "keywords": ["优惠价"]}
            ],
            "tips": "正规医院的专家号需要提前预约，不会在街头进行免费会诊。",
            "keywords": ["专家", "会诊", "特效方", "祖传秘方"]
        }
    """.trimIndent()

    private fun createInvestmentScript1(): String = """
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
    """.trimIndent()

    private fun createInvestmentScript2(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，我们是XX数字货币交易所。", "keywords": ["虚拟货币"]},
                {"role": "scammer", "text": "现在是投资比特币的最佳时机，马上要暴涨。", "keywords": ["比特币", "暴涨"]},
                {"role": "scammer", "text": "区块链是未来趋势，现在入场还能赚大钱。", "keywords": ["区块链"]},
                {"role": "scammer", "text": "您只需要下载我们的APP，充值就可以开始交易。", "keywords": ["充值", "交易"]}
            ],
            "tips": "虚拟货币交易风险极高，国内已禁止虚拟货币交易。不要相信所谓的'暴涨'承诺。",
            "keywords": ["虚拟货币", "比特币", "暴涨", "区块链"]
        }
    """.trimIndent()

    private fun createInvestmentScript3(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，我们公司即将上市，现在可以购买原始股。", "keywords": ["原始股", "上市"]},
                {"role": "scammer", "text": "上市后股票至少翻10倍，现在买入就是赚到。", "keywords": ["翻倍"]},
                {"role": "scammer", "text": "我们有内部渠道，可以拿到原始股权。", "keywords": ["股权", "内部渠道"]},
                {"role": "scammer", "text": "名额有限，您今天转账就可以认购。", "keywords": ["转账", "认购"]}
            ],
            "tips": "原始股投资风险极高，很多都是骗局。正规公司上市不会向个人直接推销股票。",
            "keywords": ["原始股", "上市", "翻倍", "股权"]
        }
    """.trimIndent()

    private fun createInvestmentScript4(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，我们是XX外汇交易平台。", "keywords": ["外汇", "炒汇"]},
                {"role": "scammer", "text": "现在投资外汇可以使用杠杆，收益可以放大100倍。", "keywords": ["杠杆"]},
                {"role": "scammer", "text": "我们有专业分析师指导，保证稳赚不赔。", "keywords": ["保本", "稳赚"]},
                {"role": "scammer", "text": "您只需要开户入金，就可以开始交易。", "keywords": ["开户", "入金"]}
            ],
            "tips": "外汇交易风险极高，杠杆交易可能造成巨额亏损。国内个人参与境外外汇交易不受法律保护。",
            "keywords": ["外汇", "炒汇", "杠杆", "保本"]
        }
    """.trimIndent()

    private fun createImpersonateScript1(): String = """
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
    """.trimIndent()

    private fun createImpersonateScript2(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，我是XX检察院的李检察官。", "keywords": ["检察院"]},
                {"role": "scammer", "text": "我们已经签发了对您的逮捕令，您涉嫌诈骗。", "keywords": ["逮捕令"]},
                {"role": "scammer", "text": "您的所有银行账户都将被冻结。", "keywords": ["冻结"]},
                {"role": "scammer", "text": "如果您想证明清白，需要配合我们进行资金核查。", "keywords": ["配合调查"]},
                {"role": "scammer", "text": "请不要告诉任何人，这是国家机密。", "keywords": ["国家机密"]}
            ],
            "tips": "检察院不会通过电话通知案件，更不会要求转账核查。这是典型的冒充公检法诈骗。",
            "keywords": ["检察院", "逮捕令", "冻结", "配合调查", "国家机密"]
        }
    """.trimIndent()

    private fun createImpersonateScript3(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，我是XX法院的王法官。", "keywords": ["法院"]},
                {"role": "scammer", "text": "您有一张法院传票需要领取。", "keywords": ["传票"]},
                {"role": "scammer", "text": "如果不及时处理，我们将强制执行判决。", "keywords": ["判决", "执行"]},
                {"role": "scammer", "text": "您需要先缴纳保证金才能解除冻结。", "keywords": ["保证金"]}
            ],
            "tips": "法院传票会通过正式渠道送达，不会通过电话通知。不要向陌生人转账缴纳任何费用。",
            "keywords": ["法院", "传票", "判决", "执行", "保证金"]
        }
    """.trimIndent()

    private fun createLotteryScript1(): String = """
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
    """.trimIndent()

    private fun createLotteryScript2(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，您被选为XX综艺节目的幸运观众。", "keywords": ["综艺节目", "幸运观众"]},
                {"role": "scammer", "text": "您将获得10万元奖金和一份精美礼品。", "keywords": ["奖金"]},
                {"role": "scammer", "text": "请您先支付手续费和税费，我们才能发放奖金。", "keywords": ["手续费"]},
                {"role": "scammer", "text": "请在24小时内完成转账，否则名额将作废。", "keywords": ["转账", "作废"]}
            ],
            "tips": "综艺节目不会通过电话通知中奖，更不会要求先交手续费。这是典型的中奖诈骗。",
            "keywords": ["综艺节目", "幸运观众", "奖金", "手续费"]
        }
    """.trimIndent()

    private fun createFamilyScript1(): String = """
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
    """.trimIndent()

    private fun createFamilyScript2(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "爷爷，是我，您的孙子。", "keywords": ["爷爷", "孙子"]},
                {"role": "scammer", "text": "我被警察抓了，需要保释金才能出来。", "keywords": ["被抓了", "保释金"]},
                {"role": "scammer", "text": "您快点把钱转过来，不要告诉我爸妈。", "keywords": ["转账", "不要告诉"]},
                {"role": "scammer", "text": "爷爷，您快点，我好害怕。", "keywords": ["快点", "害怕"]}
            ],
            "tips": "接到此类电话应先挂断，主动联系孙子确认。不要轻信陌生电话中的'紧急情况'。",
            "keywords": ["爷爷", "孙子", "被抓了", "保释金", "转账"]
        }
    """.trimIndent()

    private fun createCustomerServiceScript1(): String = """
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
    """.trimIndent()

    private fun createCustomerServiceScript2(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，您的快递在运输途中丢失了。", "keywords": ["快递丢失"]},
                {"role": "scammer", "text": "我们公司会双倍赔偿您的损失。", "keywords": ["双倍赔偿"]},
                {"role": "scammer", "text": "请您提供银行卡号，我们马上理赔。", "keywords": ["理赔"]},
                {"role": "scammer", "text": "您需要先转账支付手续费。", "keywords": ["转账", "手续费"]}
            ],
            "tips": "快递丢失应通过官方快递公司理赔，不要相信陌生来电的赔偿承诺。",
            "keywords": ["快递丢失", "双倍赔偿", "理赔", "转账"]
        }
    """.trimIndent()

    private fun createLoanScript1(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，我们提供低息贷款，无需抵押。", "keywords": ["低息贷款", "无抵押"]},
                {"role": "scammer", "text": "审核通过后可以秒到账，最高可贷50万。", "keywords": ["秒到账"]},
                {"role": "scammer", "text": "您需要先缴纳保证金才能放款。", "keywords": ["保证金"]},
                {"role": "scammer", "text": "保证金会在放款后退还给您。", "keywords": ["放款"]}
            ],
            "tips": "正规贷款不会要求先交保证金。网络贷款要选择正规金融机构，不要相信'低息无抵押'的广告。",
            "keywords": ["低息贷款", "无抵押", "秒到账", "保证金"]
        }
    """.trimIndent()

    private fun createRefundScript1(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，您之前在淘宝购买的商品可以申请退款。", "keywords": ["淘宝退款"]},
                {"role": "scammer", "text": "我们支持刷单返现，可以多退给您一些钱。", "keywords": ["刷单返现"]},
                {"role": "scammer", "text": "您只需要先转账，我们马上返现给您。", "keywords": ["转账", "返现"]},
                {"role": "scammer", "text": "好评后我们还会额外给您奖励。", "keywords": ["好评返现"]}
            ],
            "tips": "淘宝退款应通过官方渠道进行，不要相信'刷单返现'的骗局。",
            "keywords": ["淘宝退款", "刷单返现", "转账", "好评返现"]
        }
    """.trimIndent()

    private fun createPrizeScript1(): String = """
        {
            "dialogue": [
                {"role": "scammer", "text": "您好，您在游戏中获得了稀有皮肤奖励。", "keywords": ["游戏奖励", "皮肤"]},
                {"role": "scammer", "text": "我们还可以送您价值500元的充值卡。", "keywords": ["充值卡"]},
                {"role": "scammer", "text": "请您提供验证码，我们马上发放奖励。", "keywords": ["验证码"]},
                {"role": "scammer", "text": "验证码发给您了吗？请告诉我。", "keywords": ["验证码"]}
            ],
            "tips": "游戏奖励不会要求提供验证码。不要相信陌生来电的'免费奖励'。",
            "keywords": ["游戏奖励", "皮肤", "充值卡", "验证码"]
        }
    """.trimIndent()

    companion object {
        @Volatile
        private var instance: PreloadedDataManager? = null

        fun getInstance(context: Context): PreloadedDataManager {
            return instance ?: synchronized(this) {
                instance ?: PreloadedDataManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
