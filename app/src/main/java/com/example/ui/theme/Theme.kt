package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DeepViolet,
    secondary = HotPinkAccent,
    tertiary = MarineBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color(0xFF0F0B1E),
    onSecondary = Color(0xFF0F0B1E),
    onTertiary = Color(0xFF0F0B1E),
    onBackground = Color(0xFFECE5F9),
    onSurface = Color(0xFFFAF7FD)
)

private val LightColorScheme = lightColorScheme(
    primary = SeedPurple,
    secondary = SeedPink,
    tertiary = SkyAccent,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1E0E35),
    onSurface = Color(0xFF1E0E35)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
