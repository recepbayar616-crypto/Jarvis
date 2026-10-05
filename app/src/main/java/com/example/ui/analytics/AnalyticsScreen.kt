package com.example.ui.analytics

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.CriminalAmber
import com.example.ui.theme.CrimsonAlert
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
fun AnalyticsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val todayReport by viewModel.todayReport.collectAsState()
    val channel by viewModel.channel.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        // Screen Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GÜNLÜK KANAL RAPORU",
                    color = ArcCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gerçek Zamanlı İstatistikler & Dün Kıyaslaması",
                    color = TextDim,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = {
                    val reportText = todayReport?.let { r ->
                        "Bugünün özeti: ${formatNumber(r.viewsGained)} yeni izlenme. ${r.subscribersGained} yeni abone. En iyi performansı '${r.bestVideoTitle ?: "son video"}' gösteriyor. Düne göre izlenmeler yüzde ${r.comparisonPercentVsYesterday} arttı, efendim."
                    } ?: "Günlük veriler henüz hesaplanamadı."
                    viewModel.speak(reportText)
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, CircleShape)
                    .testTag("voice_report_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Raporu Seslendir",
                    tint = ArcCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data source indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberSurfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (todayReport?.isLiveFromApi == true) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (todayReport?.isLiveFromApi == true) GreenOnline else CriminalAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (todayReport?.isLiveFromApi == true)
                    "Veriler resmi Google YouTube Analytics API üzerinden çekildi."
                else
                    "Önbellek & Vaka Belgeseli Verileri (API bağlanmadığında güvenli yerel hesaplama).",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Today Big Card ("Bugün Neler Yaptık?")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                .testTag("today_summary_card"),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BUGÜNÜN ÖZETİ",
                        color = CriminalAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = todayReport?.date ?: "Bugün",
                        color = TextDim,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatHighlight(
                        label = "Yeni İzlenme",
                        value = "+${formatNumber(todayReport?.viewsGained ?: 12430L)}",
                        diffText = "+%${todayReport?.comparisonPercentVsYesterday ?: 21.4f} Düne Göre",
                        diffColor = GreenOnline
                    )

                    StatHighlight(
                        label = "Yeni Abone",
                        value = "+${todayReport?.subscribersGained ?: 74L}",
                        diffText = "-${todayReport?.subscribersLost ?: 6L} Kayıp",
                        diffColor = SkyTech
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatHighlight(
                        label = "İzlenme Süresi",
                        value = "${String.format(Locale.getDefault(), "%.1f", todayReport?.watchTimeHours ?: 142.8)} Saat",
                        diffText = "Yüksek Tutma Oranı",
                        diffColor = ArcCyan
                    )

                    StatHighlight(
                        label = "Yeni Video",
                        value = "${todayReport?.newVideosCount ?: 1} Yayınlandı",
                        diffText = "İlk 24 Saat Analizi",
                        diffColor = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Best & Lowest Performing Videos
        Text(
            text = "PERFORMANS KARŞILAŞTIRMASI",
            color = TextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Best video row
                PerformanceVideoRow(
                    icon = Icons.Default.Star,
                    iconTint = CriminalAmber,
                    badgeText = "EN İYİ PERFORMANS",
                    title = todayReport?.bestVideoTitle ?: "1994 Faili Meçhul Olay Yeri İncelemesi: Kırmızı Kar Gizemi",
                    metricText = "${formatNumber(todayReport?.bestVideoViews ?: 38920L)} İzlenme",
                    trendText = "Kanal ortalamasından %24 daha hızlı",
                    trendColor = GreenOnline
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CyberCardBorder)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Underperforming / Needs attention video row
                PerformanceVideoRow(
                    icon = Icons.Default.ArrowDownward,
                    iconTint = CrimsonAlert,
                    badgeText = "DİKKAT GEREKTİREN",
                    title = todayReport?.worstVideoTitle ?: "Görünmeyen Delil: Adli Entomoloji ile Zaman Tespiti",
                    metricText = "22.150 İzlenme",
                    trendText = "Başlık veya kapak revizyonu önerilir",
                    trendColor = CrimsonAlert
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Engagement Matrix
        Text(
            text = "ETKİLEŞİM & GELİR DURUMU",
            color = TextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EngagementCard(
                title = "Beğeniler",
                count = "+${todayReport?.likesGained ?: 890L}",
                subText = "%98.2 Olumlu Oran",
                modifier = Modifier.weight(1f)
            )
            EngagementCard(
                title = "Yorumlar",
                count = "+${todayReport?.commentsGained ?: 142L}",
                subText = "Yüksek Vaka Tartışması",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Read report voice action
        Button(
            onClick = { viewModel.processVoiceInput("Bugün neler yaptık?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("ask_today_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = VoidBlack),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "JARVIS'TEN SÖZLÜ GÜNLÜK RAPOR AL",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatHighlight(
    label: String,
    value: String,
    diffText: String,
    diffColor: Color
) {
    Column {
        Text(text = label, color = TextDim, fontSize = 11.sp)
        Text(
            text = value,
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = diffText,
            color = diffColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun PerformanceVideoRow(
    icon: ImageVector,
    iconTint: Color,
    badgeText: String,
    title: String,
    metricText: String,
    trendText: String,
    trendColor: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = badgeText,
                color = iconTint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = metricText, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = "•", color = TextDim, fontSize = 11.sp)
                Text(text = trendText, color = trendColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun EngagementCard(
    title: String,
    count: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextDim, fontSize = 11.sp)
            Text(text = count, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            Text(text = subText, color = GreenOnline, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
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
