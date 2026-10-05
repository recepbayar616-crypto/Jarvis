package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val customUrl: String?,
    val subscriberCount: Long,
    val viewCount: Long,
    val videoCount: Long,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val description: String,
    val publishedAt: String,
    val duration: String,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val thumbnailHigh: String,
    val watchTimeMinutes: Long = 0,
    val avgViewDurationSec: Int = 0,
    val avgPercentageViewed: Float = 0f,
    val impressions: Long = 0,
    val ctrPercentage: Float = 0f,
    val aiAnalysis: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_analytics")
data class DailyAnalyticsEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val viewsGained: Long,
    val subscribersGained: Long,
    val subscribersLost: Long,
    val watchTimeHours: Double,
    val likesGained: Long,
    val commentsGained: Long,
    val newVideosCount: Int,
    val topVideoId: String?,
    val topVideoTitle: String?,
    val worstVideoId: String?,
    val worstVideoTitle: String?,
    val comparisonVsYesterdayViewsPercent: Float,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks_memory")
data class TaskMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val details: String? = null,
    val category: String = "Soruşturma / Video", // e.g. "Vaka Araştırması", "Kurgu", "Kapak"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversation_messages")
data class ConversationMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "JARVIS"
    val message: String,
    val toolName: String? = null,
    val toolResultSnippet: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alert_thresholds")
data class AlertThresholdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "VIEWS_1K", "VIEWS_10K", "VELOCITY_SPIKE", "DROP"
    val thresholdValue: Long,
    val isEnabled: Boolean = true,
    val lastTriggeredMessage: String? = null,
    val lastTriggeredAt: Long? = null
)
