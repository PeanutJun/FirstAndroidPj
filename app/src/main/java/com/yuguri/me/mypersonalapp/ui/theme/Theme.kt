package com.yuguri.me.mypersonalapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0B1220)
val DarkSurface = Color(0xFF1E293B)
val CyanAccent = Color(0xFF22D3EE)
val OrangeAccent = Color(0xFFF97316)
val BlueAccent = Color(0xFF38BDF8)
val GreenAccent = Color(0xFF22C55E)
val Slate400 = Color(0xFF94A3B8)
val Slate50 = Color(0xFFF8FAFC)
val RedError = Color(0xFFF87171)
val TranslucentWhite = Color(0x0FFFFFFF)
val TranslucentCard = Color(0x14FFFFFF)

private val AppColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = BlueAccent,
    tertiary = OrangeAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Slate50,
    onSurface = Slate50,
    surfaceVariant = TranslucentWhite,
    error = RedError,
    onSurfaceVariant = Slate400
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}

