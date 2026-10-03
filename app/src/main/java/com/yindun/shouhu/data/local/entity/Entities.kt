package com.yindun.shouhu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

// ==================== 用户相关 ====================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val idNumber: String,  // 身份证号（加密存储）
    val isElderly: Boolean = true,  // 是否是老年人
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_links")
data class FamilyLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val elderlyUserId: Long,
    val childUserId: Long,
    val relationship: String,  // 'child', 'volunteer', 'community'
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 财务相关 ====================

@Entity(
    tableName = "accounts",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val bankName: String,
    val accountNumber: String,  // 加密存储
    val accountType: String,  // 'bank_card', 'wechat', 'alipay'
    val balance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["id"],
        childColumns = ["accountId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("accountId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val amount: Double,
    val counterparty: String,
    val description: String,
    val transactionTime: Long,
    val isSuspicious: Boolean = false,
    val riskScore: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "risk_rules")
data class RiskRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleName: String,
    val ruleType: String,  // 'amount_threshold', 'frequency', 'pattern'
    val parameters: String,  // JSON格式
    val riskLevel: Int,  // 1-10
    val isActive: Boolean = true
)

// ==================== 诈骗训练相关 ====================

@Entity(tableName = "fraud_scripts")
data class FraudScriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,  // 'health_product', 'investment', 'impersonate', 'lottery', 'family_emergency'
    val title: String,
    val scriptContent: String,  // JSON格式的对话流程
    val keywords: String,  // JSON格式的关键词列表
    val riskLevel: Int,
    val dialect: String = "mandarin",  // 方言
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "training_records",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = FraudScriptEntity::class,
        parentColumns = ["id"],
        childColumns = ["scriptId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("scriptId")]
)
data class TrainingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val scriptId: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val score: Int = 0,  // 识别准确度 0-100
    val identifiedKeywords: String = "[]",  // JSON格式
    val rewardAmount: Double = 0.0,
    val completed: Boolean = false
)

@Entity(
    tableName = "points_account",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class PointsAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val balance: Double = 0.0,
    val totalEarned: Double = 0.0,
    val totalSpent: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

// ==================== 通话相关 ====================

@Entity(
    tableName = "call_logs",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val phoneNumber: String,
    val callTime: Long,
    val duration: Int = 0,
    val riskLevel: Int = 0,  // 0-10
    val riskReason: String = "",
    val isWhitelisted: Boolean = false,
    val action: String = "none"  // 'none', 'warned', 'blocked'
)

@Entity(
    tableName = "whitelist",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val phoneNumber: String,
    val contactName: String,
    val relationship: String  // 'child', 'community', 'volunteer', 'other'
)

@Entity(tableName = "phone_risk_db")
data class PhoneRiskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val riskType: String,
    val reportCount: Int = 1,
    val lastReportTime: Long = System.currentTimeMillis()
)

// ==================== 案件相关 ====================

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseNumber: String,  // 案件编号
    val reporterId: Long,
    val reporterType: String,  // 'elderly', 'child'
    val caseType: String,
    val amount: Double = 0.0,
    val suspectInfo: String = "{}",  // JSON格式
    val description: String,
    val status: String = "submitted",  // 'submitted', 'processing', 'resolved', 'closed'
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "evidence",
    foreignKeys = [ForeignKey(
        entity = CaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["caseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("caseId")]
)
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long,
    val evidenceType: String,  // 'recording', 'screenshot', 'transfer_record', 'chat_screenshot', 'webpage'
    val filePath: String,
    val fileHash: String,
    val uploadedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "case_progress",
    foreignKeys = [ForeignKey(
        entity = CaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["caseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("caseId")]
)
data class CaseProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long,
    val status: String,
    val description: String,
    val operator: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ==================== 诈骗数据收集 ====================

@Entity(tableName = "fraud_reports")
data class FraudReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val fraudType: String,
    val fraudContent: String,
    val location: String = "",
    val reportTime: Long = System.currentTimeMillis(),
    val isAnonymous: Boolean = true
)
