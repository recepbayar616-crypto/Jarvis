package com.example.ai.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.ai.gemini.JarvisAiResult
import com.example.ai.gemini.JarvisToolExecutor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAIClient(
    private val toolExecutor: JarvisToolExecutor
) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val model = "gpt-6-luna"

    private val systemInstruction = """
        Sen JARVIS'sin.

        Kullanıcının polis vakaları, kriminal soruşturmalar,
        adli tıp ve çözülemeyen olay yeri belgeselleri
        yayınlayan YouTube kanalının resmi yapay zeka asistanısın.

        MUTLAK KURALLAR:

        1. YouTube istatistiklerini ASLA kafandan uydurma.
           Gerçek veri gerektiğinde mutlaka uygun aracı kullan.

        2. Araçtan gelen gerçek verileri Türkçe,
           doğal ve konuşma diline uygun şekilde aktar.

        3. Bir metrik mevcut değilse açıkça:
           "Bu metrik şu an mevcut değil."
           de.

        4. Kullanıcı bir şeyi hatırlamanı, kaydetmeni
           veya görev oluşturmanı istediğinde rememberTask
           aracını kullan.

        5. YouTube içerik önerilerinde kriminalistik ciddiyeti koru.
           Doğrulanmamış iddiaları gerçek veya delil gibi sunma.

        6. Her zaman Türkçe konuş.

        7. Üslubun doğal, zeki, sakin ve zarif bir kişisel asistan
           gibi olsun.

        8. Kullanıcıya gerektiğinde "efendim" diye hitap et.

        9. Gereksiz uzun cevaplar verme.
           Sesli asistan olarak kısa ve anlaşılır konuş.

        10. Kullanıcının istediğini gerçekleştirmek için mevcut
            araçları aktif şekilde kullan.
    """.trimIndent()

    suspend fun processUserVoiceInput(
        userSpeech: String
    ): JarvisAiResult = withContext(Dispatchers.IO) {

        val apiKey = try {
            BuildConfig.OPENAI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_OPENAI_API_KEY") {
            return@withContext localFallback(userSpeech)
        }

        try {

            val tools = createTools()

            val firstInput = JSONArray()
                .put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", userSpeech)
                )

            val firstRequest = JSONObject()
                .put("model", model)
                .put("instructions", systemInstruction)
                .put("input", firstInput)
                .put("tools", tools)

            val firstResponse =
                sendRequest(apiKey, firstRequest)

            if (firstResponse == null) {
                return@withContext localFallback(userSpeech)
            }

            val output =
                firstResponse.optJSONArray("output")

            if (output == null) {
                return@withContext localFallback(userSpeech)
            }

            var toolUsed: String? = null
            var toolDataSnippet: String? = null

            val followUpInput = JSONArray()

            for (i in 0 until output.length()) {

                val item = output.optJSONObject(i)
                    ?: continue

                if (item.optString("type") != "function_call") {
                    continue
                }

                val toolName =
                    item.optString("name")

                val callId =
                    item.optString("call_id")

                val argumentsString =
                    item.optString("arguments", "{}")

                val arguments =
                    jsonObjectToMap(
                        try {
                            JSONObject(argumentsString)
                        } catch (e: Exception) {
                            JSONObject()
                        }
                    )

                Log.d(
                    "OpenAIClient",
                    "Tool çağrısı: $toolName args=$arguments"
                )

                val toolResult =
                    toolExecutor.execute(
                        toolName,
                        arguments
                    )

                toolUsed = toolName

                toolDataSnippet =
                    toolResult.entries.joinToString(", ") {
                        "${it.key}: ${it.value}"
                    }

                followUpInput.put(
                    JSONObject()
                        .put("type", "function_call_output")
                        .put("call_id", callId)
                        .put(
                            "output",
                            JSONObject(toolResult).toString()
                        )
                )
            }

            /*
             * Araç çağrısı yapılmadıysa modelin normal cevabını al.
             */
            if (followUpInput.length() == 0) {

                val text =
                    extractOutputText(firstResponse)

                if (!text.isNullOrBlank()) {
                    return@withContext JarvisAiResult(
                        replyText = text.trim()
                    )
                }

                return@withContext localFallback(userSpeech)
            }

            /*
             * Araç sonuçlarını OpenAI'ye gönder.
             */
            val secondRequest =
                JSONObject()
                    .put("model", model)
                    .put("instructions", systemInstruction)
                    .put("input", followUpInput)
                    .put("tools", tools)

            val secondResponse =
                sendRequest(apiKey, secondRequest)

            if (secondResponse != null) {

                val finalText =
                    extractOutputText(secondResponse)

                if (!finalText.isNullOrBlank()) {

                    return@withContext JarvisAiResult(
                        replyText = finalText.trim(),
                        toolUsed = toolUsed,
                        toolDataSnippet = toolDataSnippet
                    )
                }
            }

            localFallback(userSpeech)

        } catch (e: Exception) {

            Log.e(
                "OpenAIClient",
                "OpenAI hatası",
                e
            )

            localFallback(userSpeech)
        }
    }

    private fun sendRequest(
        apiKey: String,
        json: JSONObject
    ): JSONObject? {

        val requestBody =
            json.toString()
                .toRequestBody(
                    "application/json".toMediaType()
                )

        val request =
            Request.Builder()
                .url("https://api.openai.com/v1/responses")
                .addHeader(
                    "Authorization",
                    "Bearer $apiKey"
                )
                .addHeader(
                    "Content-Type",
                    "application/json"
                )
                .post(requestBody)
                .build()

        okHttpClient
            .newCall(request)
            .execute()
            .use { response ->

                val body =
                    response.body?.string()
                        ?: ""

                if (!response.isSuccessful) {

                    Log.e(
                        "OpenAIClient",
                        "HTTP ${response.code}: $body"
                    )

                    return null
                }

                return JSONObject(body)
            }
    }

    private fun createTools(): JSONArray {

        val tools = JSONArray()

        tools.put(
            functionTool(
                "getTodayAnalytics",
                "YouTube kanalının bugünkü gerçek izlenme, yeni abone, yüklenen video, izlenme süresi ve düne göre kıyaslama verilerini getirir.",
                JSONObject()
            )
        )

        tools.put(
            functionTool(
                "getChannelOverview",
                "Kanalın adı, toplam abone, toplam izlenme ve toplam video sayısını getirir.",
                JSONObject()
            )
        )

        tools.put(
            functionTool(
                "getLatestVideoPerformance",
                "Kanalın en son videosunun gerçek performans verilerini getirir.",
                JSONObject()
            )
        )

        tools.put(
            functionTool(
                "getTopVideos",
                "Kanalın en çok izlenen videolarını getirir.",
                JSONObject()
                    .put(
                        "limit",
                        JSONObject()
                            .put("type", "integer")
                            .put(
                                "description",
                                "Getirilecek video adedi. Örneğin 3 veya 5."
                            )
                    )
            )
        )

        tools.put(
            functionTool(
                "getRecentVideos",
                "Son yayınlanan videoları ve performanslarını getirir.",
                JSONObject()
                    .put(
                        "limit",
                        JSONObject()
                            .put("type", "integer")
                            .put(
                                "description",
                                "Video sayısı. Örneğin 5 veya 10."
                            )
                    )
            )
        )

        tools.put(
            functionTool(
                "compareLastVideos",
                "Son iki videoyu izlenme ve etkileşim açısından kıyaslar.",
                JSONObject()
            )
        )

        tools.put(
            functionTool(
                "getSubscriberGrowth",
                "YouTube kanalının abone büyümesini ve trendini getirir.",
                JSONObject()
            )
        )

        tools.put(
            functionTool(
                "rememberTask",
                "Kullanıcının hatırlamak istediği notu veya görevi kaydeder.",
                JSONObject()
                    .put(
                        "note",
                        JSONObject()
                            .put("type", "string")
                            .put(
                                "description",
                                "Hatırlanacak not veya görev."
                            )
                    )
                    .put(
                        "category",
                        JSONObject()
                            .put("type", "string")
                            .put(
                                "description",
                                "Görevin kategorisi."
                            )
                    )
                    .put(
                        "required",
                        JSONArray().put("note")
                    )
            )
        )

        tools.put(
            functionTool(
                "getPendingTasks",
                "Kayıtlı yapılacak görevleri ve vaka notlarını getirir.",
                JSONObject()
            )
        )

        return tools
    }

    private fun functionTool(
        name: String,
        description: String,
        properties: JSONObject
    ): JSONObject {

        val parameters =
            JSONObject()
                .put("type", "object")
                .put("properties", properties)
                .put("additionalProperties", false)

        return JSONObject()
            .put("type", "function")
            .put("name", name)
            .put("description", description)
            .put("parameters", parameters)
    }

    private fun extractOutputText(
        response: JSONObject
    ): String? {

        val output =
            response.optJSONArray("output")
                ?: return null

        val texts = mutableListOf<String>()

        for (i in 0 until output.length()) {

            val item =
                output.optJSONObject(i)
                    ?: continue

            if (item.optString("type") != "message") {
                continue
            }

            val content =
                item.optJSONArray("content")
                    ?: continue

            for (j in 0 until content.length()) {

                val part =
                    content.optJSONObject(j)
                        ?: continue

                if (part.optString("type") == "output_text") {

                    val text =
                        part.optString("text")

                    if (text.isNotBlank()) {
                        texts.add(text)
                    }
                }
            }
        }

        return texts.joinToString("\n")
            .takeIf { it.isNotBlank() }
    }

    private fun jsonObjectToMap(
        json: JSONObject
    ): Map<String, Any?> {

        val result =
            mutableMapOf<String, Any?>()

        val keys =
            json.keys()

        while (keys.hasNext()) {

            val key = keys.next()
            val value = json.opt(key)

            result[key] =
                when (value) {

                    JSONObject.NULL -> null

                    is JSONObject ->
                        jsonObjectToMap(value)

                    is JSONArray ->
                        jsonArrayToList(value)

                    else -> value
                }
        }

        return result
    }

    private fun jsonArrayToList(
        array: JSONArray
    ): List<Any?>
        = (0 until array.length()).map { index ->

        val value = array.opt(index)

        when (value) {

            JSONObject.NULL -> null

            is JSONObject ->
                jsonObjectToMap(value)

            is JSONArray ->
                jsonArrayToList(value)

            else -> value
        }
    }

    /*
     * API anahtarı yoksa mevcut yerel JARVIS davranışı
     * çalışmaya devam eder.
     */
    private suspend fun localFallback(
        input: String
    ): JarvisAiResult {

        val lower =
            input.lowercase()

        return when {

            lower.contains("bugün") ||
            lower.contains("özet") ||
            lower.contains("rapor") -> {

                val data =
                    toolExecutor.execute(
                        "getTodayAnalytics",
                        null
                    )

                val views =
                    data["viewsGainedToday"] ?: "0"

                val subs =
                    data["subscribersGainedToday"] ?: "0"

                val best =
                    data["bestPerformingVideoToday"]
                        ?: "Mevcut değil"

                JarvisAiResult(
                    replyText =
                        "Bugün kanalımız $views yeni izlenme aldı. " +
                        "$subs yeni abone kazandık. " +
                        "Bugünün en iyi performansı '$best' videosunda, efendim.",
                    toolUsed = "getTodayAnalytics",
                    toolDataSnippet = data.toString()
                )
            }

            lower.contains("son video") ||
            lower.contains("nasıl gidiyor") -> {

                val data =
                    toolExecutor.execute(
                        "getLatestVideoPerformance",
                        null
                    )

                val title =
                    data["title"]
                        ?: "Son video"

                val views =
                    data["viewCount"]
                        ?: "0"

                JarvisAiResult(
                    replyText =
                        "Son videonuz '$title' şu anda " +
                        "$views izlenmede, efendim.",
                    toolUsed =
                        "getLatestVideoPerformance",
                    toolDataSnippet =
                        data.toString()
                )
            }

            lower.contains("hatırla") ||
            lower.contains("not al") ||
            lower.contains("kaydet") -> {

                val note =
                    input
                        .replace(
                            "jarvis",
                            "",
                            ignoreCase = true
                        )
                        .replace(
                            "bunu hatırla",
                            "",
                            ignoreCase = true
                        )
                        .replace(
                            "not al",
                            "",
                            ignoreCase = true
                        )
                        .trim()

                val finalNote =
                    if (note.isBlank())
                        "Adli Tıp Olay Yeri Notu"
                    else
                        note

                val data =
                    toolExecutor.execute(
                        "rememberTask",
                        mapOf(
                            "note" to finalNote,
                            "category" to "Vaka Araştırması"
                        )
                    )

                JarvisAiResult(
                    replyText =
                        "'$finalNote' notunu hafızaya kaydettim, efendim.",
                    toolUsed = "rememberTask",
                    toolDataSnippet =
                        data.toString()
                )
            }

            else -> {

                val data =
                    toolExecutor.execute(
                        "getChannelOverview",
                        null
                    )

                val channel =
                    data["channelName"]
                        ?: "kanalınız"

                JarvisAiResult(
                    replyText =
                        "Anlaşıldı efendim. $channel için hazırım."
                )
            }
        }
    }
}
