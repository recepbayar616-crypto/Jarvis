package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    private val _channelIdFlow = MutableStateFlow(getChannelId())
    val channelIdFlow: StateFlow<String> = _channelIdFlow.asStateFlow()

    private val _isVoiceActiveFlow = MutableStateFlow(isAutoSpeakEnabled())
    val isVoiceActiveFlow: StateFlow<Boolean> = _isVoiceActiveFlow.asStateFlow()

    fun getApiKey(): String {
        val saved = prefs.getString(KEY_API_KEY, "") ?: ""
        if (saved.isNotBlank()) return saved
        return try {
            val key = BuildConfig.YOUTUBE_API_KEY
            if (key.isNotBlank()) key else DEFAULT_YOUTUBE_KEY
        } catch (e: Exception) {
            DEFAULT_YOUTUBE_KEY
        }
    }

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _apiKeyFlow.value = key.trim()
    }

    fun getOAuthToken(): String = prefs.getString(KEY_OAUTH_TOKEN, "") ?: ""
    fun setOAuthToken(token: String) {
        prefs.edit().putString(KEY_OAUTH_TOKEN, token.trim()).apply()
    }

    fun getChannelId(): String {
        val saved = prefs.getString(KEY_CHANNEL_ID, "") ?: ""
        if (saved.isNotBlank()) return saved
        return DEFAULT_CHANNEL_ID
    }
    fun setChannelId(channelId: String) {
        prefs.edit().putString(KEY_CHANNEL_ID, channelId.trim()).apply()
        _channelIdFlow.value = channelId.trim()
    }

    fun isAutoSpeakEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SPEAK, true)
    fun setAutoSpeakEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
        _isVoiceActiveFlow.value = enabled
    }

    fun getSpeechRate(): Float = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
    }

    fun getSpeechPitch(): Float = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_SPEECH_PITCH, pitch).apply()
    }

    fun isWakeWordListening(): Boolean = prefs.getBoolean(KEY_WAKE_WORD, false)
    fun setWakeWordListening(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
    }

    fun clearAll() {
        prefs.edit().remove(KEY_API_KEY).remove(KEY_OAUTH_TOKEN).remove(KEY_CHANNEL_ID).apply()
        _apiKeyFlow.value = getApiKey()
        _channelIdFlow.value = ""
    }

    companion object {
        const val DEFAULT_YOUTUBE_KEY = "AIzaSyC7EhWXYVQ3zDXFRy_Ir6h2ach-Nw9sF54"
        const val DEFAULT_CHANNEL_ID = "@polisvakasi"
        private const val KEY_API_KEY = "youtube_api_key"
        private const val KEY_OAUTH_TOKEN = "google_oauth_token"
        private const val KEY_CHANNEL_ID = "channel_id"
        private const val KEY_AUTO_SPEAK = "auto_speak_responses"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_WAKE_WORD = "wake_word_mode"
    }
}
