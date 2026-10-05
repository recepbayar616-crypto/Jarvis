package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.preferences.JarvisPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AssistantVoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

class JarvisVoiceManager(
    private val context: Context,
    private val preferences: JarvisPreferences,
    private val onVoiceInputRecognized: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val _voiceState = MutableStateFlow(AssistantVoiceState.IDLE)
    val voiceState: StateFlow<AssistantVoiceState> = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _statusMessage = MutableStateFlow("JARVIS Çevrimiçi")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.forLanguageTag("tr-TR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("VoiceManager", "Turkish language not supported in TTS, falling back to default")
                textToSpeech?.setLanguage(Locale.getDefault())
            }
            textToSpeech?.setPitch(preferences.getSpeechPitch())
            textToSpeech?.setSpeechRate(preferences.getSpeechRate())

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = AssistantVoiceState.SPEAKING
                    _statusMessage.value = "JARVIS Konuşuyor..."
                }

                override fun onDone(utteranceId: String?) {
                    _voiceState.value = AssistantVoiceState.IDLE
                    _statusMessage.value = "JARVIS Hazır"
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _voiceState.value = AssistantVoiceState.IDLE
                    _statusMessage.value = "JARVIS Hazır"
                }
            })
            isTtsInitialized = true
        } else {
            Log.e("VoiceManager", "TTS initialization failed")
        }
    }

    fun startListening() {
        stopSpeaking()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _statusMessage.value = "Ses tanıma bu cihazda desteklenmiyor."
            _voiceState.value = AssistantVoiceState.ERROR
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@JarvisVoiceManager)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
            _voiceState.value = AssistantVoiceState.LISTENING
            _statusMessage.value = "Dinliyorum, efendim..."
        } catch (e: Exception) {
            Log.e("VoiceManager", "Start listening error", e)
            _voiceState.value = AssistantVoiceState.ERROR
            _statusMessage.value = "Mikrofon başlatılamadı."
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            if (_voiceState.value == AssistantVoiceState.LISTENING) {
                _voiceState.value = AssistantVoiceState.IDLE
                _statusMessage.value = "JARVIS Hazır"
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Stop listening error", e)
        }
    }

    fun setThinking() {
        _voiceState.value = AssistantVoiceState.THINKING
        _statusMessage.value = "Analiz ediliyor..."
    }

    fun speak(text: String) {
        if (!preferences.isAutoSpeakEnabled() || text.isBlank()) {
            _voiceState.value = AssistantVoiceState.IDLE
            return
        }

        try {
            stopListening()
            if (isTtsInitialized && textToSpeech != null) {
                textToSpeech?.setPitch(preferences.getSpeechPitch())
                textToSpeech?.setSpeechRate(preferences.getSpeechRate())

                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_RESPONSE_${System.currentTimeMillis()}")
                }
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_RESPONSE")
            } else {
                _voiceState.value = AssistantVoiceState.IDLE
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error in speak()", e)
            _voiceState.value = AssistantVoiceState.IDLE
        }
    }

    fun stopSpeaking() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error in stopSpeaking()", e)
        } finally {
            if (_voiceState.value == AssistantVoiceState.SPEAKING) {
                _voiceState.value = AssistantVoiceState.IDLE
                _statusMessage.value = "JARVIS Hazır"
            }
        }
    }

    // SpeechRecognizer Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.value = AssistantVoiceState.LISTENING
        _statusMessage.value = "Dinliyorum..."
    }

    override fun onBeginningOfSpeech() {
        _statusMessage.value = "Ses algılandı..."
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Map dB (-2 to 10 typical) to 0f - 1f range for UI waveform
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _audioRms.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _voiceState.value = AssistantVoiceState.THINKING
        _statusMessage.value = "Analiz ediliyor..."
    }

    override fun onError(error: Int) {
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "Ses anlaşılamadı, tekrar deneyin."
            SpeechRecognizer.ERROR_NETWORK -> "Ağ bağlantı hatası."
            SpeechRecognizer.ERROR_AUDIO -> "Ses yakalama hatası."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon izni gerekli."
            else -> "Dinleme zaman aşımı."
        }
        _audioRms.value = 0f
        _voiceState.value = AssistantVoiceState.IDLE
        _statusMessage.value = errorMsg
    }

    override fun onResults(results: Bundle?) {
        _audioRms.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            _recognizedText.value = text
            _voiceState.value = AssistantVoiceState.THINKING
            _statusMessage.value = "İşleniyor: \"$text\""
            onVoiceInputRecognized(text)
        } else {
            _voiceState.value = AssistantVoiceState.IDLE
            _statusMessage.value = "JARVIS Hazır"
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()
        if (!text.isNullOrBlank()) {
            _recognizedText.value = text
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Destroy error", e)
        }
    }
}
