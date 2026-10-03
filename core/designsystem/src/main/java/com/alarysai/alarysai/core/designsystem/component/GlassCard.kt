package com.alarysai.alarysai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.theme.Cyan400

/**
 * Translucent card with a glowing border tinted by [accent].
 * Clickable only when [onClick] is set.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color = Cyan400,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(accent.copy(alpha = 0.8f), accent.copy(alpha = 0.2f))),
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        // The card has no Surface behind it, so text would fall back to black without this.
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}
