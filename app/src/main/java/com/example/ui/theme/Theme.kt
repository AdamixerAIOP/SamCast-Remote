package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ObsidianRemoteColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = StudioBackground,
    primaryContainer = ButtonSurface,
    onPrimaryContainer = TextPrimary,
    secondary = VoiceEmerald,
    onSecondary = StudioBackground,
    secondaryContainer = ButtonSurfacePressed,
    onSecondaryContainer = TextSecondary,
    tertiary = PowerCrimson,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = RemoteChassisBottom,
    onSurface = TextPrimary,
    surfaceVariant = ButtonSurface,
    onSurfaceVariant = TextSecondary,
    outline = RemoteChassisBorder,
    error = PowerCrimson,
    onError = TextPrimary
)

@Composable
fun SamCastRemoteTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianRemoteColorScheme,
        typography = AppTypography,
        content = content
    )
}
