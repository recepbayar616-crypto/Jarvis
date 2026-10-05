package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import java.util.Locale

class JarvisVoiceService : Service(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification = Notification.Builder(this, "jarvis_channel")
            .setContentTitle("JARVIS aktif")
            .setContentText("Sesli asistan hazır.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        startForeground(1001, notification)

        textToSpeech = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale("tr", "TR")

            val voices = textToSpeech.voices
            val maleVoice = voices.firstOrNull {
                it.locale.language == "tr" &&
                it.name.contains("male", ignoreCase = true)
            }

            if (maleVoice != null) {
                textToSpeech.voice = maleVoice
            }
        }
    }

    fun speak(text: String) {
        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "JARVIS_RESPONSE"
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "jarvis_channel",
            "JARVIS Sesli Asistan",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
