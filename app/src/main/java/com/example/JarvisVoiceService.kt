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
        }, 1200)
    }

    private fun startRecognition() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            return
        }

        try {
