package com.example.data.youtube.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChannelListResponse(
    @Json(name = "items") val items: List<ChannelItem>? = null
)

@JsonClass(generateAdapter = true)
data class ChannelItem(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: ChannelSnippet?,
    @Json(name = "statistics") val statistics: ChannelStatistics?,
    @Json(name = "contentDetails") val contentDetails: ChannelContentDetails?
)

@JsonClass(generateAdapter = true)
data class ChannelSnippet(
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "customUrl") val customUrl: String?,
    @Json(name = "thumbnails") val thumbnails: ThumbnailsContainer?
)

@JsonClass(generateAdapter = true)
data class ChannelStatistics(
    @Json(name = "viewCount") val viewCount: String?,
    @Json(name = "subscriberCount") val subscriberCount: String?,
    @Json(name = "hiddenSubscriberCount") val hiddenSubscriberCount: Boolean?,
    @Json(name = "videoCount") val videoCount: String?
)

@JsonClass(generateAdapter = true)
data class ChannelContentDetails(
    @Json(name = "relatedPlaylists") val relatedPlaylists: RelatedPlaylists?
)

@JsonClass(generateAdapter = true)
data class RelatedPlaylists(
    @Json(name = "uploads") val uploads: String?
)

@JsonClass(generateAdapter = true)
data class SearchListResponse(
    @Json(name = "items") val items: List<SearchResultItem>? = null
)

@JsonClass(generateAdapter = true)
data class SearchResultItem(
    @Json(name = "id") val id: SearchResourceId?,
    @Json(name = "snippet") val snippet: SearchSnippet?
)

@JsonClass(generateAdapter = true)
data class SearchResourceId(
    @Json(name = "kind") val kind: String?,
    @Json(name = "channelId") val channelId: String?,
    @Json(name = "videoId") val videoId: String?
)

@JsonClass(generateAdapter = true)
data class SearchSnippet(
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "channelTitle") val channelTitle: String?,
    @Json(name = "thumbnails") val thumbnails: ThumbnailsContainer?
)

@JsonClass(generateAdapter = true)
data class PlaylistItemsResponse(
    @Json(name = "items") val items: List<PlaylistItem>? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItem(
    @Json(name = "contentDetails") val contentDetails: PlaylistItemContentDetails?,
    @Json(name = "snippet") val snippet: PlaylistItemSnippet?
)

@JsonClass(generateAdapter = true)
data class PlaylistItemContentDetails(
    @Json(name = "videoId") val videoId: String?
)

@JsonClass(generateAdapter = true)
data class PlaylistItemSnippet(
    @Json(name = "title") val title: String?,
    @Json(name = "publishedAt") val publishedAt: String?
)

@JsonClass(generateAdapter = true)
data class VideoListResponse(
    @Json(name = "items") val items: List<VideoItem>? = null
)

@JsonClass(generateAdapter = true)
data class VideoItem(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: VideoSnippet?,
    @Json(name = "contentDetails") val contentDetails: VideoContentDetails?,
    @Json(name = "statistics") val statistics: VideoStatistics?
)

@JsonClass(generateAdapter = true)
data class VideoSnippet(
    @Json(name = "publishedAt") val publishedAt: String?,
    @Json(name = "channelId") val channelId: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "thumbnails") val thumbnails: ThumbnailsContainer?
)

@JsonClass(generateAdapter = true)
data class VideoContentDetails(
    @Json(name = "duration") val duration: String? // e.g. "PT15M33S"
)

@JsonClass(generateAdapter = true)
data class VideoStatistics(
    @Json(name = "viewCount") val viewCount: String?,
    @Json(name = "likeCount") val likeCount: String?,
    @Json(name = "commentCount") val commentCount: String?
)

@JsonClass(generateAdapter = true)
data class ThumbnailsContainer(
    @Json(name = "default") val defaultThumb: ThumbnailDetails?,
    @Json(name = "medium") val mediumThumb: ThumbnailDetails?,
    @Json(name = "high") val highThumb: ThumbnailDetails?
)

@JsonClass(generateAdapter = true)
data class ThumbnailDetails(
    @Json(name = "url") val url: String?,
    @Json(name = "width") val width: Int?,
    @Json(name = "height") val height: Int?
)

@JsonClass(generateAdapter = true)
data class YouTubeAnalyticsReportResponse(
    @Json(name = "columnHeaders") val columnHeaders: List<AnalyticsColumnHeader>? = null,
    @Json(name = "rows") val rows: List<List<Any>>? = null
)

@JsonClass(generateAdapter = true)
data class AnalyticsColumnHeader(
    @Json(name = "name") val name: String?,
    @Json(name = "columnType") val columnType: String?,
    @Json(name = "dataType") val dataType: String?
)
