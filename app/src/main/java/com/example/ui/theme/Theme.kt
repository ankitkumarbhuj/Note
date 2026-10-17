package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1B00),
    primaryContainer = Color(0xFF382E00),
    onPrimaryContainer = GoldLight,
    secondary = CyanAccent,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF004F5C),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = IndigoBusiness,
    onTertiary = Color.White,
    background = ObsidianDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = RosePrivate,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF0B8),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF0E7490),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBAE6FD),
    onSecondaryContainer = Color(0xFF002022),
    tertiary = Color(0xFF4F46E5),
    onTertiary = Color.White,
    background = ObsidianLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = RosePrivate,
    onError = Color.White
)

@Composable
fun PrivaNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// For compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = PrivaNoteTheme(darkTheme = darkTheme, content = content)
