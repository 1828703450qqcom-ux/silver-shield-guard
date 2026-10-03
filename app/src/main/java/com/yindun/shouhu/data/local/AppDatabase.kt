package com.yindun.shouhu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yindun.shouhu.data.local.dao.*
import com.yindun.shouhu.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        FamilyLinkEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        RiskRuleEntity::class,
        FraudScriptEntity::class,
        TrainingRecordEntity::class,
        PointsAccountEntity::class,
        CallLogEntity::class,
        WhitelistEntity::class,
        PhoneRiskEntity::class,
        CaseEntity::class,
        EvidenceEntity::class,
        CaseProgressEntity::class,
        FraudReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun familyLinkDao(): FamilyLinkDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun riskRuleDao(): RiskRuleDao
    abstract fun fraudScriptDao(): FraudScriptDao
    abstract fun trainingRecordDao(): TrainingRecordDao
    abstract fun pointsAccountDao(): PointsAccountDao
    abstract fun callLogDao(): CallLogDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun phoneRiskDao(): PhoneRiskDao
    abstract fun caseDao(): CaseDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun caseProgressDao(): CaseProgressDao
    abstract fun fraudReportDao(): FraudReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yindun_shouhu_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
