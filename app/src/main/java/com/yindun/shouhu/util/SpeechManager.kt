package com.yindun.shouhu.util

import android.content.Intent

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * 语音管理器
 * 处理语音识别和TTS合成
 */
class SpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    // 当前支持的方言
    private var currentDialect: Dialect = Dialect.MANDARIN

    // 语音识别状态
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState

    // 识别结果
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    // TTS播放状态
    private val _ttsState = MutableStateFlow<TtsState>(TtsState.Idle)
    val ttsState: StateFlow<TtsState> = _ttsState

    init {
        initTts()
    }

    /**
     * 初始化TTS
     */
    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _ttsState.value = TtsState.Playing
                    }

                    override fun onDone(utteranceId: String?) {
                        _ttsState.value = TtsState.Completed
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _ttsState.value = TtsState.Error("播放失败")
                    }
                })
                setDialect(currentDialect)
            }
        }
    }

    /**
     * 设置方言
     */
    fun setDialect(dialect: Dialect) {
        currentDialect = dialect
        val locale = when (dialect) {
            Dialect.MANDARIN -> Locale.CHINA
            Dialect.CANTONESE -> Locale("zh", "HK")
            Dialect.SHANGHAI -> Locale("zh", "CN")
            Dialect.SICHUAN -> Locale("zh", "CN")
            Dialect.HENAN -> Locale("zh", "CN")
            Dialect.SHANDONG -> Locale("zh", "CN")
            Dialect.HUBEI -> Locale("zh", "CN")
            Dialect.HUNAN -> Locale("zh", "CN")
            Dialect.FUJIAN -> Locale("zh", "CN")
            Dialect.ANHUI -> Locale("zh", "CN")
            Dialect.JIANGSU -> Locale("zh", "CN")
            Dialect.ZHEJIANG -> Locale("zh", "CN")
            Dialect.GUANGXI -> Locale("zh", "CN")
            Dialect.YUNNAN -> Locale("zh", "CN")
            Dialect.GUIZHOU -> Locale("zh", "CN")
            Dialect.XINJIANG -> Locale("zh", "CN")
            Dialect.TIBETAN -> Locale("bo", "CN")
            Dialect.MONGOLIAN -> Locale("mn", "MN")
            Dialect.KOREAN -> Locale.KOREA
            Dialect.VIETNAMESE -> Locale("vi", "VN")
            Dialect.JAPANESE -> Locale.JAPAN
            Dialect.ENGLISH -> Locale.US
            Dialect.OTHER -> Locale.CHINA
        }

        textToSpeech?.let { tts ->
            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 降级到普通话
                tts.setLanguage(Locale.CHINA)
            }
        }
    }

    /**
     * 开始语音识别
     */
    fun startRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _recognitionState.value = RecognitionState.Error("设备不支持语音识别")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = RecognitionState.Listening
            }

            override fun onBeginningOfSpeech() {
                _recognitionState.value = RecognitionState.Processing
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _recognitionState.value = RecognitionState.Processing
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                    SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                    SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                    SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别服务繁忙"
                    SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音输入超时"
                    else -> "未知错误"
                }
                _recognitionState.value = RecognitionState.Error(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                _recognizedText.value = text
                _recognitionState.value = RecognitionState.Result(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                _recognizedText.value = text
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer?.setRecognitionListener(recognitionListener)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLocaleForDialect(currentDialect))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
        _recognitionState.value = RecognitionState.Starting
    }

    /**
     * 停止语音识别
     */
    fun stopRecognition() {
        speechRecognizer?.stopListening()
        _recognitionState.value = RecognitionState.Idle
    }

    /**
     * TTS播放文本
     */
    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (!isTtsInitialized) {
            _ttsState.value = TtsState.Error("TTS未初始化")
            return
        }

        _ttsState.value = TtsState.Playing
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * 停止TTS播放
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        _ttsState.value = TtsState.Idle
    }

    /**
     * 设置TTS语速
     */
    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * 设置TTS音调
     */
    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /**
     * 释放资源
     */
    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    /**
     * 获取方言对应的Locale
     */
    private fun getLocaleForDialect(dialect: Dialect): Locale {
        return when (dialect) {
            Dialect.MANDARIN -> Locale.CHINA
            Dialect.CANTONESE -> Locale("zh", "HK")
            Dialect.SHANGHAI -> Locale("zh", "CN")
            Dialect.SICHUAN -> Locale("zh", "CN")
            Dialect.HENAN -> Locale("zh", "CN")
            Dialect.SHANDONG -> Locale("zh", "CN")
            Dialect.HUBEI -> Locale("zh", "CN")
            Dialect.HUNAN -> Locale("zh", "CN")
            Dialect.FUJIAN -> Locale("zh", "CN")
            Dialect.ANHUI -> Locale("zh", "CN")
            Dialect.JIANGSU -> Locale("zh", "CN")
            Dialect.ZHEJIANG -> Locale("zh", "CN")
            Dialect.GUANGXI -> Locale("zh", "CN")
            Dialect.YUNNAN -> Locale("zh", "CN")
            Dialect.GUIZHOU -> Locale("zh", "CN")
            Dialect.XINJIANG -> Locale("zh", "CN")
            Dialect.TIBETAN -> Locale("bo", "CN")
            Dialect.MONGOLIAN -> Locale("mn", "MN")
            Dialect.KOREAN -> Locale.KOREA
            Dialect.VIETNAMESE -> Locale("vi", "VN")
            Dialect.JAPANESE -> Locale.JAPAN
            Dialect.ENGLISH -> Locale.US
            Dialect.OTHER -> Locale.CHINA
        }
    }

    companion object {
        @Volatile
        private var instance: SpeechManager? = null

        fun getInstance(context: Context): SpeechManager {
            return instance ?: synchronized(this) {
                instance ?: SpeechManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * 方言枚举
 */
enum class Dialect(val displayName: String, val code: String) {
    MANDARIN("普通话", "zh-CN"),
    CANTONESE("粤语", "zh-HK"),
    SHANGHAI("上海话", "zh-SH"),
    SICHUAN("四川话", "zh-SC"),
    HENAN("河南话", "zh-HN"),
    SHANDONG("山东话", "zh-SD"),
    HUBEI("湖北话", "zh-HB"),
    HUNAN("湖南话", "zh-HN"),
    FUJIAN("福建话", "zh-FJ"),
    ANHUI("安徽话", "zh-AH"),
    JIANGSU("江苏话", "zh-JS"),
    ZHEJIANG("浙江话", "zh-ZJ"),
    GUANGXI("广西话", "zh-GX"),
    YUNNAN("云南话", "zh-YN"),
    GUIZHOU("贵州话", "zh-GZ"),
    XINJIANG("新疆话", "zh-XJ"),
    TIBETAN("藏语", "bo-CN"),
    MONGOLIAN("蒙古语", "mn-MN"),
    KOREAN("朝鲜语", "ko-KR"),
    VIETNAMESE("越南语", "vi-VN"),
    JAPANESE("日语", "ja-JP"),
    ENGLISH("英语", "en-US"),
    OTHER("其他", "zh-CN")
}

/**
 * 语音识别状态
 */
sealed class RecognitionState {
    data object Idle : RecognitionState()
    data object Starting : RecognitionState()
    data object Listening : RecognitionState()
    data object Processing : RecognitionState()
    data class Result(val text: String) : RecognitionState()
    data class Error(val message: String) : RecognitionState()
}

/**
 * TTS播放状态
 */
sealed class TtsState {
    data object Idle : TtsState()
    data object Playing : TtsState()
    data object Completed : TtsState()
    data class Error(val message: String) : TtsState()
}
