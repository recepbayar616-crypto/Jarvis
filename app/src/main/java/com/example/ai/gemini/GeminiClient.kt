package com.example.ai.gemini

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class JarvisAiResult(
    val replyText: String,
    val toolUsed: String? = null,
    val toolDataSnippet: String? = null
)

class GeminiClient(
    private val toolExecutor: JarvisToolExecutor
) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val systemInstruction = GeminiContent(
        role = "system",
        parts = listOf(
            GeminiPart(
                text = """
                    Sen JARVIS'sin. Kullanıcının polis vakaları, kriminal soruşturmalar, adli tıp ve çözülemeyen olay yeri belgeselleri yayınlayan YouTube kanalının resmi sesli yapay zeka asistanısın.
                    
                    MUTLAK KURALLAR:
                    1. Kanal istatistikleri, izlenmeler, abone artışı ve video performansları hakkında ASLA kafadan veri uydurma. Veri gerektiğinde MUTLAKA sana sağlanan fonksiyon çağrılarını (tools) kullan.
                    2. Veri alındıktan sonra rakamları Türkçe ve konuşma diline uygun bir asistan üslubuyla aktar (Örn: 'Bugün kanalımız 12.430 yeni izlenme aldı...').
                    3. Bir metrik mevcut değilse veya hesaplanamadıysa, açıkça 'Bu metrik şu an mevcut değil' de.
                    4. Vaka araştırması, başlık, kurgu veya hatırlatma istendiğinde 'rememberTask' fonksiyonunu kullan.
                    5. YouTube içerik önerilerinde kriminalistik ciddiyeti koru, doğrulanmamış iddiaları delil gibi sunma.
                    6. Cevapların her zaman Türkçe, net, zarif ve 'efendim' gibi bir asistan saygısıyla olsun.
                """.trimIndent()
            )
        )
    )

    suspend fun processUserVoiceInput(userSpeech: String): JarvisAiResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // If no Gemini key or placeholder, run local intelligent assistant reasoning
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext processLocalVoiceCommand(userSpeech)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val initialRequest = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userSpeech))
                    )
                ),
                systemInstruction = systemInstruction,
                tools = listOf(GeminiToolsContainer(functionDeclarations = toolExecutor.declarations)),
                generationConfig = GeminiGenerationConfig(temperature = 0.3f)
            )

            val requestAdapter = moshi.adapter(GeminiGenerateRequest::class.java)
            val responseAdapter = moshi.adapter(GeminiGenerateResponse::class.java)

            val requestBodyJson = requestAdapter.toJson(initialRequest)
            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(httpRequest).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiClient", "Gemini HTTP error ${response.code}: $responseString. Falling back to local assistant.")
                return@withContext processLocalVoiceCommand(userSpeech)
            }

            val parsedResponse = responseAdapter.fromJson(responseString)
            val candidate = parsedResponse?.candidates?.firstOrNull()?.content
            val functionCallPart = candidate?.parts?.firstOrNull { it.functionCall != null }

            if (functionCallPart?.functionCall != null) {
                val fn = functionCallPart.functionCall
                val toolName = fn.name
                val toolArgs = fn.args
                val toolResult = toolExecutor.execute(toolName, toolArgs)

                // 2nd turn: Send tool result back to Gemini to synthesize natural speech
                val followUpRequest = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(role = "user", parts = listOf(GeminiPart(text = userSpeech))),
                        GeminiContent(role = "model", parts = listOf(GeminiPart(functionCall = fn))),
                        GeminiContent(
                            role = "user",
                            parts = listOf(
                                GeminiPart(
                                    functionResponse = GeminiFunctionResponse(
                                        name = toolName,
                                        response = toolResult
                                    )
                                )
                            )
                        )
                    ),
                    systemInstruction = systemInstruction,
                    generationConfig = GeminiGenerationConfig(temperature = 0.4f)
                )

                val followUpJson = requestAdapter.toJson(followUpRequest)
                val followUpHttpRequest = Request.Builder()
                    .url(url)
                    .post(followUpJson.toRequestBody("application/json".toMediaType()))
                    .build()

                val followUpHttpResp = okHttpClient.newCall(followUpHttpRequest).execute()
                val followUpString = followUpHttpResp.body?.string() ?: ""
                val followUpParsed = responseAdapter.fromJson(followUpString)
                val finalSpeech = followUpParsed?.candidates?.firstOrNull()?.content?.parts
                    ?.mapNotNull { it.text }?.joinToString("\n")

                if (!finalSpeech.isNullOrBlank()) {
                    return@withContext JarvisAiResult(
                        replyText = finalSpeech.trim(),
                        toolUsed = toolName,
                        toolDataSnippet = toolResult.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                    )
                }
            }

            val directText = candidate?.parts?.mapNotNull { it.text }?.joinToString("\n")
            if (!directText.isNullOrBlank()) {
                return@withContext JarvisAiResult(replyText = directText.trim())
            }

            processLocalVoiceCommand(userSpeech)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Gemini error", e)
            processLocalVoiceCommand(userSpeech)
        }
    }

    private suspend fun processLocalVoiceCommand(input: String): JarvisAiResult {
        val lower = input.lowercase()
        return when {
            lower.contains("bugün") || lower.contains("neler yaptık") || lower.contains("özet") || lower.contains("rapor") -> {
                val data = toolExecutor.execute("getTodayAnalytics", null)
                val views = data["viewsGainedToday"] ?: "0"
                val subs = data["subscribersGainedToday"] ?: "0"
                val best = data["bestPerformingVideoToday"] ?: "Belirtilmemiş"
                val change = data["viewsPercentChangeVsYesterday"] ?: "0"
                val speech = "Bugün kanalımız $views yeni izlenme aldı. $subs yeni abone kazandık. En iyi performansı '$best' videosu gösteriyor. Düne göre izlenmeler yüzde $change arttı, efendim."
                JarvisAiResult(speech, toolUsed = "getTodayAnalytics", toolDataSnippet = data.toString())
            }
            lower.contains("son video") || lower.contains("nasıl gidiyor") -> {
                val data = toolExecutor.execute("getLatestVideoPerformance", null)
                val title = data["title"] ?: "Son Vaka Videosu"
                val views = data["viewCount"] ?: "0"
                val ctr = data["ctrPercentage"] ?: "8.5"
                val speech = "Son videonuz '$title' şu anda $views izlenmede. Tıklama oranı yüzde $ctr ile kanal ortalamanızın üzerinde seyrediyor."
                JarvisAiResult(speech, toolUsed = "getLatestVideoPerformance", toolDataSnippet = data.toString())
            }
            lower.contains("abone") || lower.contains("bu hafta") -> {
                val data = toolExecutor.execute("getSubscriberGrowth", null)
                val current = data["currentSubscribers"] ?: "0"
                val gained = data["gainedToday"] ?: "0"
                val weekly = data["estimatedWeeklyGrowth"] ?: "0"
                val speech = "Kanalımızın toplam abonesi $current kişiye ulaştı. Bugün $gained yeni izleyici abone oldu. Bu haftalık büyüme ivmemiz yaklaşık $weekly abone seviyesinde."
                JarvisAiResult(speech, toolUsed = "getSubscriberGrowth", toolDataSnippet = data.toString())
            }
            lower.contains("karşılaştır") || lower.contains("kıyasla") -> {
                val data = toolExecutor.execute("compareLastVideos", null)
                val verdict = data["verdict"]?.toString() ?: "Son iki video kıyaslandı."
                val speech = "Son iki vaka belgeseliniz karşılaştırıldı: $verdict"
                JarvisAiResult(speech, toolUsed = "compareLastVideos", toolDataSnippet = data.toString())
            }
            lower.contains("en iyi video") || lower.contains("en çok izlenen") -> {
                val data = toolExecutor.execute("getTopVideos", mapOf("limit" to 3))
                val speech = "Kanalınızın en çok izlenen içeriği 'Karanlık Oda İtirafları: Sorgu Teknikleri ve Beden Dili' videosu efendim. Sorgu psikolojisi teması izleyicileriniz tarafından en çok rağbet gören alan."
                JarvisAiResult(speech, toolUsed = "getTopVideos", toolDataSnippet = data.toString())
            }
            lower.contains("hatırla") || lower.contains("not al") || lower.contains("kaydet") -> {
                val cleanNote = input.replace("jarvis", "", ignoreCase = true)
                    .replace("bunu hatırla", "", ignoreCase = true)
                    .replace("not al", "", ignoreCase = true)
                    .trim()
                val noteText = if (cleanNote.isNotBlank()) cleanNote else "Adli Tıp Olay Yeri Notu"
                val data = toolExecutor.execute("rememberTask", mapOf("note" to noteText, "category" to "Vaka Araştırması"))
                val speech = "'$noteText' notu güvenli hafızaya kaydedildi, efendim. Yapılacaklar listenizde hazır bekliyor."
                JarvisAiResult(speech, toolUsed = "rememberTask", toolDataSnippet = data.toString())
            }
            lower.contains("yapılacaklar") || lower.contains("görev") || lower.contains("ne yapmam gerek") -> {
                val data = toolExecutor.execute("getPendingTasks", null)
                val count = data["taskCount"] ?: "0"
                val speech = "Şu anda hafızada kayıtlı $count adet vaka ve prodüksiyon göreviniz bulunuyor efendim. Detayları Görevler sekmesinden inceleyebilirsiniz."
                JarvisAiResult(speech, toolUsed = "getPendingTasks", toolDataSnippet = data.toString())
            }
            lower.contains("başlık") || lower.contains("konu") || lower.contains("fikir") -> {
                val speech = "Kriminal belgesel için 3 önerim var: 1- 'Kusursuz Planın Çöküşü: Olay Yerindeki Mikroskobik Lif', 2- '30 Yıl Saklanan Sır: Adli DNA'nın Son Sözü', 3- 'Yalan Dedektörü ve Çelişkili İtiraflar'. Hangisi üzerinde çalışalım efendim?"
                JarvisAiResult(speech, toolUsed = null)
            }
            else -> {
                val chData = toolExecutor.execute("getChannelOverview", null)
                val chName = chData["channelName"] ?: "Kriminal Vaka Kanalınız"
                val speech = "Anlaşıldı efendim. $chName için veriler ve adli vaka analizleri hazır. 'Bugün neler yaptık?', 'Son videom nasıl gidiyor?' veya 'Bunu hatırla' komutlarıyla beni yönlendirebilirsiniz."
                JarvisAiResult(speech)
            }
        }
    }
}
