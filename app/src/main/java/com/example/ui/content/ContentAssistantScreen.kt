package com.example.ui.content

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class ContentGeneratorType {
    TITLES,
    INTRO_HOOK,
    THUMBNAIL_IDEA,
    CASE_TOPIC
}

@Composable
fun ContentAssistantScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(ContentGeneratorType.TITLES) }
    var caseSubjectInput by remember { mutableStateOf("1996 Olay Yeri Parmak İzi ve Çelişkili İtiraflar") }
    var generatedResult by remember {
        mutableStateOf(
            """
            1. Olay Yerinde Kalan Tek Delil: 30 Yıl Sonra Çözülen Parmak İzi
            2. Çelişkili İtiraflar ve Karanlık Oda: Katil Nerede Hata Yaptı?
            3. Kusursuz Sanılan Cinayet: Adli Tıbbın Gözünden Kaçmayan İz
            4. 1996 Faili Meçhul Dosyası: Polis Tutanaklarının Gizlediği Sır
            5. Şüphelinin 4 Saatte Çöken Yalanı: Sorgu Odası Tutanakları
            """.trimIndent()
        )
    }

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
            Column {
                Text(
                    text = "İÇERİK & VAKA ASİSTANI",
                    color = ArcCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Adli Belgesel ve Soruşturma Kurgu Motoru",
                    color = TextDim,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = { viewModel.speak(generatedResult) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, CircleShape)
                    .testTag("speak_content_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Seslendir", tint = ArcCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Investigative Ethics Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CriminalAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = CriminalAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "BELGESEL İLKESİ: Adli vakalarda uydurma delil veya teyitsiz suçlama yapılmaz. Olay yeri tutanaklarına ve resmi adli raporlara sadık kalınır.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selection Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeSelectButton(
                title = "5 Başlık",
                icon = Icons.Default.Title,
                isSelected = selectedType == ContentGeneratorType.TITLES,
                onClick = {
                    selectedType = ContentGeneratorType.TITLES
                    generateResult(selectedType, caseSubjectInput) { generatedResult = it }
                },
                modifier = Modifier.weight(1f)
            )

            ModeSelectButton(
                title = "Giriş (Hook)",
                icon = Icons.Default.RecordVoiceOver,
                isSelected = selectedType == ContentGeneratorType.INTRO_HOOK,
                onClick = {
                    selectedType = ContentGeneratorType.INTRO_HOOK
                    generateResult(selectedType, caseSubjectInput) { generatedResult = it }
                },
                modifier = Modifier.weight(1f)
            )

            ModeSelectButton(
                title = "Thumbnail",
                icon = Icons.Default.CameraAlt,
                isSelected = selectedType == ContentGeneratorType.THUMBNAIL_IDEA,
                onClick = {
                    selectedType = ContentGeneratorType.THUMBNAIL_IDEA
                    generateResult(selectedType, caseSubjectInput) { generatedResult = it }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Topic / Subject Input Field
        OutlinedTextField(
            value = caseSubjectInput,
            onValueChange = { caseSubjectInput = it },
            label = { Text("İncelenecek Vaka veya Konu", color = TextDim) },
            placeholder = { Text("Örn: Terk edilmiş araç ve kayıp balistik raporu...", color = TextDim) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("case_subject_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ArcCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = ArcCyan
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                generateResult(selectedType, caseSubjectInput) { generatedResult = it }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_content_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = VoidBlack),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "JARVIS İLE ÜRET & GELİŞTİR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Output Result Card
        Text(
            text = "ÜRETİLEN İÇERİK METNİ",
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
                Text(
                    text = generatedResult,
                    color = TextWhite,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.addTask(
                                    title = "Üretilen İçerik: $caseSubjectInput",
                                    category = "Video Hazırlığı",
                                    details = generatedResult
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("save_as_task_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Hafızaya Kaydet", color = ArcCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ModeSelectButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ArcCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) ArcCyan else CyberCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ArcCyan else TextDim,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (isSelected) TextWhite else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

private fun generateResult(
    type: ContentGeneratorType,
    topic: String,
    onResult: (String) -> Unit
) {
    val cleanTopic = if (topic.isBlank()) "Olay Yeri İncelemesi" else topic
    when (type) {
        ContentGeneratorType.TITLES -> {
            onResult(
                """
                1. $cleanTopic: Olay Yerindeki Tek Mikroskobik Delil
                2. Polis Tutanaklarındaki 3 Dakikalık Çelişki: $cleanTopic
                3. Kusursuz Cinayet Masalı Nasıl Çöktü? ($cleanTopic)
                4. Karanlık Dosya: Adli Tıbbın Çözdüğü En Zor Soruşturma
                5. Şüphelinin Gizlediği Kanıt: Balistik Tutanakları
                """.trimIndent()
            )
        }
        ContentGeneratorType.INTRO_HOOK -> {
            onResult(
                """
                [GİRİŞ / HOOK METNİ - SÜRE: 45 SANİYE]
                
                "Saat gece 03:14... Yağmurlu bir sonbahar gecesinde gelen tek bir ihbar telefonu, adli tıp tarihimizin en karmaşık cinayet soruşturmalarından birini başlattı.
                
                Olay mahalline ilk ulaşan polis memurlarının kaydettiği görüntülerde hiçbir zorlama izi yoktu. Kapı içeriden kilitliydi. Ancak kriminal masanın olay yeri şeridinin hemen altında bulduğu o tek saç teli, katilin kusursuz planını 14 saat içinde yerle bir edecekti.
                
                Bugün dosyamızda: $cleanTopic vakasının gizli kalmış adli tutanaklarını açıyoruz."
                """.trimIndent()
            )
        }
        ContentGeneratorType.THUMBNAIL_IDEA -> {
            onResult(
                """
                [THUMBNAIL (KAPAK GÖRSELİ) TASARIM PLANI]
                
                • Sol Alan: Karanlık olay yeri bandı (Sarı-Siyah 'POLICE LINE DO NOT CROSS') hafif bulanıklaştırılmış arka plan.
                • Merkez: Olay yeri tebeşir çizgisi ve üzerinde kırmızıyla işaretlenmiş şüpheli delil zarfı (Adli Numune #3).
                • Sağ Alan: Şüphelinin gölgede kalmış silüeti veya mikrofonlu sorgu lambası ışığı.
                • Yazı Stili: Yüksek kontrastlı beyaz/sarı kalın sans-serif: "TEK BİR HATA!"
                • Merak Unsuru: Asla katilin yüzünü gösterme; adli kanıtın gizemini vurgula.
                """.trimIndent()
            )
        }
        ContentGeneratorType.CASE_TOPIC -> {
            onResult(
                """
                [ADLİ VAKA DOSYASI ÖNERİSİ]
                
                Konu: $cleanTopic
                Temel Soru: Şüphelinin alibisi neden çöktü?
                Adli Delil Odak Noktası: Adli entomoloji (böcek bilimi) ve baz istasyonu sinyal analizi.
                Anlatım Tonu: Soğukkanlı, tarafsız, resmi soruşturma aşamaları odaklı belgesel formatı.
                """.trimIndent()
            )
        }
    }
}
