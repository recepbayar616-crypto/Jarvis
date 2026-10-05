package com.example.ai.gemini

import com.example.data.local.dao.JarvisDao
import com.example.data.local.entity.TaskMemoryEntity
import com.example.data.youtube.YouTubeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JarvisToolExecutor(
    private val youTubeRepository: YouTubeRepository,
    private val dao: JarvisDao
) {
    val declarations: List<GeminiFunctionDeclaration> = listOf(
        GeminiFunctionDeclaration(
            name = "getTodayAnalytics",
            description = "YouTube kanalının bugünkü gerçek izlenme, yeni abone, yüklenen video sayısı, izlenme süresi ve düne göre kıyaslama verilerini getirir.",
            parameters = GeminiFunctionParameters(type = "OBJECT", properties = emptyMap())
        ),
        GeminiFunctionDeclaration(
            name = "getChannelOverview",
            description = "Kanalın adı, toplam abone sayısı, toplam izlenme ve toplam video sayısı genel durumunu getirir.",
            parameters = GeminiFunctionParameters(type = "OBJECT", properties = emptyMap())
        ),
        GeminiFunctionDeclaration(
            name = "getLatestVideoPerformance",
            description = "Kanalın en son yayınladığı videonun adı, izlenme sayısı, beğeni ve performans analizini getirir.",
            parameters = GeminiFunctionParameters(type = "OBJECT", properties = emptyMap())
        ),
        GeminiFunctionDeclaration(
            name = "getTopVideos",
            description = "Kanalın en çok izlenen ve en iyi performans gösteren videolarını getirir.",
            parameters = GeminiFunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "limit" to GeminiParameterProperty("INTEGER", "Getirilecek video adedi (örn. 3 veya 5)")
                )
            )
        ),
        GeminiFunctionDeclaration(
            name = "getRecentVideos",
            description = "Son yayınlanan videoların listesini ve performanslarını getirir.",
            parameters = GeminiFunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "limit" to GeminiParameterProperty("INTEGER", "Video sayısı (örn. 5 veya 10)")
                )
            )
        ),
        GeminiFunctionDeclaration(
            name = "compareLastVideos",
            description = "Son iki videoyu veya son videoyla önceki videoyu izlenme ve etkileşim açısından kıyaslar.",
            parameters = GeminiFunctionParameters(type = "OBJECT", properties = emptyMap())
        ),
        GeminiFunctionDeclaration(
            name = "getSubscriberGrowth",
            description = "Bu hafta veya son dönemdeki yeni abone kazanımı ve büyüme oranını getirir.",
            parameters = GeminiFunctionParameters(type = "OBJECT", properties = emptyMap())
        ),
        GeminiFunctionDeclaration(
            name = "rememberTask",
            description = "Kullanıcının 'Bunu hatırla' dediği vaka araştırması, kurgu veya hatırlatma notunu kaydeder.",
            parameters = GeminiFunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "note" to GeminiParameterProperty("STRING", "Hatırlanacak not veya görev metni"),
                    "category" to GeminiParameterProperty("STRING", "Kategori (örn: 'Vaka Araştırması', 'Video Kurgu', 'Hatırlatıcı')")
                ),
                required = listOf("note")
            )
        ),
        GeminiFunctionDeclaration(
            name = "getPendingTasks",
            description = "Kullanıcının kayıtlı yapılacaklar listesini, vaka notlarını ve görevlerini getirir.",
            parameters = GeminiFunctionParameters(type = "OBJECT", properties = emptyMap())
        )
    )

    suspend fun execute(name: String, args: Map<String, Any?>?): Map<String, Any?> = withContext(Dispatchers.IO) {
        when (name) {
            "getTodayAnalytics" -> {
                val report = youTubeRepository.getTodayReport()
                mapOf(
                    "viewsGainedToday" to report.viewsGained,
                    "subscribersGainedToday" to report.subscribersGained,
                    "subscribersLostToday" to report.subscribersLost,
                    "watchTimeHoursToday" to report.watchTimeHours,
                    "newVideosUploadedToday" to report.newVideosCount,
                    "bestPerformingVideoToday" to (report.bestVideoTitle ?: "Mevcut Değil"),
                    "viewsPercentChangeVsYesterday" to report.comparisonPercentVsYesterday,
                    "isLiveApiConnected" to report.isLiveFromApi
                )
            }
            "getChannelOverview" -> {
                val ch = dao.getChannel()
                if (ch != null) {
                    mapOf(
                        "channelName" to ch.title,
                        "subscriberCount" to ch.subscriberCount,
                        "totalViews" to ch.viewCount,
                        "totalVideos" to ch.videoCount,
                        "customUrl" to (ch.customUrl ?: "")
                    )
                } else {
                    mapOf("error" to "YouTube kanalı verisi henüz indirilmedi veya bağlı değil.")
                }
            }
            "getLatestVideoPerformance" -> {
                val videos = dao.getAllVideos()
                val latest = videos.firstOrNull()
                if (latest != null) {
                    mapOf(
                        "title" to latest.title,
                        "publishedAt" to latest.publishedAt,
                        "viewCount" to latest.viewCount,
                        "likeCount" to latest.likeCount,
                        "commentCount" to latest.commentCount,
                        "duration" to latest.duration,
                        "ctrPercentage" to latest.ctrPercentage,
                        "retentionPercent" to latest.avgPercentageViewed,
                        "aiNote" to (latest.aiAnalysis ?: "Kanal ortalamasının üzerinde başlangıç temposu.")
                    )
                } else {
                    mapOf("error" to "Henüz kayıtlı video bulunamadı.")
                }
            }
            "getTopVideos" -> {
                val limit = (args?.get("limit") as? Number)?.toInt() ?: 3
                val top = dao.getTopVideos(limit)
                mapOf(
                    "videos" to top.map {
                        mapOf(
                            "title" to it.title,
                            "views" to it.viewCount,
                            "likes" to it.likeCount,
                            "publishedAt" to it.publishedAt
                        )
                    }
                )
            }
            "getRecentVideos" -> {
                val limit = (args?.get("limit") as? Number)?.toInt() ?: 5
                val all = dao.getAllVideos().take(limit)
                mapOf(
                    "videos" to all.map {
                        mapOf(
                            "title" to it.title,
                            "views" to it.viewCount,
                            "likes" to it.likeCount,
                            "publishedAt" to it.publishedAt,
                            "duration" to it.duration
                        )
                    }
                )
            }
            "compareLastVideos" -> {
                val videos = dao.getAllVideos()
                if (videos.size >= 2) {
                    val comp = youTubeRepository.compareVideos(videos[0].videoId, videos[1].videoId)
                    if (comp != null) {
                        mapOf(
                            "video1" to comp.video1.title,
                            "video1Views" to comp.video1.viewCount,
                            "video2" to comp.video2.title,
                            "video2Views" to comp.video2.viewCount,
                            "viewsDifference" to comp.viewsDiff,
                            "percentageDifference" to comp.viewsDiffPercent,
                            "verdict" to comp.comparisonSummary
                        )
                    } else {
                        mapOf("error" to "Kıyaslama yapılamadı.")
                    }
                } else {
                    mapOf("error" to "Kıyaslama için en az 2 video gereklidir.")
                }
            }
            "getSubscriberGrowth" -> {
                val report = youTubeRepository.getTodayReport()
                val ch = dao.getChannel()
                mapOf(
                    "currentSubscribers" to (ch?.subscriberCount ?: 0L),
                    "gainedToday" to report.subscribersGained,
                    "estimatedWeeklyGrowth" to (report.subscribersGained * 7),
                    "trend" to if (report.subscribersGained > 50) "Güçlü artış trendi" else "Stabil büyüme"
                )
            }
            "rememberTask" -> {
                val note = args?.get("note")?.toString() ?: "Vaka Hatırlatıcısı"
                val category = args?.get("category")?.toString() ?: "Vaka Araştırması"
                val id = dao.insertTask(
                    TaskMemoryEntity(
                        title = note,
                        category = category
                    )
                )
                mapOf(
                    "status" to "success",
                    "taskId" to id,
                    "savedNote" to note,
                    "message" to "Hafızaya başarıyla kaydedildi."
                )
            }
            "getPendingTasks" -> {
                val tasks = dao.getPendingTasks()
                mapOf(
                    "taskCount" to tasks.size,
                    "tasks" to tasks.map { mapOf("id" to it.id, "title" to it.title, "category" to it.category) }
                )
            }
            else -> mapOf("error" to "Bilinmeyen fonksiyon: $name")
        }
    }
}
