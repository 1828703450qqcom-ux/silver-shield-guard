package com.yindun.shouhu.data.repository

import com.yindun.shouhu.data.local.dao.*
import com.yindun.shouhu.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// ==================== 用户仓库 ====================

class UserRepository(
    private val userDao: UserDao,
    private val familyLinkDao: FamilyLinkDao
) {
    suspend fun getUserById(userId: Long): UserEntity? = userDao.getUserById(userId)
    suspend fun getUserByPhone(phone: String): UserEntity? = userDao.getUserByPhone(phone)
    suspend fun createUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    suspend fun getFamilyLinks(elderlyUserId: Long): List<FamilyLinkEntity> =
        familyLinkDao.getFamilyLinks(elderlyUserId)

    suspend fun getLinkedElderly(childUserId: Long): List<FamilyLinkEntity> =
        familyLinkDao.getLinkedElderly(childUserId)

    suspend fun createFamilyLink(link: FamilyLinkEntity): Long =
        familyLinkDao.insertFamilyLink(link)

    suspend fun deleteFamilyLink(link: FamilyLinkEntity) = familyLinkDao.deleteFamilyLink(link)
}

// ==================== 财务仓库 ====================

class FinancialRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val riskRuleDao: RiskRuleDao
) {
    fun getAccountsByUser(userId: Long): Flow<List<AccountEntity>> =
        accountDao.getAccountsByUser(userId)

    suspend fun createAccount(account: AccountEntity): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    fun getRecentTransactions(userId: Long, limit: Int = 50): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(userId, limit)

    fun getSuspiciousTransactions(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getSuspiciousTransactions(userId)

    suspend fun createTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun getActiveRiskRules(): List<RiskRuleEntity> = riskRuleDao.getActiveRules()
}

// ==================== 诈骗训练仓库 ====================

class TrainingRepository(
    private val fraudScriptDao: FraudScriptDao,
    private val trainingRecordDao: TrainingRecordDao,
    private val pointsAccountDao: PointsAccountDao
) {
    fun getScriptsByCategory(category: String): Flow<List<FraudScriptEntity>> =
        fraudScriptDao.getScriptsByCategory(category)

    fun getScriptsByDialect(dialect: String): Flow<List<FraudScriptEntity>> =
        fraudScriptDao.getScriptsByDialect(dialect)

    suspend fun getScriptById(scriptId: Long): FraudScriptEntity? =
        fraudScriptDao.getScriptById(scriptId)

    fun getAllCategories(): Flow<List<String>> = fraudScriptDao.getAllCategories()

    fun getTrainingHistory(userId: Long): Flow<List<TrainingRecordEntity>> =
        trainingRecordDao.getTrainingHistory(userId)

    suspend fun createTrainingRecord(record: TrainingRecordEntity): Long =
        trainingRecordDao.insertRecord(record)

    suspend fun updateTrainingRecord(record: TrainingRecordEntity) =
        trainingRecordDao.updateRecord(record)

    suspend fun getTotalRewards(userId: Long): Double =
        trainingRecordDao.getTotalRewards(userId) ?: 0.0

    suspend fun getCompletedCount(userId: Long): Int = trainingRecordDao.getCompletedCount(userId)

    suspend fun getPointsAccount(userId: Long): PointsAccountEntity? =
        pointsAccountDao.getPointsAccount(userId)

    fun getPointsAccountFlow(userId: Long): Flow<PointsAccountEntity?> =
        pointsAccountDao.getPointsAccountFlow(userId)

    suspend fun updatePointsAccount(account: PointsAccountEntity) =
        pointsAccountDao.updateAccount(account)
}

// ==================== 通话防护仓库 ====================

class CallProtectionRepository(
    private val callLogDao: CallLogDao,
    private val whitelistDao: WhitelistDao,
    private val phoneRiskDao: PhoneRiskDao
) {
    fun getCallLogs(userId: Long, limit: Int = 100): Flow<List<CallLogEntity>> =
        callLogDao.getCallLogs(userId, limit)

    fun getHighRiskCalls(userId: Long, minRiskLevel: Int = 5): Flow<List<CallLogEntity>> =
        callLogDao.getHighRiskCalls(userId, minRiskLevel)

    suspend fun createCallLog(callLog: CallLogEntity): Long =
        callLogDao.insertCallLog(callLog)

    suspend fun updateCallLog(callLog: CallLogEntity) = callLogDao.updateCallLog(callLog)

    fun getWhitelist(userId: Long): Flow<List<WhitelistEntity>> =
        whitelistDao.getWhitelist(userId)

    suspend fun isInWhitelist(userId: Long, phoneNumber: String): Boolean =
        whitelistDao.isInWhitelist(userId, phoneNumber) != null

    suspend fun addToWhitelist(entry: WhitelistEntity): Long = whitelistDao.insertEntry(entry)
    suspend fun removeFromWhitelist(entry: WhitelistEntity) = whitelistDao.deleteEntry(entry)

    suspend fun getPhoneRisk(phoneNumber: String): PhoneRiskEntity? =
        phoneRiskDao.getPhoneRisk(phoneNumber)
}

// ==================== 案件仓库 ====================

class CaseRepository(
    private val caseDao: CaseDao,
    private val evidenceDao: EvidenceDao,
    private val caseProgressDao: CaseProgressDao
) {
    fun getCasesByUser(userId: Long): Flow<List<CaseEntity>> = caseDao.getCasesByUser(userId)

    fun getCasesByFamilyMember(childUserId: Long): Flow<List<CaseEntity>> =
        caseDao.getCasesByFamilyMember(childUserId)

    suspend fun getCaseById(caseId: Long): CaseEntity? = caseDao.getCaseById(caseId)
    suspend fun createCase(case: CaseEntity): Long = caseDao.insertCase(case)
    suspend fun updateCase(case: CaseEntity) = caseDao.updateCase(case)

    fun getEvidenceByCase(caseId: Long): Flow<List<EvidenceEntity>> =
        evidenceDao.getEvidenceByCase(caseId)

    suspend fun addEvidence(evidence: EvidenceEntity): Long = evidenceDao.insertEvidence(evidence)

    fun getProgressByCase(caseId: Long): Flow<List<CaseProgressEntity>> =
        caseProgressDao.getProgressByCase(caseId)

    suspend fun addProgress(progress: CaseProgressEntity): Long =
        caseProgressDao.insertProgress(progress)
}

// ==================== 诈骗数据收集仓库 ====================

class FraudReportRepository(
    private val fraudReportDao: FraudReportDao
) {
    fun getAllReports(): Flow<List<FraudReportEntity>> = fraudReportDao.getAllReports()

    suspend fun getReportsByPhone(phoneNumber: String): List<FraudReportEntity> =
        fraudReportDao.getReportsByPhone(phoneNumber)

    fun getReportsByType(fraudType: String): Flow<List<FraudReportEntity>> =
        fraudReportDao.getReportsByType(fraudType)

    suspend fun submitReport(report: FraudReportEntity): Long = fraudReportDao.insertReport(report)
}
