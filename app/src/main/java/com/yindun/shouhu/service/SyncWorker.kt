package com.yindun.shouhu.service

import android.content.Context
import androidx.work.*
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.FraudScriptEntity
import com.yindun.shouhu.util.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * 同步工作器
 * 定期同步诈骗话术库
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val syncManager = SyncManager.getInstance(applicationContext)
            val database = AppDatabase.getDatabase(applicationContext)

            // 获取上次同步时间
            val prefs = applicationContext.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            val lastSyncTime = prefs.getLong("last_sync_time", 0)

            // 同步话术库
            val scripts = syncManager.syncFraudScripts(lastSyncTime)
            if (scripts.isNotEmpty()) {
                database.fraudScriptDao().insertScripts(scripts)
            }

            // 更新同步时间
            prefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()

            // 如果是首次运行，加载本地预置话术
            if (lastSyncTime == 0L) {
                loadPresetScripts(database)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * 加载预置话术
     */
    private suspend fun loadPresetScripts(database: AppDatabase) {
        val presetScripts = getPresetScripts()
        database.fraudScriptDao().insertScripts(presetScripts)
    }

    /**
     * 获取预置话术
     */
    private fun getPresetScripts(): List<FraudScriptEntity> {
        return listOf(
            // 保健品诈骗
            FraudScriptEntity(
                category = "health_product",
                title = "免费体检诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "您好，我们是XX健康中心，现在有免费体检活动，名额有限。"},
                            {"role": "scammer", "text": "检查出您身体有严重问题，需要购买我们的特效药。"},
                            {"role": "scammer", "text": "这个药很贵但是效果好，您可以先付定金。"}
                        ],
                        "keywords": ["免费体检", "特效药", "定金"],
                        "tips": "正规医疗机构不会在街头推销，体检应去正规医院。"
                    }
                """.trimIndent(),
                keywords = "[\"免费体检\",\"特效药\",\"定金\",\"健康讲座\"]",
                riskLevel = 7,
                dialect = "mandarin"
            ),
            // 投资理财诈骗
            FraudScriptEntity(
                category = "investment",
                title = "高额回报诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "我们有一个稳赚不赔的投资项目，月收益30%。"},
                            {"role": "scammer", "text": "很多老客户都已经赚了几百万。"},
                            {"role": "scammer", "text": "您先投一点试试，保证能赚回来。"}
                        ],
                        "keywords": ["稳赚不赔", "月收益", "投资"],
                        "tips": "投资有风险，承诺高回报的基本都是诈骗。"
                    }
                """.trimIndent(),
                keywords = "[\"高额回报\",\"稳赚不赔\",\"投资\",\"内幕消息\"]",
                riskLevel = 9,
                dialect = "mandarin"
            ),
            // 冒充公检法
            FraudScriptEntity(
                category = "impersonate",
                title = "冒充警察诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "我是XX公安局的，您涉嫌一起洗钱案件。"},
                            {"role": "scammer", "text": "您的账户已被冻结，需要配合调查。"},
                            {"role": "scammer", "text": "请将资金转到我们的安全账户进行核查。"}
                        ],
                        "keywords": ["公安局", "洗钱", "安全账户"],
                        "tips": "公检法不会电话办案，更不会要求转账到'安全账户'。"
                    }
                """.trimIndent(),
                keywords = "[\"公检法\",\"安全账户\",\"涉嫌洗钱\",\"配合调查\"]",
                riskLevel = 10,
                dialect = "mandarin"
            ),
            // 中奖诈骗
            FraudScriptEntity(
                category = "lottery",
                title = "中奖通知诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "恭喜您中了我们的特等奖，奖品价值100万。"},
                            {"role": "scammer", "text": "请您先缴纳20%的个人所得税。"},
                            {"role": "scammer", "text": "缴税后我们立即发放奖品。"}
                        ],
                        "keywords": ["中奖", "特等奖", "个人所得税"],
                        "tips": "中奖要求先交税的基本都是诈骗。"
                    }
                """.trimIndent(),
                keywords = "[\"中奖\",\"领奖\",\"交税\",\"手续费\"]",
                riskLevel = 8,
                dialect = "mandarin"
            ),
            // 冒充亲属紧急情况
            FraudScriptEntity(
                category = "family_emergency",
                title = "冒充子女诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "妈/爸，我出事了，急需用钱。"},
                            {"role": "scammer", "text": "我现在在医院，需要马上交手术费。"},
                            {"role": "scammer", "text": "您快点转账，先不要告诉别人。"}
                        ],
                        "keywords": ["出事了", "手术费", "不要告诉别人"],
                        "tips": "接到此类电话应先挂断，主动联系子女确认。"
                    }
                """.trimIndent(),
                keywords = "[\"紧急情况\",\"出事了\",\"急需用钱\",\"不要告诉别人\"]",
                riskLevel = 9,
                dialect = "mandarin"
            ),
            // 冒充客服
            FraudScriptEntity(
                category = "customer_service",
                title = "冒充电商客服诈骗",
                scriptContent = """
                    {
                        "dialogue": [
                            {"role": "scammer", "text": "您好，我是XX电商平台客服。"},
                            {"role": "scammer", "text": "您购买的商品有质量问题，我们要给您退款。"},
                            {"role": "scammer", "text": "请提供您的银行卡号和验证码。"}
                        ],
                        "keywords": ["退款", "银行卡号", "验证码"],
                        "tips": "正规客服不会索要验证码，退款应通过官方渠道。"
                    }
                """.trimIndent(),
                keywords = "[\"退款\",\"客服\",\"验证码\",\"银行卡号\"]",
                riskLevel = 8,
                dialect = "mandarin"
            )
        )
    }

    companion object {
        private const val WORK_NAME = "sync_fraud_scripts"

        /**
         * 调度定期同步任务
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                1, TimeUnit.DAYS // 每天同步一次
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        /**
         * 取消同步任务
         */
        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * 立即执行一次同步
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}
