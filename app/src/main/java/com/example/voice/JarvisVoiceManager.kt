package com.example.voice

import android.content.Context
import android.content.Intent
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
) : RecognitionListener {

    private val _voiceState = MutableStateFlow(AssistantVoiceState.IDLE)
    val voiceState: StateFlow<AssistantVoiceState> = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _statusMessage =
        MutableStateFlow("JARVIS Çevrimiçi")
    val statusMessage: StateFlow<String> =
        _statusMessage.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private var mediaPlayer: android.media.MediaPlayer? = null

    private val ttsClient =
        OpenAITtsClient(context.applicationContext)

    private val scope = CoroutineScope(Dispatchers.Main)

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
                SpeechRecognizer
                    .createSpeechRecognizer(context)
                    .apply {
                        setRecognitionListener(this@JarvisVoiceManager)
                    }

            val intent =
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

            speechRecognizer?.startListening(intent)

            _voiceState.value =
                AssistantVoiceState.LISTENING

            _statusMessage.value =
                "Dinliyorum, efendim..."

        } catch (e: Exception) {

            Log.e(
                "VoiceManager",
                "Start listening error",
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

            if (
                _voiceState.value ==
                AssistantVoiceState.LISTENING
            ) {

                _voiceState.value =
                    AssistantVoiceState.IDLE

                _statusMessage.value =
                    "JARVIS Hazır"
            }

        } catch (e: Exception) {

            Log.e(
                "VoiceManager",
                "Stop listening error",
                e
            )
        }
