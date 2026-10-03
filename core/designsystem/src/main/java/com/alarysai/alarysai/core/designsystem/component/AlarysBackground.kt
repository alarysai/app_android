package com.alarysai.alarysai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.alarysai.alarysai.core.designsystem.theme.Blue500
import com.alarysai.alarysai.core.designsystem.theme.Navy900
import com.alarysai.alarysai.core.designsystem.theme.Navy950
import com.alarysai.alarysai.core.designsystem.theme.Violet500

/** Dark navy with soft blue and violet glows, drawn behind every screen. */
@Composable
fun AlarysBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Navy900, Navy950)))
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Blue500.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.08f),
                        radius = size.width * 0.8f,
                    ),
                    radius = size.width * 0.8f,
                    center = Offset(size.width * 0.5f, size.height * 0.08f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Violet500.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width, size.height * 0.35f),
                        radius = size.width * 0.7f,
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width, size.height * 0.35f),
                )
            },
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            content()
        }
    }
}
