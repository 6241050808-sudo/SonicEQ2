package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SonicDarkColorScheme = darkColorScheme(
    primary = SonicCyan,
    onPrimary = SonicBackground,
    primaryContainer = SonicSurfaceVariant,
    onPrimaryContainer = SonicCyan,
    secondary = SonicPurple,
    onSecondary = SonicBackground,
    secondaryContainer = SonicSurfaceVariant,
    onSecondaryContainer = SonicPurple,
    tertiary = SonicPink,
    background = SonicBackground,
    onBackground = SonicTextPrimary,
    surface = SonicSurface,
    onSurface = SonicTextPrimary,
    surfaceVariant = SonicSurfaceVariant,
    onSurfaceVariant = SonicTextSecondary,
    outline = SonicSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // SonicEQ is specifically designed as an immersive dark audio studio theme
    MaterialTheme(
        colorScheme = SonicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
