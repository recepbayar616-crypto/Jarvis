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
import java.util.concurrent.TimeUnit

class OpenAITtsClient(
    private val context: Context
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateSpeech(text: String): File? =
        withContext(Dispatchers.IO) {

            if (BuildConfig.OPENAI_API_KEY.isBlank()) {
                Log.e("OpenAITtsClient", "OPENAI_API_KEY boş!")
                return@withContext null
            }

            try {
                val json = JSONObject().apply {
                    put("model", "gpt-4o-mini-tts")
                    put("voice", "cedar")
                    put("input", text)
                    put(
                        "instructions",
                        """
                        Türkçe konuş.
                        Derin, erkek, sakin ve doğal bir ses kullan.
                        JARVIS tarzında profesyonel ve kendinden emin konuş.
                        Robotik olma.
                        Gereksiz vurgu yapma.
                        Cümleleri doğal insan konuşması gibi söyle.
                        Hızın orta seviyede olsun.
                        """.trimIndent()
                    )
                    put("response_format", "mp3")
                    put("speed", 0.95)
                }

                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/audio/speech")
                    .addHeader(
                        "Authorization",
                        "Bearer ${BuildConfig.OPENAI_API_KEY}"
                    )
                    .addHeader(
                        "Content-Type",
                        "application/json"
                    )
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {

                        val errorBody = response.body?.string()

                        Log.e(
                            "OpenAITtsClient",
                            "TTS API HATASI | HTTP ${response.code} | $errorBody"
                        )

                        return@withContext null
                    }

                    val bytes = response.body?.bytes()

                    if (bytes == null || bytes.isEmpty()) {
                        Log.e(
                            "OpenAITtsClient",
                            "TTS başarılı görünüyor ama ses verisi boş!"
                        )

                        return@withContext null
                    }

                    val file = File(
                        context.cacheDir,
                        "jarvis_voice_${System.currentTimeMillis()}.mp3"
                    )

                    file.writeBytes(bytes)

                    Log.d(
                        "OpenAITtsClient",
                        "TTS başarılı. Dosya: ${file.absolutePath}"
                    )

                    file
                }

            } catch (e: Exception) {

                Log.e(
                    "OpenAITtsClient",
                    "TTS bağlantı/uygulama hatası",
                    e
                )

                null
            }
        }
}
