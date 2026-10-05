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

            _voiceState.value =
                AssistantVoiceState.ERROR

            _statusMessage.value =
                "Bu cihazda ses tanıma desteklenmiyor."

            return
        }

        try {

            speechRecognizer?.cancel()
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

                        _audioRms.value =
                            0f

                        _voiceState.value =
                            AssistantVoiceState.THINKING

                        _statusMessage.value =
                            "Analiz ediliyor..."
                    }

                    override fun onError(
                        error: Int
                    ) {

                        _audioRms.value =
                            0f

                        _voiceState.value =
                            AssistantVoiceState.IDLE

                        _statusMessage.value =
                            when (error) {

                                SpeechRecognizer.ERROR_NO_MATCH ->
                                    "Ses anlaşılamadı."

                                SpeechRecognizer.ERROR_NETWORK ->
                                    "Ağ bağlantısı hatası."

                                SpeechRecognizer.ERROR_AUDIO ->
                                    "Ses yakalama hatası."

                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                    "Mikrofon izni gerekli."

                                else ->
                                    "Dinleme sona erdi."
                            }
                    }

                    override fun onResults(
                        results: Bundle?
                    ) {

                        _audioRms.value =
                            0f

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
                            _recognizedText.value =
                                text
                        }
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {
                    }
                }
            )

            val recognitionIntent =
                Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                ).apply {

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        "tr-TR"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        "tr-TR"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                        true
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_MAX_RESULTS,
                        1
                    )
                }

            speechRecognizer?.startListening(
                recognitionIntent
            )

            _voiceState.value =
                AssistantVoiceState.LISTENING

            _statusMessage.value =
                "Dinliyorum..."

        } catch (e: Exception) {

            Log.e(
                "JarvisVoiceManager",
                "Ses tanıma başlatılamadı",
                e
            )

            _voiceState.value =
                AssistantVoiceState.ERROR

            _statusMessage.value =
                "Mikrofon başlatılamadı."
        }
    }

    fun stopListening() {

        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(
                "JarvisVoiceManager",
                "Dinleme durdurulamadı",
                e
            )
        }

        _audioRms.value =
            0f

        if (
            _voiceState.value ==
            AssistantVoiceState.LISTENING
        ) {

            _voiceState.value =
                AssistantVoiceState.IDLE

            _statusMessage.value =
                "JARVIS Hazır"
        }
    }

    fun setThinking() {

        _voiceState.value =
            AssistantVoiceState.THINKING

        _statusMessage.value =
            "Analiz ediliyor..."
    }

    fun speak(text: String) {

        if (
            text.isBlank() ||
            !preferences.isAutoSpeakEnabled()
        ) {

            _voiceState.value =
                AssistantVoiceState.IDLE

            return
        }

        stopListening()

        speechJob?.cancel()

        speechJob =
            scope.launch {

                try {

                    _voiceState.value =
                        AssistantVoiceState.SPEAKING

                    _statusMessage.value =
                        "JARVIS konuşuyor..."

                    val audioFile =
                        ttsClient.generateSpeech(text)

                    if (audioFile == null) {

                        Log.e(
                            "JarvisVoiceManager",
                            "OpenAI TTS ses oluşturamadı."
                        )

                        _voiceState.value =
                            AssistantVoiceState.ERROR

                        _statusMessage.value =
                            "Ses oluşturulamadı."

                        return@launch
                    }

                    playAudio(audioFile)

                } catch (e: Exception) {

                    Log.e(
                        "JarvisVoiceManager",
                        "TTS hatası",
                        e
                    )

                    _voiceState.value =
                        AssistantVoiceState.ERROR

                    _statusMessage.value =
                        "Ses oluşturulamadı."
                }
            }
    }

    private fun playAudio(
        file: File
    ) {

        try {

            mediaPlayer?.release()

            val player =
                MediaPlayer()

            mediaPlayer =
                player

            player.setDataSource(
                file.absolutePath
            )

            player.setOnCompletionListener {

                _voiceState.value =
                    AssistantVoiceState.IDLE

                _statusMessage.value =
                    "JARVIS Hazır"

                player.release()

                mediaPlayer = null

                file.delete()
            }

            player.setOnErrorListener { _, _, _ ->

                _voiceState.value =
                    AssistantVoiceState.ERROR

                _statusMessage.value =
                    "Ses oynatılamadı."

                player.release()

                mediaPlayer = null

                file.delete()

                true
            }

            player.prepare()
            player.start()

        } catch (e: Exception) {

            Log.e(
                "JarvisVoiceManager",
                "Ses oynatma hatası",
                e
            )

            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            }

            mediaPlayer = null

            file.delete()

            _voiceState.value =
                AssistantVoiceState.ERROR

            _statusMessage.value =
                "Ses oynatılamadı."
        }
    }

    fun stopSpeaking() {

        speechJob?.cancel()
        speechJob = null

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

        mediaPlayer = null

        _voiceState.value =
            AssistantVoiceState.IDLE

        _statusMessage.value =
            "JARVIS Hazır"
    }

    fun destroy() {

        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }

        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }

        speechRecognizer = null

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }

        mediaPlayer = null

        speechJob?.cancel()
        speechJob = null
    }
}
