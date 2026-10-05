package com.example.ui.videos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VideoEntity
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
fun VideoComparisonScreen(
    viewModel: JarvisViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val videos by viewModel.videos.collectAsState()
    var selectedIndex1 by remember { mutableStateOf(0) }
    var selectedIndex2 by remember { mutableStateOf(if (videos.size > 1) 1 else 0) }

    val v1: VideoEntity? = videos.getOrNull(selectedIndex1)
    val v2: VideoEntity? = videos.getOrNull(selectedIndex2)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        // Header
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
                    .testTag("back_button_comparison")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextWhite)
            }

            Text(
                text = "VİDEO KIYASLAMA LABORATUVARI",
                color = ArcCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = {
                    if (v1 != null && v2 != null) {
                        val speech = "'${v1.title}' ile '${v2.title}' karşılaştırıldı. ${if (v1.viewCount >= v2.viewCount) "'${v1.title}' ${formatNumber(v1.viewCount)} izlenme ile önde." else "'${v2.title}' ${formatNumber(v2.viewCount)} izlenme ile önde."}"
                        viewModel.speak(speech)
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, CircleShape)
                    .testTag("speak_comparison_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Seslendir", tint = ArcCyan)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (videos.size < 2 || v1 == null || v2 == null) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(text = "Karşılaştırma için en az 2 video gereklidir.", color = TextMuted)
            }
            return
        }

        // Side-by-side title cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Video 1
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, ArcCyan, RoundedCornerShape(12.dp))
                    .clickable {
                        selectedIndex1 = (selectedIndex1 + 1) % videos.size
                    },
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "VİDEO A (DOKUN: DEĞİŞTİR)", color = ArcCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = v1.title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
                    Text(text = v1.publishedAt, color = TextDim, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }

            // Video 2
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CriminalAmber, RoundedCornerShape(12.dp))
                    .clickable {
                        selectedIndex2 = (selectedIndex2 + 1) % videos.size
                    },
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "VİDEO B (DOKUN: DEĞİŞTİR)", color = CriminalAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = v2.title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
                    Text(text = v2.publishedAt, color = TextDim, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Visual Comparison Bars
        Text(
            text = "METRİK KIYASLAMA TABLOSU",
            color = TextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        ComparisonBarItem(
            label = "İzlenme Sayısı",
            val1 = v1.viewCount.toFloat(),
            val2 = v2.viewCount.toFloat(),
            text1 = formatNumber(v1.viewCount),
            text2 = formatNumber(v2.viewCount)
        )

        Spacer(modifier = Modifier.height(14.dp))

        ComparisonBarItem(
            label = "Beğeniler",
            val1 = v1.likeCount.toFloat(),
            val2 = v2.likeCount.toFloat(),
            text1 = formatNumber(v1.likeCount),
            text2 = formatNumber(v2.likeCount)
        )

        Spacer(modifier = Modifier.height(14.dp))

        ComparisonBarItem(
            label = "Yorum Yoğunluğu",
            val1 = v1.commentCount.toFloat(),
            val2 = v2.commentCount.toFloat(),
            text1 = formatNumber(v1.commentCount),
            text2 = formatNumber(v2.commentCount)
        )

        Spacer(modifier = Modifier.height(14.dp))

        ComparisonBarItem(
            label = "Tıklama Oranı (CTR %)",
            val1 = v1.ctrPercentage,
            val2 = v2.ctrPercentage,
            text1 = "%${v1.ctrPercentage}",
            text2 = "%${v2.ctrPercentage}"
        )

        Spacer(modifier = Modifier.height(14.dp))

        ComparisonBarItem(
            label = "Kitle Tutma Oranı (%)",
            val1 = v1.avgPercentageViewed,
            val2 = v2.avgPercentageViewed,
            text1 = "%${v1.avgPercentageViewed}",
            text2 = "%${v2.avgPercentageViewed}"
        )

        Spacer(modifier = Modifier.height(22.dp))

        // AI Comparison Verdict Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "JARVIS KIYASLAMA KARARI", color = ArcCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                val diff = v1.viewCount - v2.viewCount
                val winner = if (v1.viewCount >= v2.viewCount) v1 else v2
                val diffPercent = if (v2.viewCount > 0) ((diff.toFloat() / v2.viewCount) * 100) else 0f

                Text(
                    text = "'${winner.title}' videosu daha yüksek kitle ilgisine ulaştı. Kriminal incelemelerde sorgu veya somut adli balistik delillerine odaklanan başlıklar, genel kronolojik vaka anlatımlarına göre belirgin şekilde daha yüksek CTR ve kitle tutma sağlıyor.",
                    color = TextWhite,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ComparisonBarItem(
    label: String,
    val1: Float,
    val2: Float,
    text1: String,
    text2: String
) {
    val total = (val1 + val2).coerceAtLeast(1f)
    val weight1 = (val1 / total).coerceIn(0.1f, 0.9f)
    val weight2 = 1f - weight1

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$text1 (A)", color = ArcCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextDim, fontSize = 11.sp)
            Text(text = "(B) $text2", color = CriminalAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(CyberSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .weight(weight1)
                    .fillMaxSize()
                    .background(ArcCyan)
            )
            Box(
                modifier = Modifier
                    .weight(weight2)
                    .fillMaxSize()
                    .background(CriminalAmber)
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
