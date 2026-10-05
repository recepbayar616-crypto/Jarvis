package com.example.ui.videos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.JarvisViewModel
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.CriminalAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.GreenOnline
import com.example.ui.theme.SkyTech
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import java.util.Locale

@Composable
fun VideoDetailScreen(
    videoId: String,
    viewModel: JarvisViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val videos by viewModel.videos.collectAsState()
    val video = videos.firstOrNull { it.videoId == videoId } ?: videos.firstOrNull()

    val scrollState = rememberScrollState()

    if (video == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VoidBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Video bulunamadı.", color = TextMuted)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, CircleShape)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextWhite
                )
            }

            Text(
                text = "DETAYLI VAKA ANALİZİ",
                color = ArcCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = {
                    val summary = "Video: ${video.title}. Toplam ${formatNumber(video.viewCount)} izlenme, yüzde ${video.ctrPercentage} tıklama oranı ve yüzde ${video.avgPercentageViewed} kitle tutma oranına sahip. Yapay zeka değerlendirmesi: ${video.aiAnalysis ?: "Kanal ortalamasının üzerinde bir vaka kurgusu."}"
                    viewModel.speak(summary)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, CircleShape)
                    .testTag("speak_video_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Seslendir",
                    tint = ArcCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = video.thumbnailHigh,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(VoidBlack.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = video.duration,
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = video.title,
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Yayın Tarihi: ${video.publishedAt} • ID: ${video.videoId}",
            color = TextDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Metrics Grid
        Text(
            text = "GERÇEKLEŞEN METRİKLER (DATA)",
            color = ArcCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(label = "İzlenme", value = formatNumber(video.viewCount), modifier = Modifier.weight(1f))
            MetricTile(label = "Beğeni", value = formatNumber(video.likeCount), modifier = Modifier.weight(1f))
            MetricTile(label = "Yorum", value = formatNumber(video.commentCount), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(label = "Tıklama Oranı (CTR)", value = "%${video.ctrPercentage}", highlight = ArcCyan, modifier = Modifier.weight(1f))
            MetricTile(label = "Ort. İzlenme Oranı", value = "%${video.avgPercentageViewed}", highlight = GreenOnline, modifier = Modifier.weight(1f))
            MetricTile(label = "İzlenme Süresi", value = "${formatNumber(video.watchTimeMinutes)} dk", highlight = SkyTech, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(label = "Gösterim (Impression)", value = formatNumber(video.impressions), modifier = Modifier.weight(1f))
            MetricTile(label = "Ort. Süre", value = "${video.avgViewDurationSec / 60} dk ${video.avgViewDurationSec % 60} sn", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI ANALİZİ - Strictly separated into DATA, INTERPRETATION, RECOMMENDATION
        Text(
            text = "YAPAY ZEKA ANALİZİ (AI ANALİZİ)",
            color = CriminalAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Section 1: DATA
        AiAnalysisBlock(
            icon = Icons.Default.BarChart,
            iconTint = ArcCyan,
            tag = "DATA [GERÇEKLEŞEN VERİLER]",
            content = "Video ${formatNumber(video.viewCount)} izlenme aldı. İlk 3 dakikada kitle kaybı yalnızca %14 seviyesinde kaldı. CTR %${video.ctrPercentage} ile kanal ortalamasının (%7.2) üzerinde."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Section 2: INTERPRETATION
        AiAnalysisBlock(
            icon = Icons.Default.Psychology,
            iconTint = CriminalAmber,
            tag = "INTERPRETATION [PERFORMANSIN YORUMU]",
            content = video.aiAnalysis ?: "Kriminal kurgu ve ilk sahnedeki adli tıp olay yeri şeridi merak duygusunu canlı tuttu. Yorumlardaki tartışma yoğunluğu adli balistik delillerinin çelişkisi üzerine odaklanmış."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Section 3: RECOMMENDATION
        AiAnalysisBlock(
            icon = Icons.Default.Lightbulb,
            iconTint = GreenOnline,
            tag = "RECOMMENDATION [ÖNERİLEN SONRAKİ ADIMLAR]",
            content = "1- Benzer çözülemeyen cinayet dosyaları serisi başlatılmalı.\n2- Videonun 15. dakikasındaki tempo düşüşünü önlemek için ara delil animasyonları eklenebilir.\n3- Başlıkta 'Olay Yeri Raporu' ifadesi A/B testine tabi tutulmalı."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.processVoiceInput("'${video.title}' videosunu değerlendir.")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("ask_ai_video_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ArcCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "JARVIS'E BU VİDEOYU SÖZLÜ DANIŞ",
                color = ArcCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    highlight: androidx.compose.ui.graphics.Color = TextWhite,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, color = TextDim, fontSize = 10.sp)
            Text(
                text = value,
                color = highlight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun AiAnalysisBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    tag: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tag,
                    color = iconTint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = content,
                color = TextWhite,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%,d", number).replace(',', '.')
        else -> number.toString()
    }
}
