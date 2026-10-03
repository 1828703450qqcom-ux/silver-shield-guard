package com.yindun.shouhu.util.ai

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer

/**
 * ONNX Runtime 管理器
 * 负责加载和管理AI模型
 */
class OnnxRuntimeManager(private val context: Context) {

    private val ortEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private val sessionCache = mutableMapOf<String, OrtSession>()

    /**
     * 从assets加载模型
     * @param modelPath assets中的模型路径
     * @param modelName 模型名称（用于缓存）
     */
    suspend fun loadModelFromAssets(
        modelPath: String,
        modelName: String
    ): OrtSession = withContext(Dispatchers.IO) {
        sessionCache[modelName] ?: run {
            // 检查是否已解压到内部存储
            val internalFile = File(context.filesDir, "models/$modelName")
            if (!internalFile.exists()) {
                // 从assets解压模型
                internalFile.parentFile?.mkdirs()
                context.assets.open(modelPath).use { input ->
                    FileOutputStream(internalFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            // 创建会话
            val session = ortEnvironment.createSession(internalFile.absolutePath)
            sessionCache[modelName] = session
            session
        }
    }

    /**
     * 从文件路径加载模型
     * @param modelFile 模型文件
     * @param modelName 模型名称（用于缓存）
     */
    suspend fun loadModelFromFile(
        modelFile: File,
        modelName: String
    ): OrtSession = withContext(Dispatchers.IO) {
        sessionCache[modelName] ?: run {
            val session = ortEnvironment.createSession(modelFile.absolutePath)
            sessionCache[modelName] = session
            session
        }
    }

    /**
     * 推理执行
     * @param session ONNX会话
     * @param inputName 输入名称
     * @param inputData 输入数据
     * @param shape 输入形状
     */
    suspend fun runInference(
        session: OrtSession,
        inputName: String,
        inputData: FloatArray,
        shape: LongArray
    ): FloatArray = withContext(Dispatchers.Default) {
        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(inputData),
            shape
        )

        val inputMap = mapOf(inputName to inputTensor)
        val results = session.run(inputMap)

        // 获取输出结果
        val outputTensor = results[0].value
        when (outputTensor) {
            is Array<*> -> when (val first = outputTensor.firstOrNull()) {
                is FloatArray -> first
                is Number -> outputTensor.mapNotNull { (it as? Number)?.toFloat() }.toFloatArray()
                else -> FloatArray(0)
            }
            is FloatArray -> outputTensor
            else -> FloatArray(0)
        }
    }

    /**
     * 推理执行（返回原始结果）
     */
    suspend fun runInferenceRaw(
        session: OrtSession,
        inputs: Map<String, OnnxTensor>
    ): List<Any?> = withContext(Dispatchers.Default) {
        val results = session.run(inputs)
        results.map { it.value }
    }

    /**
     * 释放模型会话
     */
    fun releaseSession(modelName: String) {
        sessionCache.remove(modelName)?.close()
    }

    /**
     * 释放所有会话
     */
    fun releaseAll() {
        sessionCache.values.forEach { it.close() }
        sessionCache.clear()
    }

    /**
     * 检查模型是否已加载
     */
    fun isModelLoaded(modelName: String): Boolean {
        return sessionCache.containsKey(modelName)
    }

    /**
     * 获取已加载的模型列表
     */
    fun getLoadedModels(): List<String> {
        return sessionCache.keys.toList()
    }

    companion object {
        @Volatile
        private var instance: OnnxRuntimeManager? = null

        fun getInstance(context: Context): OnnxRuntimeManager {
            return instance ?: synchronized(this) {
                instance ?: OnnxRuntimeManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
