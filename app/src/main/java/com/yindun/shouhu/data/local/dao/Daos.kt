package com.yindun.shouhu.data.local.dao

import androidx.room.*
import com.yindun.shouhu.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// ==================== 用户 DAO ====================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface FamilyLinkDao {
    @Query("SELECT * FROM family_links WHERE elderlyUserId = :elderlyUserId")
    suspend fun getFamilyLinks(elderlyUserId: Long): List<FamilyLinkEntity>

    @Query("SELECT * FROM family_links WHERE childUserId = :childUserId")
    suspend fun getLinkedElderly(childUserId: Long): List<FamilyLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyLink(link: FamilyLinkEntity): Long

    @Delete
    suspend fun deleteFamilyLink(link: FamilyLinkEntity)
}

// ==================== 财务 DAO ====================

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId")
    fun getAccountsByUser(userId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteAllAccountsByUser(userId: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY transactionTime DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId IN (SELECT id FROM accounts WHERE userId = :userId) ORDER BY transactionTime DESC LIMIT :limit")
    fun getRecentTransactions(userId: Long, limit: Int = 50): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSuspicious = 1 AND accountId IN (SELECT id FROM accounts WHERE userId = :userId) ORDER BY transactionTime DESC")
    fun getSuspiciousTransactions(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionTime BETWEEN :startTime AND :endTime AND accountId IN (SELECT id FROM accounts WHERE userId = :userId)")
    suspend fun getTransactionsByDateRange(userId: Long, startTime: Long, endTime: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}

@Dao
interface RiskRuleDao {
    @Query("SELECT * FROM risk_rules WHERE isActive = 1")
    suspend fun getActiveRules(): List<RiskRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RiskRuleEntity): Long

    @Update
    suspend fun updateRule(rule: RiskRuleEntity)

    @Delete
    suspend fun deleteRule(rule: RiskRuleEntity)
}

// ==================== 诈骗训练 DAO ====================

@Dao
interface FraudScriptDao {
    @Query("SELECT * FROM fraud_scripts WHERE category = :category")
    fun getScriptsByCategory(category: String): Flow<List<FraudScriptEntity>>

    @Query("SELECT * FROM fraud_scripts WHERE dialect = :dialect")
    fun getScriptsByDialect(dialect: String): Flow<List<FraudScriptEntity>>

    @Query("SELECT * FROM fraud_scripts WHERE id = :scriptId")
    suspend fun getScriptById(scriptId: Long): FraudScriptEntity?

    @Query("SELECT DISTINCT category FROM fraud_scripts")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: FraudScriptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScripts(scripts: List<FraudScriptEntity>)

    @Update
    suspend fun updateScript(script: FraudScriptEntity)

    @Delete
    suspend fun deleteScript(script: FraudScriptEntity)
}

@Dao
interface TrainingRecordDao {
    @Query("SELECT * FROM training_records WHERE userId = :userId ORDER BY startTime DESC")
    fun getTrainingHistory(userId: Long): Flow<List<TrainingRecordEntity>>

    @Query("SELECT * FROM training_records WHERE userId = :userId AND completed = 1")
    fun getCompletedTrainings(userId: Long): Flow<List<TrainingRecordEntity>>

    @Query("SELECT SUM(rewardAmount) FROM training_records WHERE userId = :userId AND completed = 1")
    suspend fun getTotalRewards(userId: Long): Double?

    @Query("SELECT COUNT(*) FROM training_records WHERE userId = :userId AND completed = 1")
    suspend fun getCompletedCount(userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TrainingRecordEntity): Long

    @Update
    suspend fun updateRecord(record: TrainingRecordEntity)

    @Delete
    suspend fun deleteRecord(record: TrainingRecordEntity)
}

@Dao
interface PointsAccountDao {
    @Query("SELECT * FROM points_account WHERE userId = :userId")
    suspend fun getPointsAccount(userId: Long): PointsAccountEntity?

    @Query("SELECT * FROM points_account WHERE userId = :userId")
    fun getPointsAccountFlow(userId: Long): Flow<PointsAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: PointsAccountEntity): Long

    @Update
    suspend fun updateAccount(account: PointsAccountEntity)
}

// ==================== 通话 DAO ====================

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs WHERE userId = :userId ORDER BY callTime DESC LIMIT :limit")
    fun getCallLogs(userId: Long, limit: Int = 100): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE userId = :userId AND riskLevel >= :minRiskLevel ORDER BY callTime DESC")
    fun getHighRiskCalls(userId: Long, minRiskLevel: Int = 5): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE phoneNumber = :phoneNumber ORDER BY callTime DESC LIMIT 1")
    suspend fun getLastCallByNumber(phoneNumber: String): CallLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity): Long

    @Update
    suspend fun updateCallLog(callLog: CallLogEntity)
}

@Dao
interface WhitelistDao {
    @Query("SELECT * FROM whitelist WHERE userId = :userId")
    fun getWhitelist(userId: Long): Flow<List<WhitelistEntity>>

    @Query("SELECT * FROM whitelist WHERE userId = :userId AND phoneNumber = :phoneNumber")
    suspend fun isInWhitelist(userId: Long, phoneNumber: String): WhitelistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WhitelistEntity): Long

    @Delete
    suspend fun deleteEntry(entry: WhitelistEntity)

    @Query("DELETE FROM whitelist WHERE userId = :userId")
    suspend fun clearWhitelist(userId: Long)
}

@Dao
interface PhoneRiskDao {
    @Query("SELECT * FROM phone_risk_db WHERE phoneNumber = :phoneNumber")
    suspend fun getPhoneRisk(phoneNumber: String): PhoneRiskEntity?

    @Query("SELECT * FROM phone_risk_db WHERE phoneNumber LIKE :pattern")
    suspend fun searchPhones(pattern: String): List<PhoneRiskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(phoneRisk: PhoneRiskEntity)

    @Update
    suspend fun updatePhoneRisk(phoneRisk: PhoneRiskEntity)
}

// ==================== 案件 DAO ====================

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases WHERE reporterId = :userId ORDER BY createdAt DESC")
    fun getCasesByUser(userId: Long): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :caseId")
    suspend fun getCaseById(caseId: Long): CaseEntity?

    @Query("SELECT * FROM cases WHERE caseNumber = :caseNumber")
    suspend fun getCaseByNumber(caseNumber: String): CaseEntity?

    @Query("SELECT * FROM cases WHERE reporterId IN (SELECT elderlyUserId FROM family_links WHERE childUserId = :childUserId) ORDER BY createdAt DESC")
    fun getCasesByFamilyMember(childUserId: Long): Flow<List<CaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(caseEntity: CaseEntity): Long

    @Update
    suspend fun updateCase(caseEntity: CaseEntity)

    @Delete
    suspend fun deleteCase(caseEntity: CaseEntity)
}

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence WHERE caseId = :caseId")
    fun getEvidenceByCase(caseId: Long): Flow<List<EvidenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceEntity): Long

    @Delete
    suspend fun deleteEvidence(evidence: EvidenceEntity)

    @Query("DELETE FROM evidence WHERE caseId = :caseId")
    suspend fun deleteAllEvidenceByCase(caseId: Long)
}

@Dao
interface CaseProgressDao {
    @Query("SELECT * FROM case_progress WHERE caseId = :caseId ORDER BY timestamp DESC")
    fun getProgressByCase(caseId: Long): Flow<List<CaseProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: CaseProgressEntity): Long
}

// ==================== 诈骗数据收集 DAO ====================

@Dao
interface FraudReportDao {
    @Query("SELECT * FROM fraud_reports ORDER BY reportTime DESC")
    fun getAllReports(): Flow<List<FraudReportEntity>>

    @Query("SELECT * FROM fraud_reports WHERE phoneNumber = :phoneNumber")
    suspend fun getReportsByPhone(phoneNumber: String): List<FraudReportEntity>

    @Query("SELECT * FROM fraud_reports WHERE fraudType = :fraudType")
    fun getReportsByType(fraudType: String): Flow<List<FraudReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: FraudReportEntity): Long

    @Delete
    suspend fun deleteReport(report: FraudReportEntity)
}
