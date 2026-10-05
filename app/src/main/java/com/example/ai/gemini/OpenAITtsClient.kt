package com.example.ai.gemini

import android.content.Context
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

            val apiKey = BuildConfig.OPENAI_API_KEY

            if (apiKey.isBlank()) {
                throw Exception("OPENAI_API_KEY BuildConfig içine gelmiyor.")
            }

            val json = JSONObject().apply {
                put("model", "gpt-4o-mini-tts")
                put("voice", "cedar")
                put("input", text)
                put(
                    "instructions",
                    "Türkçe konuş. Sakin, doğal, profesyonel ve derin bir erkek asistan sesi kullan."
                )
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

            client.newCall(request).execute().use { response ->

                if (!response.isSuccessful) {
                    val error = response.body?.string()
                        ?: "OpenAI boş hata döndürdü."

                    throw Exception(
                        "OpenAI TTS HTTP ${response.code}: $error"
                    )
                }

                val bytes = response.body?.bytes()
                    ?: throw Exception("OpenAI ses verisi döndürmedi.")

                if (bytes.isEmpty()) {
                    throw Exception("OpenAI boş ses dosyası döndürdü.")
                }

                val file = File(
                    context.cacheDir,
                    "jarvis_voice_${System.currentTimeMillis()}.mp3"
                )

                file.writeBytes(bytes)

                file
            }
        }
}
