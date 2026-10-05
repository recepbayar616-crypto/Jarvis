package com.example.ui.settings

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.preferences.JarvisPreferences
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
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val prefs = viewModel.preferences
    val channel by viewModel.channel.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    var apiKeyInput by remember {
        mutableStateOf(
            if (prefs.getApiKey().isNotBlank()) prefs.getApiKey() else JarvisPreferences.DEFAULT_YOUTUBE_KEY
        )
    }
    var channelIdInput by remember { mutableStateOf(prefs.getChannelId()) }
    var oauthTokenInput by remember { mutableStateOf(prefs.getOAuthToken()) }
    var isApiKeyMasked by remember { mutableStateOf(true) }

    var autoSpeak by remember { mutableStateOf(prefs.isAutoSpeakEnabled()) }
    var speechRate by remember { mutableFloatStateOf(prefs.getSpeechRate()) }
    var speechPitch by remember { mutableFloatStateOf(prefs.getSpeechPitch()) }
    var wakeWordEnabled by remember { mutableStateOf(prefs.isWakeWordListening()) }

    var alert1k by remember { mutableStateOf(true) }
    var alert10k by remember { mutableStateOf(true) }
    var alertSpike by remember { mutableStateOf(true) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        // Title
        Text(
            text = "JARVIS AYARLARI",
            color = ArcCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "YouTube Data & Analytics API, Canlı Hesap Bağlantısı ve Ses",
            color = TextDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Active Key Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ArcCyan.copy(alpha = 0.12f))
                .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ArcCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "YOUTUBE DATA API V3 AKTİF",
                        color = ArcCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Google Cloud Console Anahtarı: AIzaSyC7...sF54 tanımlandı.",
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Connected Channel Preview (if available)
        if (channel != null) {
            Text(
                text = "BAĞLI YOUTUBE KANALI (CANLI)",
                color = GreenOnline,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GreenOnline.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!channel?.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = channel?.avatarUrl,
                            contentDescription = channel?.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, ArcCyan, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel?.title ?: "Kanal",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!channel?.customUrl.isNullOrBlank()) {
                            Text(
                                text = channel?.customUrl ?: "",
                                color = ArcCyan,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "${formatNumber(channel?.subscriberCount ?: 0L)} Abone • ${channel?.videoCount ?: 0} Video • ${formatNumber(channel?.viewCount ?: 0L)} Toplam İzlenme",
                            color = TextDim,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshYouTube() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceVariant)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ArcCyan, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Yenile", tint = ArcCyan)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 1. YouTube Connection Section
        Text(
            text = "YOUTUBE KANAL BAĞLANTISI & API YAPILANDIRMASI",
            color = CriminalAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // API Key Field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Google Cloud YouTube Data API v3 Key", color = TextDim, fontSize = 12.sp) },
                    visualTransformation = if (isApiKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { isApiKeyMasked = !isApiKeyMasked }) {
                            Icon(
                                imageVector = if (isApiKeyMasked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = ArcCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Channel ID / Handle Field
                OutlinedTextField(
                    value = channelIdInput,
                    onValueChange = { channelIdInput = it },
                    label = { Text("Kanal ID, @Handle veya Kanal Adı", color = TextDim, fontSize = 12.sp) },
                    placeholder = { Text("Örn: @polisvakasi veya UC2v6xIi94-mFQkm5_a4v2tQ", color = TextDim, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("channel_id_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = ArcCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // OAuth Bearer Token Field (Optional for Analytics)
                OutlinedTextField(
                    value = oauthTokenInput,
                    onValueChange = { oauthTokenInput = it },
                    label = { Text("Google OAuth Token (YouTube Analytics API için)", color = TextDim, fontSize = 12.sp) },
                    placeholder = { Text("Bearer ya da erişim belirteci (opsiyonel)", color = TextDim, fontSize = 12.sp) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("oauth_token_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = ArcCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Connect Button
                Button(
                    onClick = {
                        prefs.setApiKey(apiKeyInput)
                        prefs.setOAuthToken(oauthTokenInput)
                        val targetChannel = channelIdInput.ifBlank { "UC_KRIMINAL_VAKA_DOSYALARI" }
                        viewModel.connectChannel(targetChannel)
                        feedbackMessage = "Kanal bağlantısı ve canlı veri senkronizasyonu başlatıldı..."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("connect_youtube_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = VoidBlack),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = VoidBlack, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = "GOOGLE / YOUTUBE HESABINI BAĞLA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (feedbackMessage != null || syncMessage != null) {
                    Text(
                        text = feedbackMessage ?: syncMessage ?: "",
                        color = GreenOnline,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Clear Demo / Cache Data
        Text(
            text = "VERİ TEMİZLEME & CANLI SIFIRLAMA",
            color = CrimsonAlert,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Demo ve Önbellek Kayıtlarını Temizle",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Yerel veritabanındaki tüm önbellek ve örnek vaka verilerini temizler; yalnızca bağladığınız kanalın gerçek YouTube verilerini tutar.",
                    color = TextDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Button(
                    onClick = {
                        viewModel.clearDemoData()
                        feedbackMessage = "Demo ve önbellek verileri tamamen temizlendi."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("clear_demo_cache_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ÖNBELLEK VE DEMO VERİLERİ TEMİZLE", color = CrimsonAlert, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Voice & Assistant Parameters
        Text(
            text = "TÜRKÇE SES & ASİSTAN PARAMETRELERİ",
            color = CriminalAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Auto-speak toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Cevapları Seslendir (TTS)", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Jarvis yanıtları Türkçe olarak sesli okur", color = TextDim, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoSpeak,
                        onCheckedChange = {
                            autoSpeak = it
                            prefs.setAutoSpeakEnabled(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArcCyan, checkedTrackColor = ArcCyan.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Speech Rate Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Konuşma Hızı", color = TextWhite, fontSize = 13.sp)
                        Text(text = "${String.format(Locale.getDefault(), "%.2f", speechRate)}x", color = ArcCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = {
                            speechRate = it
                            prefs.setSpeechRate(it)
                        },
                        valueRange = 0.75f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = ArcCyan, activeTrackColor = ArcCyan)
                    )
                }

                // Speech Pitch Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ses Tonu (Pitch)", color = TextWhite, fontSize = 13.sp)
                        Text(text = String.format(Locale.getDefault(), "%.2f", speechPitch), color = SkyTech, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            prefs.setSpeechPitch(it)
                        },
                        valueRange = 0.7f..1.3f,
                        colors = SliderDefaults.colors(thumbColor = SkyTech, activeTrackColor = SkyTech)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wake-word mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "'Hey Jarvis' Uyandırma Modu", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Sesli komut algılama için optimize edildi", color = TextDim, fontSize = 11.sp)
                    }
                    Switch(
                        checked = wakeWordEnabled,
                        onCheckedChange = {
                            wakeWordEnabled = it
                            prefs.setWakeWordListening(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArcCyan, checkedTrackColor = ArcCyan.copy(alpha = 0.3f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Proactive Analytics & Alert Thresholds
        Text(
            text = "PROAKTİF BİLDİRİM VE EŞİKLER",
            color = CriminalAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AlertToggleRow(title = "1.000 İzlenme Kilometre Taşı", isChecked = alert1k, onToggle = { alert1k = it })
                Spacer(modifier = Modifier.height(8.dp))
                AlertToggleRow(title = "10.000 İzlenme Kilometre Taşı", isChecked = alert10k, onToggle = { alert10k = it })
                Spacer(modifier = Modifier.height(8.dp))
                AlertToggleRow(title = "Hızlı İvmelenme (+%50 İzlenme Hızı Artışı)", isChecked = alertSpike, onToggle = { alertSpike = it })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Clear History & Disconnect
        Text(
            text = "GÜVENLİK VE ÇIKIŞ",
            color = TextDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sohbet ve Komut Geçmişini Temizle", color = TextMuted, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        apiKeyInput = ""
                        channelIdInput = ""
                        oauthTokenInput = ""
                        viewModel.clearCache()
                        feedbackMessage = "YouTube bağlantısı kesildi ve kimlik bilgileri silindi."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.LinkOff, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "YouTube Bağlantısını Kes", color = CrimsonAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AlertToggleRow(
    title: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = TextWhite, fontSize = 13.sp)
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = CriminalAmber, checkedTrackColor = CriminalAmber.copy(alpha = 0.3f))
        )
    }
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%,d", number).replace(',', '.')
        else -> number.toString()
    }
}
