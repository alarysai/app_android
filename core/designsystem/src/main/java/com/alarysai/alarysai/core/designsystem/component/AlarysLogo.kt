package com.alarysai.alarysai.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.designsystem.theme.Blue500
import com.alarysai.alarysai.core.designsystem.theme.Cyan400
import com.alarysai.alarysai.core.designsystem.theme.Violet500

/**
 * Stand-in for the "A" ribbon mark plus the ALARYS AI wordmark, drawn in code.
 * Replace the mark with the official vector asset once it is available.
 */
@Composable
fun AlarysLogo(
    modifier: Modifier = Modifier,
    markSize: Dp = 72.dp,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AlarysMark(Modifier.size(markSize))
        Text(
            text = "ALARYS AI",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            letterSpacing = 6.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun AlarysMark(modifier: Modifier) {
    Canvas(modifier) {
        val strokeWidth = size.minDimension * 0.16f
        val inset = strokeWidth / 2
        val path = Path().apply {
            moveTo(inset, size.height - inset)
            lineTo(size.width / 2, inset)
            lineTo(size.width - inset, size.height - inset)
            moveTo(size.width * 0.3f, size.height * 0.68f)
            lineTo(size.width * 0.7f, size.height * 0.68f)
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(Cyan400, Blue500, Violet500),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Preview
@Composable
private fun AlarysLogoPreview() {
    AlarysTheme { AlarysBackground { AlarysLogo() } }
}
