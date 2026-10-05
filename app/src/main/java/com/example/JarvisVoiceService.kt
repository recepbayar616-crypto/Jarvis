package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.ai.gemini.OpenAITtsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class JarvisVoiceService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var ttsClient: OpenAITtsClient

    private var waitingForCommand = false
    private var isSpeaking = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification = Notification.Builder(
            this,
            "jarvis_channel"
        )
            .setContentTitle("JARVIS aktif")
            .setContentText("“Jarvis” komutunu bekliyor.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        startForeground(1001, notification)

        ttsClient = OpenAITtsClient(this)
    }

    private fun startWakeWordListening() {
        waitingForCommand = false
        startRecognition()
    }

    private fun startCommandListening() {

        waitingForCommand = true

        speak("Efendim?")

        handler.postDelayed({

            if (waitingForCommand && !isSpeaking) {
                startRecognition()
            }

        }, 500)
    }

    private fun startRecognition() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            return
        }

        if (isSpeaking) {
            return
        }

        try {

            speechRecognizer?.cancel()
            speechRecognizer?.destroy()

            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onResults(results: Bundle?) {

                        val texts =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val spokenText =
                            texts
                                ?.firstOrNull()
                                ?.trim()
                                ?.lowercase(Locale("tr", "TR"))

                        if (spokenText.isNullOrBlank()) {
                            scheduleRestart()
                            return
                        }

                        if (!waitingForCommand) {

                            if (spokenText.contains("jarvis")) {

                                startCommandListening()

                            } else {

                                scheduleRestart()
                            }

                        } else {

                            val intent = Intent(
                                this@JarvisVoiceService,
                                MainActivity::class.java
                            ).apply {

                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP

                                putExtra(
                                    "JARVIS_VOICE_COMMAND",
                                    spokenText
                                )
                            }

                            startActivity(intent)

                            waitingForCommand = false

                            handler.postDelayed({

                                startWakeWordListening()

                            }, 1500)
                        }
                    }

                    override fun onError(error: Int) {
                        scheduleRestart()
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(
                        buffer: ByteArray?
                    ) {}

                    override fun onEndOfSpeech() {}

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
                }
            )

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
                        RecognizerIntent.EXTRA_MAX_RESULTS,
                        1
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                        false
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                        1500L
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                        2500L
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                        2000L
                    )
                }

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {

            scheduleRestart()
        }
    }

    private fun scheduleRestart() {

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({

            if (!isSpeaking) {
                startRecognition()
            }

        }, 2500)
    }

    private fun speak(text: String) {

        if (isSpeaking) return

        isSpeaking = true

        CoroutineScope(Dispatchers.Main).launch {

            val audioFile =
                ttsClient.generateSpeech(text)

            if (audioFile == null) {

                isSpeaking = false
                return@launch
            }

            try {

                mediaPlayer?.release()

                mediaPlayer =
                    MediaPlayer().apply {

                        setDataSource(
                            audioFile.absolutePath
                        )

                        setOnCompletionListener {

                            isSpeaking = false

                            release()

                            mediaPlayer = null

                            if (waitingForCommand) {

                                handler.postDelayed({

                                    startRecognition()

                                }, 300)
                            }
                        }

                        setOnErrorListener { _, _, _ ->

                            isSpeaking = false

                            release()

                            mediaPlayer = null

                            true
                        }

                        prepare()
                        start()
                    }

            } catch (e: Exception) {

                isSpeaking = false
            }
        }
    }

    private fun createNotificationChannel() {

        val channel =
            NotificationChannel(
                "jarvis_channel",
                "JARVIS Sesli Asistan",
                NotificationManager.IMPORTANCE_LOW
            )

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        speechRecognizer?.cancel()
        speechRecognizer?.destroy()

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
