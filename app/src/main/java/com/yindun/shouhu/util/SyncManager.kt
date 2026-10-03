package com.yindun.shouhu.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.yindun.shouhu.data.local.entity.FraudScriptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 同步管理器
 * 负责与服务器同步诈骗话术库
 */
class SyncManager(private val context: Context) {

    private val baseUrl = "https://api.yindunshouhu.com/v1" // 示例API地址
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    /**
     * 检查网络连接
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * 同步诈骗话术库
     * @param lastSyncTime 上次同步时间
     * @return 新增或更新的话术列表
     */
    suspend fun syncFraudScripts(
        lastSyncTime: Long = 0
    ): List<FraudScriptEntity> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext emptyList()
        }

        try {
            val url = URL("$baseUrl/fraud-scripts/sync?lastSyncTime=$lastSyncTime")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                parseFraudScriptsResponse(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 解析话术库响应
     */
    private fun parseFraudScriptsResponse(response: String): List<FraudScriptEntity> {
        val scripts = mutableListOf<FraudScriptEntity>()

        try {
            val json = JSONObject(response)
            val data = json.getJSONArray("data")

            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                scripts.add(
                    FraudScriptEntity(
                        id = item.getLong("id"),
                        category = item.getString("category"),
                        title = item.getString("title"),
                        scriptContent = item.getString("scriptContent"),
                        keywords = item.getString("keywords"),
                        riskLevel = item.getInt("riskLevel"),
                        dialect = item.optString("dialect", "mandarin"),
                        createdAt = dateFormat.parse(item.getString("createdAt"))?.time ?: System.currentTimeMillis(),
                        updatedAt = dateFormat.parse(item.getString("updatedAt"))?.time ?: System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return scripts
    }

    /**
     * 上传诈骗报告
     * @param phoneNumber 诈骗电话
     * @param fraudType 诈骗类型
     * @param content 诈骗内容
     * @param location 位置信息
     * @param isAnonymous 是否匿名
     */
    suspend fun uploadFraudReport(
        phoneNumber: String,
        fraudType: String,
        content: String,
        location: String = "",
        isAnonymous: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext false
        }

        try {
            val jsonBody = JSONObject().apply {
                put("phoneNumber", phoneNumber)
                put("fraudType", fraudType)
                put("content", content)
                put("location", location)
                put("isAnonymous", isAnonymous)
                put("timestamp", dateFormat.format(Date()))
            }

            val url = URL("$baseUrl/fraud-reports")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            connection.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray())
            }

            connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_CREATED
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取诈骗电话风险数据
     * @param phoneNumber 电话号码
     */
    suspend fun getPhoneRiskData(phoneNumber: String): PhoneRiskData? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext null
        }

        try {
            val url = URL("$baseUrl/phone-risk/$phoneNumber")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                parsePhoneRiskResponse(response)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 解析电话风险数据响应
     */
    private fun parsePhoneRiskResponse(response: String): PhoneRiskData? {
        return try {
            val json = JSONObject(response)
            val data = json.getJSONObject("data")

            PhoneRiskData(
                phoneNumber = data.getString("phoneNumber"),
                riskLevel = data.getInt("riskLevel"),
                reportCount = data.getInt("reportCount"),
                location = data.optString("location", ""),
                fraudTypes = data.optJSONArray("fraudTypes")?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                } ?: emptyList()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取最新诈骗案例
     * @param category 诈骗类型
     * @param limit 数量限制
     */
    suspend fun getLatestFraudCases(
        category: String? = null,
        limit: Int = 10
    ): List<FraudCase> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext emptyList()
        }

        try {
            val urlString = buildString {
                append("$baseUrl/fraud-cases?limit=$limit")
                category?.let { append("&category=$it") }
            }

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                parseFraudCasesResponse(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 解析诈骗案例响应
     */
    private fun parseFraudCasesResponse(response: String): List<FraudCase> {
        val cases = mutableListOf<FraudCase>()

        try {
            val json = JSONObject(response)
            val data = json.getJSONArray("data")

            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                cases.add(
                    FraudCase(
                        id = item.getLong("id"),
                        title = item.getString("title"),
                        category = item.getString("category"),
                        description = item.getString("description"),
                        location = item.optString("location", ""),
                        riskLevel = item.getInt("riskLevel"),
                        createdAt = dateFormat.parse(item.getString("createdAt"))?.time ?: System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cases
    }

    /**
     * 获取统计数据
     */
    suspend fun getStatistics(): SyncStatistics? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext null
        }

        try {
            val url = URL("$baseUrl/statistics")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                parseStatisticsResponse(response)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 解析统计数据响应
     */
    private fun parseStatisticsResponse(response: String): SyncStatistics? {
        return try {
            val json = JSONObject(response)
            val data = json.getJSONObject("data")

            SyncStatistics(
                totalReports = data.getInt("totalReports"),
                todayReports = data.getInt("todayReports"),
                blockedCalls = data.getInt("blockedCalls"),
                savedUsers = data.getInt("savedUsers")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        @Volatile
        private var instance: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 电话风险数据
 */
data class PhoneRiskData(
    val phoneNumber: String,
    val riskLevel: Int,
    val reportCount: Int,
    val location: String,
    val fraudTypes: List<String>
)

/**
 * 诈骗案例
 */
data class FraudCase(
    val id: Long,
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val riskLevel: Int,
    val createdAt: Long
)

/**
 * 同步统计数据
 */
data class SyncStatistics(
    val totalReports: Int,
    val todayReports: Int,
    val blockedCalls: Int,
    val savedUsers: Int
)
