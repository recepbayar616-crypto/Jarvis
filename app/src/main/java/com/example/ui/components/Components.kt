package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.CriminalAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.GreenOnline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.voice.AssistantVoiceState
import kotlin.math.sin

@Composable
fun AudioEqualizerWave(
    voiceState: AssistantVoiceState,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 18
    val transition = rememberInfiniteTransition(label = "eq_bars")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(36.dp)
    ) {
        for (i in 0 until barCount) {
            val isActive = voiceState == AssistantVoiceState.LISTENING || voiceState == AssistantVoiceState.SPEAKING
            val baseSine = (sin(phase + (i * 0.45f)) + 1f) / 2f
            val dynamicHeight = if (isActive) {
                (10.dp + (24.dp * audioRms) + (12.dp * baseSine)).coerceIn(6.dp, 36.dp)
            } else {
                (6.dp + (4.dp * baseSine)).coerceIn(4.dp, 10.dp)
            }

            val barColor = when {
                voiceState == AssistantVoiceState.LISTENING -> ArcCyan
                voiceState == AssistantVoiceState.SPEAKING -> ArcCyan
                voiceState == AssistantVoiceState.THINKING -> CriminalAmber
                else -> ArcCyan.copy(alpha = 0.35f)
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(dynamicHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun StatusBadge(
    isLiveApi: Boolean,
    channelConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (channelConnected) GreenOnline else CriminalAmber)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isLiveApi && channelConnected) "CANLI YOUTUBE API" else if (channelConnected) "VAKA BELGESELİ (DEMO/ÖNBELLEK)" else "BAĞLANTI BEKLENİYOR",
            color = if (channelConnected) TextWhite else CriminalAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun QuickVoiceCommandChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.8f))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            color = TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
