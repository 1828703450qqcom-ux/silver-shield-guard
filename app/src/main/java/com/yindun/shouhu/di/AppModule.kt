package com.yindun.shouhu.di

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.repository.*

object AppModule {

    private var database: AppDatabase? = null

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
    }

    private fun getDatabase(): AppDatabase {
        return database ?: throw IllegalStateException("AppModule not initialized")
    }

    // ==================== Repository 单例 ====================

    val userRepository: UserRepository by lazy {
        UserRepository(
            getDatabase().userDao(),
            getDatabase().familyLinkDao()
        )
    }

    val financialRepository: FinancialRepository by lazy {
        FinancialRepository(
            getDatabase().accountDao(),
            getDatabase().transactionDao(),
            getDatabase().riskRuleDao()
        )
    }

    val trainingRepository: TrainingRepository by lazy {
        TrainingRepository(
            getDatabase().fraudScriptDao(),
            getDatabase().trainingRecordDao(),
            getDatabase().pointsAccountDao()
        )
    }

    val callProtectionRepository: CallProtectionRepository by lazy {
        CallProtectionRepository(
            getDatabase().callLogDao(),
            getDatabase().whitelistDao(),
            getDatabase().phoneRiskDao()
        )
    }

    val caseRepository: CaseRepository by lazy {
        CaseRepository(
            getDatabase().caseDao(),
            getDatabase().evidenceDao(),
            getDatabase().caseProgressDao()
        )
    }

    val fraudReportRepository: FraudReportRepository by lazy {
        FraudReportRepository(getDatabase().fraudReportDao())
    }
}
