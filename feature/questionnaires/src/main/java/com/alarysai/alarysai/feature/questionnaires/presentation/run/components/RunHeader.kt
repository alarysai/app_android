package com.alarysai.alarysai.feature.questionnaires.presentation.run.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.feature.questionnaires.R
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.RunProgressUi

/** X to leave, the questionnaire title and "Skip" on optional questions. */
@Composable
fun RunTopBar(
    title: String,
    canSkip: Boolean,
    onClose: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.run_close))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 72.dp),
        )
        if (canSkip) {
            TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterEnd)) {
                Text(stringResource(R.string.run_skip), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * One segment per question on the path: answered ones full, the current one half-lit.
 * Below it, "Question N of M" and the percentage.
 */
@Composable
fun RunProgressBar(
    progress: RunProgressUi,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.run_progress, progress.number, progress.total)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(progress.total) { index ->
                val color = when {
                    index < progress.number - 1 -> MaterialTheme.colorScheme.primary
                    index == progress.number - 1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    else -> Color.White.copy(alpha = 0.12f)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(color, RoundedCornerShape(2.dp)),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(description, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.run_percent, progress.percent),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
