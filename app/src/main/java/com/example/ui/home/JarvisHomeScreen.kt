package com.example.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorOrb
import com.example.ui.components.AudioEqualizerWave
import com.example.ui.components.QuickVoiceCommandChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.CriminalAmber
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.SkyTech
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import com.example.voice.AssistantVoiceState

@Composable
fun JarvisHomeScreen(
    viewModel: JarvisViewModel,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()
    val lastUserSpeech by viewModel.lastUserSpeech.collectAsState()
    val lastReply by viewModel.lastJarvisReply.collectAsState()
    val channel by viewModel.channel.collectAsState()
    val todayReport by viewModel.todayReport.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(
                isLiveApi = todayReport?.isLiveFromApi == true,
                channelConnected = channel != null,
                modifier = Modifier.clickable { onNavigateToSettings() }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceVariant)
                        .border(1.dp, CyberCardBorder, CircleShape)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ayarlar",
                        tint = TextWhite
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshYouTube() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceVariant)
                        .border(1.dp, CyberCardBorder, CircleShape)
                        .testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yenile",
                        tint = if (isRefreshing) ArcCyan else TextMuted
                    )
                }
            }
        }

        if (channel == null) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onNavigateToSettings() }
                    .testTag("connect_channel_prompt_card"),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = ArcCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "YouTube Kanalınızı Bağlayın",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "API anahtarı hazır. Canlı verileri getirmek için dokunun.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "BAĞLA >",
                        color = ArcCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Large JARVIS Identity Header
        Text(
            text = "J A R V I S",
            color = ArcCyan,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 8.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "KRİMİNAL VAKA & YOUTUBE SESLİ ZEKA MERKEZİ",
            color = TextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Central Holographic Arc Reactor Orb
        ArcReactorOrb(
            state = voiceState,
            audioRms = audioRms,
            size = 200.dp,
            onClick = {
                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.toggleListening()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Audio Equalizer Frequency Waveform
        AudioEqualizerWave(
            voiceState = voiceState,
            audioRms = audioRms
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status description
        Text(
            text = statusMsg,
            color = when (voiceState) {
                AssistantVoiceState.LISTENING -> ArcCyan
                AssistantVoiceState.THINKING -> CriminalAmber
                AssistantVoiceState.SPEAKING -> SkyTech
                AssistantVoiceState.ERROR -> CrimsonAlert
                AssistantVoiceState.IDLE -> TextMuted
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big Mic Button & Push-to-Talk action
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (voiceState == AssistantVoiceState.LISTENING) ArcCyan else CyberSurfaceVariant,
                            CyberSurface
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = if (voiceState == AssistantVoiceState.LISTENING) ArcCyan else CyberCardBorder,
                    shape = CircleShape
                )
                .clickable {
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        viewModel.toggleListening()
                    }
                }
                .padding(18.dp)
                .testTag("microphone_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (voiceState == AssistantVoiceState.LISTENING) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = "Mikrofon Dinleme",
                tint = if (voiceState == AssistantVoiceState.LISTENING) VoidBlack else ArcCyan,
                modifier = Modifier.size(34.dp)
            )
        }

        Text(
            text = "Jarvis'i dinleme moduna almak için mikrofona dokunun.",
            color = TextDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Conversation Response Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                .testTag("conversation_card"),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // User Prompt if available
                AnimatedVisibility(
                    visible = lastUserSpeech.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SİZ:",
                                color = CriminalAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "\"$lastUserSpeech\"",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CyberCardBorder)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Jarvis Verbal Output
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JARVIS YANITI:",
                        color = ArcCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = { viewModel.speak(lastReply) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Seslendir",
                            tint = ArcCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = lastReply,
                    color = TextWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Channel Snapshot Card (Today's Quick Numbers)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                .clickable { onNavigateToAnalytics() }
                .testTag("channel_snapshot_card"),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!channel?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = channel?.avatarUrl,
                                contentDescription = channel?.title,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, ArcCyan, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column {
                            Text(
                                text = channel?.title ?: "Polis Vakası",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${channel?.customUrl ?: "@polisvakasi"} • CANLI VERİ",
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArcCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "YOUTUBE CANLI",
                            color = ArcCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniItem(
                        label = "Toplam İzlenme",
                        value = formatNumber(channel?.viewCount ?: 14086258L),
                        highlight = ArcCyan
                    )
                    MetricMiniItem(
                        label = "Gerçek Abone",
                        value = formatNumber(channel?.subscriberCount ?: 106000L),
                        highlight = GreenOnline()
                    )
                    MetricMiniItem(
                        label = "Video Sayısı",
                        value = "${channel?.videoCount ?: 11} Video",
                        highlight = CriminalAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Voice Commands Scroll Row
        Text(
            text = "HIZLI SESLİ KOMUTLAR",
            color = TextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickCommands = listOf(
                "Bugün neler yaptık?",
                "Son videom nasıl gidiyor?",
                "Bu hafta kaç abone kazandık?",
                "En iyi videom hangisi?",
                "Son 10 videoyu karşılaştır.",
                "Bugün yapılacaklarımı göster."
            )
            quickCommands.forEach { cmd ->
                QuickVoiceCommandChip(
                    text = cmd,
                    onClick = { viewModel.processVoiceInput(cmd) },
                    modifier = Modifier.clickable { viewModel.processVoiceInput(cmd) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricMiniItem(
    label: String,
    value: String,
    highlight: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = label,
            color = TextDim,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = highlight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun GreenOnline() = androidx.compose.ui.graphics.Color(0xFF10B981)

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(java.util.Locale.US, "%.1fB", number / 1_000.0)
        else -> number.toString()
    }
}
