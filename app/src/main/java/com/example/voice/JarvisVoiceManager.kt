package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.ai.gemini.OpenAITtsClient
import com.example.data.preferences.JarvisPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

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
) {

    private val _voiceState =
        MutableStateFlow(AssistantVoiceState.IDLE)

    val voiceState: StateFlow<AssistantVoiceState> =
        _voiceState.asStateFlow()

    private val _audioRms =
        MutableStateFlow(0f)

    val audioRms: StateFlow<Float> =
        _audioRms.asStateFlow()

    private val _recognizedText =
        MutableStateFlow("")

    val recognizedText: StateFlow<String> =
        _recognizedText.asStateFlow()

    private val _statusMessage =
        MutableStateFlow("JARVIS Çevrimiçi")

    val statusMessage: StateFlow<String> =
        _statusMessage.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private var mediaPlayer: MediaPlayer? = null

    private val ttsClient =
        OpenAITtsClient(context.applicationContext)

    private val scope =
        CoroutineScope(Dispatchers.Main)

    private var speechJob: Job? = null

    fun startListening() {

        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {

            _statusMessage.value =
                "Ses tanıma bu cihazda desteklenmiyor."

            _voiceState.value =
                AssistantVoiceState.ERROR

            return
        }

        try {

            speechRecognizer?.destroy()

            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(context)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(
                        params: Bundle?
                    ) {
                        _voiceState.value =
                            AssistantVoiceState.LISTENING

                        _statusMessage.value =
                            "Dinliyorum..."
                    }

                    override fun onBeginningOfSpeech() {
                        _statusMessage.value =
                            "Ses algılandı..."
                    }

                    override fun onRmsChanged(
                        rmsdB: Float
                    ) {

                        val normalized =
                            ((rmsdB + 2f) / 12f)
                                .coerceIn(0f, 1f)

                        _audioRms.value =
                            normalized
                    }

                    override fun onBufferReceived(
                        buffer: ByteArray?
                    ) {
                    }

                    override fun onEndOfSpeech() {

                        _voiceState.value =
                            AssistantVoiceState.THINKING

                        _statusMessage.value =
                            "Analiz ediliyor..."
                    }

                    override fun onError(
                        error: Int
                    ) {

                        _audioRms.value = 0f

                        _voiceState.value =
                            AssistantVoiceState.IDLE

                        _statusMessage.value =
                            when (error) {

                                SpeechRecognizer.ERROR_NO_MATCH ->
                                    "Ses anlaşılamadı, tekrar deneyin."

                                SpeechRecognizer.ERROR_NETWORK ->
                                    "Ağ bağlantı hatası."

                                SpeechRecognizer.ERROR_AUDIO ->
                                    "Ses yakalama hatası."

                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                    "Mikrofon izni gerekli."

                                else ->
                                    "Dinleme zaman aşımı."
                            }
                    }

                    override fun onResults(
                        results: Bundle?
                    ) {

                        _audioRms.value = 0f

                        val matches =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val text =
                            matches
                                ?.firstOrNull()
                                ?.trim()

                        if (!text.isNullOrBlank()) {

                            _recognizedText.value =
                                text

                            _voiceState.value =
                                AssistantVoiceState.THINKING

                            _statusMessage.value =
                                "İşleniyor: \"$text\""

                            onVoiceInputRecognized(text)

                        } else {

                            _voiceState.value =
                                AssistantVoiceState.IDLE

                            _statusMessage.value =
                                "JARVIS Hazır"
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {

                        val matches =
                            partialResults?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val text =
                            matches?.firstOrNull()

                        if (!text.isNullOrBlank()) {
                            _recognizedText.value = text
                        }
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {
                    }
                }
            )

            val intent =
                Intent(
                   
