package com.alarysai.alarysai.feature.questionnaires.presentation.run.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alarysai.alarysai.core.designsystem.theme.Violet500
import com.alarysai.alarysai.feature.questionnaires.R
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepUi

/** Step illustration (when there is one), the answer-type badge, the title and the help text. */
@Composable
fun StepHeader(
    step: StepUi,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (step.imageUrl != null) {
            AsyncImage(
                model = step.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
        AnswerTypeBadge(step)
        step.title?.let { title ->
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        step.helpText?.let { help ->
            Text(text = help, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AnswerTypeBadge(step: StepUi) {
    val (icon, labelRes) = step.badge()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Violet500.copy(alpha = 0.18f))
            .border(1.dp, Violet500.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}

private fun StepUi.badge(): Pair<ImageVector, Int> = when (this) {
    is StepUi.Video -> Icons.Outlined.PlayCircle to R.string.run_badge_video
    is StepUi.Question -> when (answerType) {
        AnswerType.SINGLE_CHOICE -> Icons.Outlined.CheckCircle to R.string.run_badge_single
        AnswerType.MULTIPLE_CHOICE -> Icons.Outlined.CheckBox to R.string.run_badge_multiple
        AnswerType.OPEN_TEXT -> Icons.Outlined.Edit to R.string.run_badge_open_text
        AnswerType.YES_NO -> Icons.Outlined.ToggleOn to R.string.run_badge_yes_no
    }
}
