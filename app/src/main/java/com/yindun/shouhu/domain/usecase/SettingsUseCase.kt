package com.yindun.shouhu.domain.usecase

import android.content.Context
import android.content.SharedPreferences
import com.yindun.shouhu.data.local.AppDatabase
import com.yindun.shouhu.data.local.entity.UserEntity
import com.yindun.shouhu.util.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 设置管理用例
 * 处理用户配置、隐私设置、系统设置等
 */
class SettingsUseCase(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }
    private val cryptoManager by lazy { CryptoManager.getInstance(context) }
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    companion object {
        // 设置键
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_DIALECT = "dialect"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_VOICE_ALERT_ENABLED = "voice_alert_enabled"
        const val KEY_SPEECH_RATE = "speech_rate"
        const val KEY_ELDERLY_MODE = "elderly_mode"
        const val KEY_AUTO_SYNC = "auto_sync"
        const val KEY_DATA_ENCRYPTION = "data_encryption"

        // 字体大小选项
        const val FONT_SIZE_SMALL = 0
        const val FONT_SIZE_NORMAL = 1
        const val FONT_SIZE_LARGE = 2
        const val FONT_SIZE_EXTRA_LARGE = 3

        // 默认值
        const val DEFAULT_FONT_SIZE = FONT_SIZE_LARGE
        const val DEFAULT_DIALECT = "mandarin"
        const val DEFAULT_SPEECH_RATE = 1.0f
    }

    // ==================== 用户信息管理 ====================

    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(userId: Long): UserEntity? = withContext(Dispatchers.IO) {
        database.userDao().getUserById(userId)
    }

    /**
     * 创建用户
     */
    suspend fun createUser(
        name: String,
        phone: String,
        idNumber: String,
        isElderly: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        val encryptedIdNumber = cryptoManager.encrypt(idNumber)
        val user = UserEntity(
            name = name,
            phone = phone,
            idNumber = encryptedIdNumber,
            isElderly = isElderly
        )
        database.userDao().insertUser(user)
    }

    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo(user: UserEntity) = withContext(Dispatchers.IO) {
        database.userDao().updateUser(user)
    }

    /**
     * 删除用户
     */
    suspend fun deleteUser(userId: Long) = withContext(Dispatchers.IO) {
        val user = database.userDao().getUserById(userId)
        user?.let {
            database.userDao().deleteUser(it)
        }
    }

    // ==================== 字体设置 ====================

    /**
     * 获取字体大小
     */
    fun getFontSize(): Int {
        return prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
    }

    /**
     * 设置字体大小
     */
    fun setFontSize(size: Int) {
        prefs.edit().putInt(KEY_FONT_SIZE, size).apply()
    }

    /**
     * 获取字体大小的缩放比例
     */
    fun getFontScale(): Float {
        return when (getFontSize()) {
            FONT_SIZE_SMALL -> 0.85f
            FONT_SIZE_NORMAL -> 1.0f
            FONT_SIZE_LARGE -> 1.2f
            FONT_SIZE_EXTRA_LARGE -> 1.5f
            else -> 1.2f
        }
    }

    // ==================== 方言设置 ====================

    /**
     * 获取当前方言
     */
    fun getDialect(): String {
        return prefs.getString(KEY_DIALECT, DEFAULT_DIALECT) ?: DEFAULT_DIALECT
    }

    /**
     * 设置方言
     */
    fun setDialect(dialect: String) {
        prefs.edit().putString(KEY_DIALECT, dialect).apply()
    }

    /**
     * 获取所有支持的方言
     */
    fun getSupportedDialects(): List<DialectOption> {
        return listOf(
            DialectOption("mandarin", "普通话", "zh-CN"),
            DialectOption("cantonese", "粤语", "zh-HK"),
            DialectOption("shanghainese", "上海话", "zh-SH"),
            DialectOption("sichuanese", "四川话", "zh-SC"),
            DialectOption("henanese", "河南话", "zh-HN"),
            DialectOption("shandong", "山东话", "zh-SD"),
            DialectOption("hubei", "湖北话", "zh-HB"),
            DialectOption("hunan", "湖南话", "zh-HN"),
            DialectOption("fujian", "福建话", "zh-FJ"),
            DialectOption("anhui", "安徽话", "zh-AH"),
            DialectOption("jiangsu", "江苏话", "zh-JS"),
            DialectOption("zhejiang", "浙江话", "zh-ZJ"),
            DialectOption("guangxi", "广西话", "zh-GX"),
            DialectOption("yunnan", "云南话", "zh-YN"),
            DialectOption("guizhou", "贵州话", "zh-GZ"),
            DialectOption("xinjiang", "新疆话", "zh-XJ"),
            DialectOption("tibetan", "藏语", "bo-CN"),
            DialectOption("mongolian", "蒙古语", "mn-MN"),
            DialectOption("korean", "朝鲜语", "ko-KR"),
            DialectOption("vietnamese", "越南语", "vi-VN"),
            DialectOption("japanese", "日语", "ja-JP"),
            DialectOption("english", "英语", "en-US")
        )
    }

    // ==================== 显示设置 ====================

    /**
     * 获取深色模式状态
     */
    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * 设置深色模式
     */
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    /**
     * 获取适老化模式状态
     */
    fun isElderlyMode(): Boolean {
        return prefs.getBoolean(KEY_ELDERLY_MODE, true)
    }

    /**
     * 设置适老化模式
     */
    fun setElderlyMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ELDERLY_MODE, enabled).apply()
    }

    // ==================== 通知设置 ====================

    /**
     * 获取通知开关状态
     */
    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    /**
     * 设置通知开关
     */
    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    /**
     * 获取语音提醒状态
     */
    fun isVoiceAlertEnabled(): Boolean {
        return prefs.getBoolean(KEY_VOICE_ALERT_ENABLED, true)
    }

    /**
     * 设置语音提醒
     */
    fun setVoiceAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_ALERT_ENABLED, enabled).apply()
    }

    // ==================== 语音设置 ====================

    /**
     * 获取语速
     */
    fun getSpeechRate(): Float {
        return prefs.getFloat(KEY_SPEECH_RATE, DEFAULT_SPEECH_RATE)
    }

    /**
     * 设置语速
     */
    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate.coerceIn(0.5f, 2.0f)).apply()
    }

    // ==================== 数据设置 ====================

    /**
     * 获取数据加密状态
     */
    fun isDataEncryptionEnabled(): Boolean {
        return prefs.getBoolean(KEY_DATA_ENCRYPTION, true)
    }

    /**
     * 设置数据加密
     */
    fun setDataEncryptionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_ENCRYPTION, enabled).apply()
    }

    /**
     * 获取自动同步状态
     */
    fun isAutoSyncEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SYNC, true)
    }

    /**
     * 设置自动同步
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    // ==================== 数据管理 ====================

    /**
     * 删除所有本地数据
     */
    suspend fun deleteAllLocalData() = withContext(Dispatchers.IO) {
        // 清除SharedPreferences
        prefs.edit().clear().apply()

        // 清除数据库（需要重新创建）
        // 实际项目中可能需要更复杂的处理
    }

    /**
     * 导出用户数据
     */
    suspend fun exportUserData(userId: Long): String = withContext(Dispatchers.IO) {
        val user = database.userDao().getUserById(userId)
        val accounts = database.accountDao().getAccountsByUser(userId)
        // 构建JSON数据
        """
        {
            "user": {
                "name": "${user?.name}",
                "phone": "${user?.phone}"
            },
            "settings": {
                "fontSize": ${getFontSize()},
                "dialect": "${getDialect()}",
                "elderlyMode": ${isElderlyMode()}
            }
        }
        """.trimIndent()
    }

    /**
     * 获取应用版本信息
     */
    fun getAppVersion(): Pair<String, Int> {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            Pair(packageInfo.versionName ?: "1.0.0", packageInfo.versionCode)
        } catch (e: Exception) {
            Pair("1.0.0", 1)
        }
    }

    /**
     * 获取应用存储使用情况
     */
    fun getStorageUsage(): StorageUsage {
        val dbSize = context.getDatabasePath("yindun_shouhu_database").length()
        val prefsSize = 1024L // 估算
        val cacheSize = context.cacheDir.length()

        return StorageUsage(
            databaseSize = dbSize,
            preferencesSize = prefsSize,
            cacheSize = cacheSize,
            totalSize = dbSize + prefsSize + cacheSize
        )
    }
}

/**
 * 方言选项
 */
data class DialectOption(
    val code: String,
    val displayName: String,
    val locale: String
)

/**
 * 存储使用情况
 */
data class StorageUsage(
    val databaseSize: Long,
    val preferencesSize: Long,
    val cacheSize: Long,
    val totalSize: Long
) {
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    fun getFormattedDatabaseSize(): String = formatSize(databaseSize)
    fun getFormattedPreferencesSize(): String = formatSize(preferencesSize)
    fun getFormattedCacheSize(): String = formatSize(cacheSize)
    fun getFormattedTotalSize(): String = formatSize(totalSize)
}
