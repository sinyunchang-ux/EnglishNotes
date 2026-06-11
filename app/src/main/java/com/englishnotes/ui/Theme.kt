package com.englishnotes.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryBlue = Color(0xFF1A73E8)
val PrimaryBlueDark = Color(0xFF1557B0)
val GreenRecorded = Color(0xFF34A853)
val GreenLight = Color(0xFFE6F4EA)
val RedRecording = Color(0xFFEA4335)
val SurfaceGray = Color(0xFFF8F9FA)
val DividerGray = Color(0xFFE0E0E0)
val TextPrimary = Color(0xFF202124)
val TextSecondary = Color(0xFF5F6368)
val HeaderBg = Color(0xFF1A73E8)
val HeaderText = Color.White

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    secondary = GreenRecorded,
    onSecondary = Color.White,
    background = Color.White,
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun EnglishNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
