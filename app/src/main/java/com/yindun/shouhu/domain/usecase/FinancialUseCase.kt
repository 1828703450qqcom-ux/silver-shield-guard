package com.yindun.shouhu.domain.usecase

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.AccountEntity
import com.yindun.shouhu.data.local.entity.TransactionEntity
import com.yindun.shouhu.domain.engine.RiskRuleManager
import com.yindun.shouhu.domain.engine.RuleEvaluationResult
import com.yindun.shouhu.domain.engine.TransactionSummary
import com.yindun.shouhu.util.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 财务监控用例
 * 处理账户绑定、交易监控等业务逻辑
 */
class FinancialUseCase(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }
    private val cryptoManager by lazy { CryptoManager.getInstance(context) }
    private val riskRuleManager by lazy { RiskRuleManager.getInstance(context) }

    /**
     * 绑定银行卡
     */
    suspend fun bindBankCard(
        userId: Long,
        bankName: String,
        cardNumber: String,
        balance: Double = 0.0
    ): Long = withContext(Dispatchers.IO) {
        // 加密卡号
        val encryptedCardNumber = cryptoManager.encrypt(cardNumber)

        val account = AccountEntity(
            userId = userId,
            bankName = bankName,
            accountNumber = encryptedCardNumber,
            accountType = "bank_card",
            balance = balance
        )
        database.accountDao().insertAccount(account)
    }

    /**
     * 绑定微信钱包
     */
    suspend fun bindWechatWallet(
        userId: Long,
        wechatId: String,
        balance: Double = 0.0
    ): Long = withContext(Dispatchers.IO) {
        val encryptedWechatId = cryptoManager.encrypt(wechatId)

        val account = AccountEntity(
            userId = userId,
            bankName = "微信钱包",
            accountNumber = encryptedWechatId,
            accountType = "wechat",
            balance = balance
        )
        database.accountDao().insertAccount(account)
    }

    /**
     * 绑定支付宝
     */
    suspend fun bindAlipay(
        userId: Long,
        alipayId: String,
        balance: Double = 0.0
    ): Long = withContext(Dispatchers.IO) {
        val encryptedAlipayId = cryptoManager.encrypt(alipayId)

        val account = AccountEntity(
            userId = userId,
            bankName = "支付宝",
            accountNumber = encryptedAlipayId,
            accountType = "alipay",
            balance = balance
        )
        database.accountDao().insertAccount(account)
    }

    /**
     * 获取用户的所有账户
     */
    fun getAccountsByUser(userId: Long): Flow<List<AccountEntity>> {
        return database.accountDao().getAccountsByUser(userId)
    }

    /**
     * 解绑账户
     */
    suspend fun unbindAccount(accountId: Long) = withContext(Dispatchers.IO) {
        val account = database.accountDao().getAccountById(accountId)
        account?.let {
            database.accountDao().deleteAccount(it)
        }
    }

    /**
     * 添加交易记录
     */
    suspend fun addTransaction(
        accountId: Long,
        amount: Double,
        counterparty: String,
        description: String,
        transactionTime: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val transaction = TransactionEntity(
            accountId = accountId,
            amount = amount,
            counterparty = counterparty,
            description = description,
            transactionTime = transactionTime
        )
        database.transactionDao().insertTransaction(transaction)
    }

    /**
     * 获取账户的交易记录
     */
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>> {
        return database.transactionDao().getTransactionsByAccount(accountId)
    }

    /**
     * 获取用户的最近交易
     */
    fun getRecentTransactions(userId: Long, limit: Int = 50): Flow<List<TransactionEntity>> {
        return database.transactionDao().getRecentTransactions(userId, limit)
    }

    /**
     * 获取可疑交易
     */
    fun getSuspiciousTransactions(userId: Long): Flow<List<TransactionEntity>> {
        return database.transactionDao().getSuspiciousTransactions(userId)
    }

    /**
     * 分析交易风险
     */
    suspend fun analyzeTransaction(
        accountId: Long,
        amount: Double,
        counterparty: String,
        transactionTime: Long
    ): RuleEvaluationResult = withContext(Dispatchers.Default) {
        // 获取该账户的近期交易
        val recentTransactions = database.transactionDao()
            .getTransactionsByAccount(accountId)
            .let { flow ->
                // 这里需要等待Flow emit，实际项目中可能需要调整
                mutableListOf<TransactionSummary>()
            }

        // 评估风险
        riskRuleManager.evaluateTransaction(
            amount = amount,
            counterparty = counterparty,
            transactionTime = transactionTime,
            recentTransactions = recentTransactions
        )
    }

    /**
     * 标记交易为可疑
     */
    suspend fun markTransactionAsSuspicious(
        transactionId: Long,
        riskScore: Int
    ) = withContext(Dispatchers.IO) {
        // 获取交易（这里简化处理，实际应该通过ID查询）
        val transactions = database.transactionDao().getRecentTransactions(0, 1)
        // 实际项目中应该有getTransactionById方法
    }

    /**
     * 获取账户余额
     */
    suspend fun getAccountBalance(accountId: Long): Double = withContext(Dispatchers.IO) {
        database.accountDao().getAccountById(accountId)?.balance ?: 0.0
    }

    /**
     * 更新账户余额
     */
    suspend fun updateAccountBalance(accountId: Long, newBalance: Double) = withContext(Dispatchers.IO) {
        val account = database.accountDao().getAccountById(accountId)
        account?.let {
            database.accountDao().updateAccount(it.copy(balance = newBalance))
        }
    }

    /**
     * 获取交易统计
     */
    suspend fun getTransactionStats(userId: Long, days: Int = 30): TransactionStats = withContext(Dispatchers.Default) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - days * 24 * 60 * 60 * 1000L

        val transactions = database.transactionDao()
            .getTransactionsByDateRange(userId, startTime, endTime)

        val totalAmount = transactions.sumOf { it.amount }
        val avgAmount = if (transactions.isNotEmpty()) totalAmount / transactions.size else 0.0
        val maxAmount = transactions.maxOfOrNull { it.amount } ?: 0.0
        val suspiciousCount = transactions.count { it.isSuspicious }

        TransactionStats(
            totalTransactions = transactions.size,
            totalAmount = totalAmount,
            averageAmount = avgAmount,
            maxAmount = maxAmount,
            suspiciousCount = suspiciousCount,
            periodDays = days
        )
    }

    /**
     * 初始化风控规则
     */
    suspend fun initRiskRules() {
        riskRuleManager.initDefaultRules()
    }

    companion object {
        @Volatile
        private var instance: FinancialUseCase? = null

        fun getInstance(context: Context): FinancialUseCase {
            return instance ?: synchronized(this) {
                instance ?: FinancialUseCase(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 交易统计
 */
data class TransactionStats(
    val totalTransactions: Int,
    val totalAmount: Double,
    val averageAmount: Double,
    val maxAmount: Double,
    val suspiciousCount: Int,
    val periodDays: Int
) {
    val suspiciousRate: Double
        get() = if (totalTransactions > 0) suspiciousCount.toDouble() / totalTransactions else 0.0
}
