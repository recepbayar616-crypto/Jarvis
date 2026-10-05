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

            if (BuildConfig.OPENAI_API_KEY.isBlank()) {
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
                        return@withContext null
                    }

                    val bytes = response.body?.bytes()
                        ?: return@withContext null

                    val file = File(
                        context.cacheDir,
                        "jarvis_voice_${System.currentTimeMillis()}.mp3"
                    )

                    file.writeBytes(bytes)

                    file
                }

            } catch (e: Exception) {
                null
            }
        }
}
