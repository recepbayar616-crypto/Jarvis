package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class JarvisVoiceService : Service(), TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var textToSpeech: TextToSpeech

    private var waitingForCommand = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification = Notification.Builder(this, "jarvis_channel")
            .setContentTitle("JARVIS aktif")
            .setContentText("“Jarvis” komutunu bekliyor.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        startForeground(1001, notification)

        textToSpeech = TextToSpeech(this, this)

        startWakeWordListening()
    }

    private fun startWakeWordListening() {
        waitingForCommand = false
        startRecognition()
    }

    private fun startCommandListening() {
        waitingForCommand = true

        speak("Sizi dinliyorum.")

        handler.postDelayed({
            startRecognition()
        }, 1500)
    }

    private fun startRecognition() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            return
        }

        try {
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
                            texts?.firstOrNull()
                                ?.trim()
                                ?.lowercase(Locale("tr", "TR"))

                        if (spokenText.isNullOrBlank()) {
                            restartListening()
                            return
                        }

                        if (!waitingForCommand) {

                            if (spokenText.contains("jarvis")) {
                                startCommandListening()
                            } else {
                                restartListening()
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
                            }, 1000)
                        }
                    }

                    override fun onError(error: Int) {
                        restartListening()
                    }

                    override fun onReadyForSpeech(
                        params: Bundle?
                    ) {}

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(
                        rmsdB: Float
                    ) {}

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

            val intent = Intent(
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
            }

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            restartListening()
        }
    }

    private fun restartListening() {

        handler.postDelayed({

            if (waitingForCommand) {
                startCommandListening()
            } else {
                startWakeWordListening()
            }

        }, 1000)
    }

    private fun speak(text: String) {

        if (::textToSpeech.isInitialized) {

            textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_RESPONSE"
            )
        }
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language =
                Locale.forLanguageTag("tr-TR")
        }
    }

    private fun createNotificationChannel() {

        val channel = NotificationChannel(
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

        speechRecognizer?.destroy()

        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
