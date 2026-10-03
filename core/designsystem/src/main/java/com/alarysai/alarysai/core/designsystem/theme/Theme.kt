package com.alarysai.alarysai.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** The brand is dark only, as in the mockups; the system light/dark setting is ignored. */
private val AlarysColors = darkColorScheme(
    primary = Cyan400,
    onPrimary = Navy950,
    primaryContainer = Blue500,
    onPrimaryContainer = TextPrimary,
    secondary = Violet500,
    tertiary = Teal400,
    background = Navy950,
    onBackground = TextPrimary,
    surface = Navy900,
    onSurface = TextPrimary,
    surfaceVariant = Navy800,
    onSurfaceVariant = TextSecondary,
)

@Composable
fun AlarysTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlarysColors,
        content = content,
    )
}
