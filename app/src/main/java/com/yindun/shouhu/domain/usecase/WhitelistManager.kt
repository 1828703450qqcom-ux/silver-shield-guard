package com.yindun.shouhu.domain.usecase

import android.content.Context
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.WhitelistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 白名单管理器
 * 管理来电白名单
 */
class WhitelistManager(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    /**
     * 获取用户的白名单
     */
    fun getWhitelist(userId: Long): Flow<List<WhitelistEntity>> {
        return database.whitelistDao().getWhitelist(userId)
    }

    /**
     * 检查号码是否在白名单中
     */
    suspend fun isInWhitelist(userId: Long, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        database.whitelistDao().isInWhitelist(userId, phoneNumber) != null
    }

    /**
     * 添加到白名单
     */
    suspend fun addToWhitelist(
        userId: Long,
        phoneNumber: String,
        contactName: String,
        relationship: String
    ): Long = withContext(Dispatchers.IO) {
        val entry = WhitelistEntity(
            userId = userId,
            phoneNumber = normalizePhoneNumber(phoneNumber),
            contactName = contactName,
            relationship = relationship
        )
        database.whitelistDao().insertEntry(entry)
    }

    /**
     * 从白名单移除
     */
    suspend fun removeFromWhitelist(entry: WhitelistEntity) = withContext(Dispatchers.IO) {
        database.whitelistDao().deleteEntry(entry)
    }

    /**
     * 清空白名单
     */
    suspend fun clearWhitelist(userId: Long) = withContext(Dispatchers.IO) {
        database.whitelistDao().clearWhitelist(userId)
    }

    /**
     * 批量添加到白名单
     */
    suspend fun batchAddToWhitelist(
        userId: Long,
        entries: List<WhitelistEntry>
    ): List<Long> = withContext(Dispatchers.IO) {
        entries.map { entry ->
            addToWhitelist(
                userId = userId,
                phoneNumber = entry.phoneNumber,
                contactName = entry.contactName,
                relationship = entry.relationship
            )
        }
    }

    /**
     * 标准化电话号码
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        // 移除所有非数字字符
        val digits = phoneNumber.replace(Regex("[^0-9]"), "")

        // 处理中国手机号
        return when {
            digits.length == 11 && digits.startsWith("1") -> digits
            digits.length == 13 && digits.startsWith("86") -> digits.substring(2)
            digits.length == 12 && digits.startsWith("086") -> digits.substring(3)
            else -> digits
        }
    }

    /**
     * 预设白名单模板
     */
    suspend fun loadPresetWhitelist(userId: Long) = withContext(Dispatchers.IO) {
        // 这里可以从服务器或本地预置数据加载
        // 暂时为空，用户可以手动添加
    }

    companion object {
        @Volatile
        private var instance: WhitelistManager? = null

        fun getInstance(context: Context): WhitelistManager {
            return instance ?: synchronized(this) {
                instance ?: WhitelistManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 白名单条目
 */
data class WhitelistEntry(
    val phoneNumber: String,
    val contactName: String,
    val relationship: String // 'child', 'spouse', 'sibling', 'parent', 'friend', 'community', 'volunteer', 'other'
) {
    companion object {
        val RELATIONSHIP_CHILD = "child"
        val RELATIONSHIP_SPOUSE = "spouse"
        val RELATIONSHIP_SIBLING = "sibling"
        val RELATIONSHIP_PARENT = "parent"
        val RELATIONSHIP_FRIEND = "friend"
        val RELATIONSHIP_COMMUNITY = "community"
        val RELATIONSHIP_VOLUNTEER = "volunteer"
        val RELATIONSHIP_OTHER = "other"

        fun getRelationshipDisplayName(relationship: String): String {
            return when (relationship) {
                RELATIONSHIP_CHILD -> "子女"
                RELATIONSHIP_SPOUSE -> "配偶"
                RELATIONSHIP_SIBLING -> "兄弟姐妹"
                RELATIONSHIP_PARENT -> "父母"
                RELATIONSHIP_FRIEND -> "朋友"
                RELATIONSHIP_COMMUNITY -> "社区"
                RELATIONSHIP_VOLUNTEER -> "志愿者"
                RELATIONSHIP_OTHER -> "其他"
                else -> relationship
            }
        }
    }
}
