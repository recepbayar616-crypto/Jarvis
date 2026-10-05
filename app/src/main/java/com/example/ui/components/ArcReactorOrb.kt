package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.CriminalAmber
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.SkyTech
import com.example.voice.AssistantVoiceState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorOrb(
    state: AssistantVoiceState,
    audioRms: Float,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_infinite")

    // Slow rotation for ambient futuristic rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantVoiceState.THINKING) 3000 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Reverse rotation for counter-ring
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantVoiceState.THINKING) 2000 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Breathing pulse for core glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantVoiceState.LISTENING) 700 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val waveAmplitude = remember { Animatable(0f) }
    LaunchedEffect(audioRms) {
        waveAmplitude.animateTo(
            targetValue = audioRms.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 60)
        )
    }

    val primaryGlowColor = when (state) {
        AssistantVoiceState.LISTENING -> ArcCyan
        AssistantVoiceState.THINKING -> SkyTech
        AssistantVoiceState.SPEAKING -> ArcCyan
        AssistantVoiceState.ERROR -> CrimsonAlert
        AssistantVoiceState.IDLE -> ArcCyan
    }

    val secondaryGlowColor = when (state) {
        AssistantVoiceState.LISTENING -> SkyTech
        AssistantVoiceState.THINKING -> CriminalAmber
        AssistantVoiceState.SPEAKING -> SkyTech
        AssistantVoiceState.ERROR -> CrimsonAlert
        AssistantVoiceState.IDLE -> SkyTech.copy(alpha = 0.5f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f

            // Dynamic reactive outer halo
            val dynamicRadius = baseRadius * (if (state == AssistantVoiceState.LISTENING) 0.88f + (waveAmplitude.value * 0.12f) else pulseScale * 0.9f)

            // 1. Soft Ambient Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlowColor.copy(alpha = if (state == AssistantVoiceState.IDLE) 0.25f else 0.45f),
                        secondaryGlowColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius
                ),
                center = center,
                radius = dynamicRadius
            )

            // 2. Outer segmented tech ring
            val outerRingRadius = baseRadius * 0.84f
            val segments = 24
            for (i in 0 until segments) {
                val startAngle = (i * (360f / segments)) + rotationAngle
                val isTickLong = i % 4 == 0
                val tickLength = if (isTickLong) 14f else 8f
                val rad = (startAngle * PI / 180f).toFloat()

                val p1 = Offset(
                    center.x + (outerRingRadius - tickLength) * cos(rad),
                    center.y + (outerRingRadius - tickLength) * sin(rad)
                )
                val p2 = Offset(
                    center.x + outerRingRadius * cos(rad),
                    center.y + outerRingRadius * sin(rad)
                )

                drawLine(
                    color = primaryGlowColor.copy(alpha = if (isTickLong) 0.8f else 0.35f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (isTickLong) 2.5f else 1.5f,
                    cap = StrokeCap.Round
                )
            }

            // 3. Middle high-tech counter-rotating arcs
            val midRadius = baseRadius * 0.65f
            drawArc(
                color = primaryGlowColor.copy(alpha = 0.7f),
                startAngle = counterRotation,
                sweepAngle = 70f,
                useCenter = false,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round),
                topLeft = Offset(center.x - midRadius, center.y - midRadius),
                size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2)
            )
            drawArc(
                color = secondaryGlowColor.copy(alpha = 0.7f),
                startAngle = counterRotation + 120f,
                sweepAngle = 60f,
                useCenter = false,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round),
                topLeft = Offset(center.x - midRadius, center.y - midRadius),
                size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2)
            )
            drawArc(
                color = primaryGlowColor.copy(alpha = 0.5f),
                startAngle = counterRotation + 240f,
                sweepAngle = 80f,
                useCenter = false,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round),
                topLeft = Offset(center.x - midRadius, center.y - midRadius),
                size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2)
            )

            // 4. Inner glowing nucleus core
            val coreRadius = baseRadius * 0.35f * (if (state == AssistantVoiceState.SPEAKING) 1f + (sin(rotationAngle * 0.1f) * 0.08f) else 1f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryGlowColor,
                        secondaryGlowColor.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                center = center,
                radius = coreRadius
            )

            // Inner crisp center border
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                center = center,
                radius = coreRadius * 0.45f,
                style = Stroke(width = 2f)
            )

            // Audio reactive resonance waves if listening or speaking
            if (state == AssistantVoiceState.LISTENING || state == AssistantVoiceState.SPEAKING) {
                val waveCount = 3
                for (w in 1..waveCount) {
                    val r = coreRadius + (baseRadius * 0.28f * w / waveCount) + (waveAmplitude.value * 16f)
                    drawCircle(
                        color = primaryGlowColor.copy(alpha = (0.4f / w).coerceIn(0.1f, 0.5f)),
                        center = center,
                        radius = r,
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }
}
