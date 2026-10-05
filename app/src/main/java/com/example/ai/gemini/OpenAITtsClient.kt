package com.example.ai.gemini

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class OpenAITtsClient(
    private val context: Context
) {

    private val client = OkHttpClient()

    suspend fun generateSpeech(text: String): File? =
        withContext(Dispatchers.IO) {

            try {
                val apiKey = BuildConfig.OPENAI_API_KEY

                Log.d(
                    "JARVIS_TTS",
                    "API key durumu: ${if (apiKey.isNotBlank()) "VAR" else "YOK"}"
                )

                if (apiKey.isBlank()) {
                    Log.e("JARVIS_TTS", "OPENAI_API_KEY BOŞ!")
                    return@withContext null
                }

                val json = JSONObject().apply {
                    put("model", "gpt-4o-mini-tts")
                    put("voice", "cedar")
                    put("input", text)
                    put("instructions", "Türkçe, sakin, doğal, profesyonel ve derin bir erkek asistan sesiyle konuş.")
                    put("response_format", "mp3")
                    put("speed", 0.95)
                }

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/audio/speech")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(
                        json.toString()
                            .toRequestBody("application/json".toMediaType())
                    )
                    .build()

                Log.d("JARVIS_TTS", "OpenAI TTS isteği gönderiliyor...")

                client.newCall(request).execute().use { response ->

                    Log.d(
                        "JARVIS_TTS",
                        "HTTP kodu: ${response.code}"
                    )

                    if (!response.isSuccessful) {
                        val error = response.body?.string()

                        Log.e(
                            "JARVIS_TTS",
                            "OPENAI TTS HATASI: $error"
                        )

                        return@withContext null
                    }

                    val bytes = response.body?.bytes()

                    if (bytes == null || bytes.isEmpty()) {
                        Log.e(
                            "JARVIS_TTS",
                            "OpenAI boş ses dosyası döndürdü."
                        )
                        return@withContext null
                    }

                    val file = File(
                        context.cacheDir,
                        "jarvis_voice_${System.currentTimeMillis()}.mp3"
                    )

                    file.writeBytes(bytes)

                    Log.d(
                        "JARVIS_TTS",
                        "SES BAŞARIYLA OLUŞTURULDU: ${bytes.size} byte"
                    )

                    return@withContext file
                }

            } catch (e: Exception) {

                Log.e(
                    "JARVIS_TTS",
                    "TTS EXCEPTION",
                    e
                )

                null
            }
        }
}
