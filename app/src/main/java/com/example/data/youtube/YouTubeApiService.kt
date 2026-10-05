package com.example.data.youtube

import com.example.data.youtube.model.ChannelListResponse
import com.example.data.youtube.model.PlaylistItemsResponse
import com.example.data.youtube.model.VideoListResponse
import com.example.data.youtube.model.YouTubeAnalyticsReportResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface YouTubeDataApiService {
    @GET("youtube/v3/channels")
    suspend fun getChannelDetails(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") channelId: String? = null,
        @Query("forHandle") forHandle: String? = null,
        @Query("forUsername") forUsername: String? = null,
        @Query("mine") mine: Boolean? = null,
        @Query("key") apiKey: String? = null,
        @Header("Authorization") authorization: String? = null
    ): ChannelListResponse

    @GET("youtube/v3/search")
    suspend fun searchChannels(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "channel",
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 5,
        @Query("key") apiKey: String? = null,
        @Header("Authorization") authorization: String? = null
    ): com.example.data.youtube.model.SearchListResponse

    @GET("youtube/v3/playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String? = null,
        @Header("Authorization") authorization: String? = null
    ): PlaylistItemsResponse

    @GET("youtube/v3/videos")
    suspend fun getVideosList(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String? = null,
        @Header("Authorization") authorization: String? = null
    ): VideoListResponse
}

interface YouTubeAnalyticsApiService {
    @GET("v2/reports")
    suspend fun getAnalyticsReport(
        @Query("ids") ids: String = "channel==MINE",
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("metrics") metrics: String = "views,estimatedMinutesWatched,averageViewDuration,subscribersGained,subscribersLost,likes,comments",
        @Query("dimensions") dimensions: String? = "day",
        @Query("sort") sort: String? = "-day",
        @Header("Authorization") authorization: String
    ): YouTubeAnalyticsReportResponse
}

object YouTubeNetworkClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val dataApi: YouTubeDataApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(YouTubeDataApiService::class.java)
    }

    val analyticsApi: YouTubeAnalyticsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://youtubeanalytics.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(YouTubeAnalyticsApiService::class.java)
    }
}
