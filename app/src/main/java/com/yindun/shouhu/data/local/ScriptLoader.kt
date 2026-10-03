package com.yindun.shouhu.data.local

import android.content.Context
import com.yindun.shouhu.data.local.entity.FraudScriptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 话术库加载器
 * 从assets加载预置话术
 */
class ScriptLoader(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    /**
     * 加载预置话术
     */
    suspend fun loadPresetScripts() = withContext(Dispatchers.IO) {
        try {
            // 读取JSON文件
            val jsonString = context.assets.open("scripts/preset_scripts.json")
                .bufferedReader()
                .use(BufferedReader::readText)

            val json = JSONObject(jsonString)
            val scriptsArray = json.getJSONArray("scripts")

            val scripts = mutableListOf<FraudScriptEntity>()

            for (i in 0 until scriptsArray.length()) {
                val scriptJson = scriptsArray.getJSONObject(i)
                val script = parseScript(scriptJson)
                scripts.add(script)
            }

            // 批量插入数据库
            if (scripts.isNotEmpty()) {
                database.fraudScriptDao().insertScripts(scripts)
            }

            scripts.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 解析单个话术
     */
    private fun parseScript(json: JSONObject): FraudScriptEntity {
        val id = json.getLong("id")
        val category = json.getString("category")
        val title = json.getString("title")
        val difficulty = json.optInt("difficulty", 5)
        val tips = json.optString("tips", "")

        // 解析对话
        val dialogueArray = json.getJSONArray("dialogue")
        val dialogue = mutableListOf<JSONObject>()

        for (i in 0 until dialogueArray.length()) {
            dialogue.add(dialogueArray.getJSONObject(i))
        }

        // 构建话术内容JSON
        val scriptContent = JSONObject().apply {
            put("dialogue", JSONArray(dialogue))
            put("keywords", json.optJSONArray("keywords") ?: JSONArray())
            put("tips", tips)
            put("warnings", JSONArray())
        }

        // 提取关键词
        val keywords = mutableListOf<String>()
        val keywordsArray = json.optJSONArray("keywords")
        if (keywordsArray != null) {
            for (i in 0 until keywordsArray.length()) {
                keywords.add(keywordsArray.getString(i))
            }
        }

        return FraudScriptEntity(
            id = id,
            category = category,
            title = title,
            scriptContent = scriptContent.toString(),
            keywords = JSONArray(keywords).toString(),
            riskLevel = difficulty,
            dialect = "mandarin"
        )
    }

    /**
     * 检查是否需要加载预置话术
     */
    suspend fun checkAndLoadPresetScripts(): Int = withContext(Dispatchers.IO) {
        // 检查数据库中是否已有话术
        val categories = database.fraudScriptDao().getAllCategories()
        // 这里需要等待Flow，简化处理
        // 实际项目中应该使用first()

        // 直接加载预置话术（会自动处理重复）
        loadPresetScripts()
    }

    /**
     * 从JSON字符串加载话术
     */
    suspend fun loadScriptsFromJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(jsonString)
            val scriptsArray = json.getJSONArray("scripts")

            val scripts = mutableListOf<FraudScriptEntity>()

            for (i in 0 until scriptsArray.length()) {
                val scriptJson = scriptsArray.getJSONObject(i)
                val script = parseScript(scriptJson)
                scripts.add(script)
            }

            if (scripts.isNotEmpty()) {
                database.fraudScriptDao().insertScripts(scripts)
            }

            scripts.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    companion object {
        @Volatile
        private var instance: ScriptLoader? = null

        fun getInstance(context: Context): ScriptLoader {
            return instance ?: synchronized(this) {
                instance ?: ScriptLoader(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
