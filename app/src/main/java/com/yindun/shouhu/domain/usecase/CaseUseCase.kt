package com.yindun.shouhu.domain.usecase

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.CaseEntity
import com.yindun.shouhu.data.local.entity.CaseProgressEntity
import com.yindun.shouhu.data.local.entity.EvidenceEntity
import com.yindun.shouhu.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 案件管理用例
 * 处理报案提交、进度查询、证据管理等业务逻辑
 */
class CaseUseCase(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }
    private val pdfGenerator by lazy { PdfGenerator.getInstance(context) }

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
    private val caseNumberFormat = SimpleDateFormat("yyyyMMdd", Locale.CHINA)

    /**
     * 提交报案（老年人自主提交）
     */
    suspend fun submitCase(
        reporterId: Long,
        caseType: String,
        amount: Double,
        suspectPhone: String,
        suspectInfo: String,
        description: String,
        evidencePaths: List<String> = emptyList()
    ): CaseSubmitResult = withContext(Dispatchers.IO) {
        try {
            // 生成案件编号
            val caseNumber = generateCaseNumber()

            // 创建案件
            val case = CaseEntity(
                caseNumber = caseNumber,
                reporterId = reporterId,
                reporterType = "elderly",
                caseType = caseType,
                amount = amount,
                suspectInfo = org.json.JSONObject().apply {
                    put("phone", suspectPhone)
                    put("info", suspectInfo)
                }.toString(),
                description = description,
                status = "submitted",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val caseId = database.caseDao().insertCase(case)

            // 添加证据
            evidencePaths.forEach { path ->
                val evidence = EvidenceEntity(
                    caseId = caseId,
                    evidenceType = "file",
                    filePath = path,
                    fileHash = "", // 实际应该计算文件哈希
                    uploadedAt = System.currentTimeMillis()
                )
                database.evidenceDao().insertEvidence(evidence)
            }

            // 添加初始进度
            val initialProgress = CaseProgressEntity(
                caseId = caseId,
                status = "submitted",
                description = "案件已提交，等待审核",
                operator = "系统",
                timestamp = System.currentTimeMillis()
            )
            database.caseProgressDao().insertProgress(initialProgress)

            // 生成PDF材料
            val caseInfo = com.yindun.shouhu.util.CaseInfo(
                caseNumber = caseNumber,
                fraudType = caseType,
                amount = amount,
                riskLevel = "待评估",
                victimName = "", // 需要从用户信息获取
                victimPhone = "",
                victimAddress = "",
                suspectPhone = suspectPhone,
                suspectInfo = suspectInfo,
                description = description,
                evidences = evidencePaths.map { path ->
                    com.yindun.shouhu.util.EvidenceInfo(
                        type = "文件",
                        description = path.substringAfterLast("/"),
                        filePath = path
                    )
                }
            )

            val pdfFile = pdfGenerator.generateCaseSummaryPdf(caseInfo)

            CaseSubmitResult(
                success = true,
                caseId = caseId,
                caseNumber = caseNumber,
                pdfPath = pdfFile.absolutePath,
                message = "案件提交成功"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CaseSubmitResult(
                success = false,
                caseId = -1,
                caseNumber = "",
                pdfPath = null,
                message = "案件提交失败: ${e.message}"
            )
        }
    }

    /**
     * 子女代为提交线索
     */
    suspend fun submitCaseByChild(
        childUserId: Long,
        elderlyUserId: Long,
        caseType: String,
        amount: Double,
        suspectPhone: String,
        suspectInfo: String,
        description: String,
        additionalInfo: String,
        evidencePaths: List<String> = emptyList()
    ): CaseSubmitResult = withContext(Dispatchers.IO) {
        try {
            val caseNumber = generateCaseNumber()

            // 创建案件
            val case = CaseEntity(
                caseNumber = caseNumber,
                reporterId = childUserId,
                reporterType = "child",
                caseType = caseType,
                amount = amount,
                suspectInfo = org.json.JSONObject().apply {
                    put("phone", suspectPhone)
                    put("info", suspectInfo)
                    put("elderlyUserId", elderlyUserId)
                    put("additionalInfo", additionalInfo)
                    put("submittedBy", "child")
                }.toString(),
                description = description,
                status = "submitted",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val caseId = database.caseDao().insertCase(case)

            // 添加证据
            evidencePaths.forEach { path ->
                val evidence = EvidenceEntity(
                    caseId = caseId,
                    evidenceType = "file",
                    filePath = path,
                    fileHash = "",
                    uploadedAt = System.currentTimeMillis()
                )
                database.evidenceDao().insertEvidence(evidence)
            }

            // 添加初始进度
            val initialProgress = CaseProgressEntity(
                caseId = caseId,
                status = "submitted",
                description = "由守护人代为提交，等待审核",
                operator = "系统",
                timestamp = System.currentTimeMillis()
            )
            database.caseProgressDao().insertProgress(initialProgress)

            CaseSubmitResult(
                success = true,
                caseId = caseId,
                caseNumber = caseNumber,
                pdfPath = null,
                message = "案件提交成功"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CaseSubmitResult(
                success = false,
                caseId = -1,
                caseNumber = "",
                pdfPath = null,
                message = "案件提交失败: ${e.message}"
            )
        }
    }

    /**
     * 获取用户的案件列表
     */
    fun getCasesByUser(userId: Long): Flow<List<CaseEntity>> {
        return database.caseDao().getCasesByUser(userId)
    }

    /**
     * 获取家庭成员的案件列表（子女查看父母的案件）
     */
    fun getCasesByFamilyMember(childUserId: Long): Flow<List<CaseEntity>> {
        return database.caseDao().getCasesByFamilyMember(childUserId)
    }

    /**
     * 获取案件详情
     */
    suspend fun getCaseById(caseId: Long): CaseEntity? {
        return database.caseDao().getCaseById(caseId)
    }

    /**
     * 获取案件进度
     */
    fun getCaseProgress(caseId: Long): Flow<List<CaseProgressEntity>> {
        return database.caseProgressDao().getProgressByCase(caseId)
    }

    /**
     * 获取案件证据
     */
    fun getCaseEvidence(caseId: Long): Flow<List<EvidenceEntity>> {
        return database.evidenceDao().getEvidenceByCase(caseId)
    }

    /**
     * 添加证据
     */
    suspend fun addEvidence(
        caseId: Long,
        evidenceType: String,
        filePath: String,
        description: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val evidence = EvidenceEntity(
            caseId = caseId,
            evidenceType = evidenceType,
            filePath = filePath,
            fileHash = "", // 实际应该计算哈希
            uploadedAt = System.currentTimeMillis()
        )
        database.evidenceDao().insertEvidence(evidence)
    }

    /**
     * 添加进度记录
     */
    suspend fun addProgress(
        caseId: Long,
        status: String,
        description: String,
        operator: String
    ): Long = withContext(Dispatchers.IO) {
        val progress = CaseProgressEntity(
            caseId = caseId,
            status = status,
            description = description,
            operator = operator,
            timestamp = System.currentTimeMillis()
        )
        database.caseProgressDao().insertProgress(progress)
    }

    /**
     * 更新案件状态
     */
    suspend fun updateCaseStatus(
        caseId: Long,
        status: String
    ) = withContext(Dispatchers.IO) {
        val case = database.caseDao().getCaseById(caseId)
        case?.let {
            database.caseDao().updateCase(
                it.copy(
                    status = status,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * 生成案件编号
     */
    private fun generateCaseNumber(): String {
        val dateStr = caseNumberFormat.format(Date())
        val random = UUID.randomUUID().toString().substring(0, 6).uppercase()
        return "YD$dateStr$random"
    }

    /**
     * 获取案件统计数据
     */
    suspend fun getCaseStats(userId: Long): CaseStats = withContext(Dispatchers.Default) {
        // 这里简化处理，实际应该通过DAO查询统计
        CaseStats(
            totalCases = 0,
            submittedCases = 0,
            processingCases = 0,
            resolvedCases = 0
        )
    }

    companion object {
        @Volatile
        private var instance: CaseUseCase? = null

        fun getInstance(context: Context): CaseUseCase {
            return instance ?: synchronized(this) {
                instance ?: CaseUseCase(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 案件提交结果
 */
data class CaseSubmitResult(
    val success: Boolean,
    val caseId: Long,
    val caseNumber: String,
    val pdfPath: String?,
    val message: String
)

/**
 * 案件统计
 */
data class CaseStats(
    val totalCases: Int,
    val submittedCases: Int,
    val processingCases: Int,
    val resolvedCases: Int
)
