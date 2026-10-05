package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AlertThresholdEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.local.entity.DailyAnalyticsEntity
import com.example.data.local.entity.TaskMemoryEntity
import com.example.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Channel
    @Query("SELECT * FROM channels LIMIT 1")
    fun getChannelFlow(): Flow<ChannelEntity?>

    @Query("SELECT * FROM channels LIMIT 1")
    suspend fun getChannel(): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels")
    suspend fun clearChannel()

    // Videos
    @Query("SELECT * FROM videos ORDER BY publishedAt DESC")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY publishedAt DESC")
    suspend fun getAllVideos(): List<VideoEntity>

    @Query("SELECT * FROM videos ORDER BY viewCount DESC LIMIT :limit")
    suspend fun getTopVideos(limit: Int): List<VideoEntity>

    @Query("SELECT * FROM videos WHERE videoId = :id LIMIT 1")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE videoId = :id LIMIT 1")
    fun getVideoByIdFlow(id: String): Flow<VideoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("UPDATE videos SET aiAnalysis = :analysis WHERE videoId = :videoId")
    suspend fun updateVideoAnalysis(videoId: String, analysis: String)

    @Query("DELETE FROM videos")
    suspend fun clearVideos()

    // Analytics
    @Query("SELECT * FROM daily_analytics ORDER BY date DESC LIMIT 1")
    fun getLatestAnalyticsFlow(): Flow<DailyAnalyticsEntity?>

    @Query("SELECT * FROM daily_analytics ORDER BY date DESC LIMIT 1")
    suspend fun getLatestAnalytics(): DailyAnalyticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyAnalytics(analytics: DailyAnalyticsEntity)

    // Tasks & Memory
    @Query("SELECT * FROM tasks_memory ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskMemoryEntity>>

    @Query("SELECT * FROM tasks_memory WHERE isCompleted = 0 ORDER BY createdAt DESC")
    suspend fun getPendingTasks(): List<TaskMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskMemoryEntity): Long

    @Update
    suspend fun updateTask(task: TaskMemoryEntity)

    @Delete
    suspend fun deleteTask(task: TaskMemoryEntity)

    @Query("DELETE FROM tasks_memory")
    suspend fun clearTasks()

    // Conversation History
    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC LIMIT 100")
    fun getConversationFlow(): Flow<List<ConversationMessageEntity>>

    @Insert
    suspend fun insertMessage(message: ConversationMessageEntity)

    @Query("DELETE FROM conversation_messages")
    suspend fun clearConversation()

    // Alert Thresholds
    @Query("SELECT * FROM alert_thresholds")
    fun getAllAlertsFlow(): Flow<List<AlertThresholdEntity>>

    @Query("SELECT * FROM alert_thresholds WHERE isEnabled = 1")
    suspend fun getEnabledAlerts(): List<AlertThresholdEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertThresholdEntity>)

    @Update
    suspend fun updateAlert(alert: AlertThresholdEntity)
}
