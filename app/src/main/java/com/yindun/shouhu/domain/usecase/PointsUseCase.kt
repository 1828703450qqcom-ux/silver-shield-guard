package com.yindun.shouhu.domain.usecase

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.PointsAccountEntity
import com.yindun.shouhu.data.local.entity.TrainingRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 积分管理用例
 * 管理用户的积分和奖励
 */
class PointsUseCase(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    /**
     * 获取用户积分账户
     */
    fun getPointsAccount(userId: Long): Flow<PointsAccountEntity?> {
        return database.pointsAccountDao().getPointsAccountFlow(userId)
    }

    /**
     * 获取或创建积分账户
     */
    suspend fun getOrCreatePointsAccount(userId: Long): PointsAccountEntity = withContext(Dispatchers.IO) {
        val existing = database.pointsAccountDao().getPointsAccount(userId)
        if (existing != null) {
            existing
        } else {
            val newAccount = PointsAccountEntity(
                userId = userId,
                balance = 0.0,
                totalEarned = 0.0,
                totalSpent = 0.0,
                updatedAt = System.currentTimeMillis()
            )
            database.pointsAccountDao().insertAccount(newAccount)
            newAccount
        }
    }

    /**
     * 添加积分
     */
    suspend fun addPoints(userId: Long, amount: Double, description: String) = withContext(Dispatchers.IO) {
        val account = getOrCreatePointsAccount(userId)
        database.pointsAccountDao().updateAccount(
            account.copy(
                balance = account.balance + amount,
                totalEarned = account.totalEarned + amount,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * 消费积分
     */
    suspend fun spendPoints(userId: Long, amount: Double, description: String): Boolean = withContext(Dispatchers.IO) {
        val account = getOrCreatePointsAccount(userId)
        if (account.balance >= amount) {
            database.pointsAccountDao().updateAccount(
                account.copy(
                    balance = account.balance - amount,
                    totalSpent = account.totalSpent + amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
            true
        } else {
            false
        }
    }

    /**
     * 获取用户训练记录
     */
    fun getTrainingHistory(userId: Long): Flow<List<TrainingRecordEntity>> {
        return database.trainingRecordDao().getTrainingHistory(userId)
    }

    /**
     * 获取已完成的训练
     */
    fun getCompletedTrainings(userId: Long): Flow<List<TrainingRecordEntity>> {
        return database.trainingRecordDao().getCompletedTrainings(userId)
    }

    /**
     * 获取训练统计
     */
    suspend fun getTrainingStats(userId: Long): TrainingStats = withContext(Dispatchers.Default) {
        val completedCount = database.trainingRecordDao().getCompletedCount(userId)
        val totalRewards = database.trainingRecordDao().getTotalRewards(userId) ?: 0.0

        // 获取最近7天的训练记录
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        val recentTrainings = database.trainingRecordDao().getTrainingHistory(userId)
            .let { flow ->
                // 实际项目中应该在DAO层过滤
                // 这里简化处理
                emptyList<TrainingRecordEntity>()
            }

        TrainingStats(
            totalCompleted = completedCount,
            totalRewards = totalRewards,
            averageScore = 0.0, // 需要计算
            streakDays = calculateStreakDays(userId)
        )
    }

    /**
     * 计算连续训练天数
     */
    private suspend fun calculateStreakDays(userId: Long): Int {
        // 这里需要查询训练记录并计算连续天数
        // 简化实现
        return 0
    }

    /**
     * 可兑换的商品列表
     */
    fun getRewardsCatalog(): List<RewardItem> {
        return listOf(
            RewardItem(
                id = 1,
                name = "防诈骗手册",
                description = "纸质版防诈骗知识手册",
                pointsCost = 10.0,
                category = "physical"
            ),
            RewardItem(
                id = 2,
                name = "手机支架",
                description = "适老化大字体手机支架",
                pointsCost = 20.0,
                category = "physical"
            ),
            RewardItem(
                id = 3,
                name = "公交卡充值",
                description = "10元公交卡充值",
                pointsCost = 15.0,
                category = "service"
            ),
            RewardItem(
                id = 4,
                name = "话费充值",
                description = "10元话费充值",
                pointsCost = 15.0,
                category = "service"
            ),
            RewardItem(
                id = 5,
                name = "社区评优资格",
                description = "获得社区防诈骗先锋称号",
                pointsCost = 50.0,
                category = "honor"
            ),
            RewardItem(
                id = 6,
                name = "反诈保险",
                description = "1万元诈骗损失保险",
                pointsCost = 100.0,
                category = "insurance"
            )
        )
    }

    /**
     * 兑换奖励
     */
    suspend fun redeemReward(userId: Long, rewardId: Int): Boolean = withContext(Dispatchers.IO) {
        val rewards = getRewardsCatalog()
        val reward = rewards.find { it.id == rewardId } ?: return@withContext false

        val account = getOrCreatePointsAccount(userId)
        if (account.balance >= reward.pointsCost) {
            // 扣除积分
            database.pointsAccountDao().updateAccount(
                account.copy(
                    balance = account.balance - reward.pointsCost,
                    totalSpent = account.totalSpent + reward.pointsCost,
                    updatedAt = System.currentTimeMillis()
                )
            )
            true
        } else {
            false
        }
    }

    companion object {
        @Volatile
        private var instance: PointsUseCase? = null

        fun getInstance(context: Context): PointsUseCase {
            return instance ?: synchronized(this) {
                instance ?: PointsUseCase(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 训练统计
 */
data class TrainingStats(
    val totalCompleted: Int,
    val totalRewards: Double,
    val averageScore: Double,
    val streakDays: Int
)

/**
 * 奖励商品
 */
data class RewardItem(
    val id: Int,
    val name: String,
    val description: String,
    val pointsCost: Double,
    val category: String
) {
    companion object {
        fun getCategoryDisplayName(category: String): String {
            return when (category) {
                "physical" -> "实物奖品"
                "service" -> "服务抵扣"
                "honor" -> "荣誉称号"
                "insurance" -> "保险保障"
                else -> category
            }
        }
    }
}
