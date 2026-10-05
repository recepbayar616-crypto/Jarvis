package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = VoidBlack,
    primaryContainer = ArcCyanDark,
    onPrimaryContainer = TextWhite,
    secondary = SkyTech,
    onSecondary = VoidBlack,
    tertiary = CriminalAmber,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = TextWhite,
    surface = CyberSurface,
    onSurface = TextWhite,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = CyberCardBorder,
    error = CrimsonAlert,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
