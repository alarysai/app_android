package com.alarysai.alarysai.feature.questionnaires.presentation.run.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.Violet500
import com.alarysai.alarysai.feature.questionnaires.R
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunContent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewAnswerUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewItemUi

/** "Review your request": every answer (tap to edit) and the prompt preview. */
@Composable
fun ReviewContent(
    review: QuestionnaireRunContent.Review,
    onEditAnswer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.run_review_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.run_review_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                review.items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ReviewRow(item, onClick = { onEditAnswer(item.stepId) })
                }
            }
        }
        review.promptPreview?.let { PromptPreview(it) }
    }
}

@Composable
private fun ReviewRow(item: ReviewItemUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item.question?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = when (val answer = item.answer) {
                    is ReviewAnswerUi.Text -> answer.value
                    ReviewAnswerUi.VideoWatched -> stringResource(R.string.run_review_video_watched)
                    ReviewAnswerUi.Skipped -> stringResource(R.string.run_review_skipped)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        PromptChip(item.isPartOfPrompt)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** "In the prompt" (violet) or "Context only" (grey), from the step's "part of the prompt?". */
@Composable
private fun PromptChip(isPartOfPrompt: Boolean) {
    val color = if (isPartOfPrompt) Violet500 else Color.White
    Text(
        text = stringResource(if (isPartOfPrompt) R.string.run_chip_in_prompt else R.string.run_chip_context_only),
        style = MaterialTheme.typography.labelSmall,
        color = if (isPartOfPrompt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun PromptPreview(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.run_prompt_preview_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(14.dp),
        )
        Text(
            stringResource(R.string.run_prompt_preview_note),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
