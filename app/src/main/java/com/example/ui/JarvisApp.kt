package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.content.ContentAssistantScreen
import com.example.ui.home.JarvisHomeScreen
import com.example.ui.memory.MemoryScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VoidBlack
import com.example.ui.videos.VideoComparisonScreen
import com.example.ui.videos.VideoDetailScreen
import com.example.ui.videos.VideosScreen

enum class JarvisNavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Asistan", Icons.Default.Home, "nav_home"),
    ANALYTICS("Rapor", Icons.Default.Analytics, "nav_analytics"),
    VIDEOS("Videolar", Icons.Default.PlayCircle, "nav_videos"),
    CONTENT("İçerik", Icons.Default.AutoAwesome, "nav_content"),
    MEMORY("Hafıza", Icons.Default.Memory, "nav_memory"),
    SETTINGS("Ayarlar", Icons.Default.Settings, "nav_settings")
}

@Composable
fun JarvisApp(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(JarvisNavTab.HOME) }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var isComparingVideos by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VoidBlack,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (selectedVideoId == null && !isComparingVideos) {
                NavigationBar(
                    containerColor = CyberSurface,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = CyberCardBorder
                    )
                ) {
                    JarvisNavTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                currentTab = tab
                                selectedVideoId = null
                                isComparingVideos = false
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VoidBlack,
                                selectedTextColor = ArcCyan,
                                unselectedIconColor = TextDim,
                                unselectedTextColor = TextDim,
                                indicatorColor = ArcCyan
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedVideoId != null -> {
                    VideoDetailScreen(
                        videoId = selectedVideoId!!,
                        viewModel = viewModel,
                        onBack = { selectedVideoId = null }
                    )
                }
                isComparingVideos -> {
                    VideoComparisonScreen(
                        viewModel = viewModel,
                        onBack = { isComparingVideos = false }
                    )
                }
                else -> {
                    when (currentTab) {
                        JarvisNavTab.HOME -> JarvisHomeScreen(
                            viewModel = viewModel,
                            onNavigateToAnalytics = { currentTab = JarvisNavTab.ANALYTICS },
                            onNavigateToSettings = { currentTab = JarvisNavTab.SETTINGS }
                        )
                        JarvisNavTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                        JarvisNavTab.VIDEOS -> VideosScreen(
                            viewModel = viewModel,
                            onVideoSelected = { selectedVideoId = it },
                            onCompareClick = { isComparingVideos = true }
                        )
                        JarvisNavTab.CONTENT -> ContentAssistantScreen(viewModel = viewModel)
                        JarvisNavTab.MEMORY -> MemoryScreen(viewModel = viewModel)
                        JarvisNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
