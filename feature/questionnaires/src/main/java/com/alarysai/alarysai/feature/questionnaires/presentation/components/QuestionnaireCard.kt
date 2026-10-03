package com.alarysai.alarysai.feature.questionnaires.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.designsystem.theme.CardAccents
import com.alarysai.alarysai.feature.questionnaires.R
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnaireItemUi

/** One questionnaire in the list; tapping opens it. */
@Composable
fun QuestionnaireCard(
    questionnaire: QuestionnaireItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = CardAccents[questionnaire.accentIndex.mod(CardAccents.size)]
    GlassCard(modifier = modifier.fillMaxWidth(), accent = accent, onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuestionnaireImage(questionnaire = questionnaire, accent = accent)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = questionnaire.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (questionnaire.description != null) {
                    Text(
                        text = questionnaire.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!questionnaire.isInUserLanguage) PortugueseOnlyBadge()
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Images are always null until uploads are enabled; the initial letter stands in for them. */
@Composable
private fun QuestionnaireImage(questionnaire: QuestionnaireItemUi, accent: Color) {
    val shape = RoundedCornerShape(12.dp)
    val imageModifier = Modifier
        .size(56.dp)
        .clip(shape)
    if (questionnaire.imageUrl != null) {
        AsyncImage(
            model = questionnaire.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = imageModifier,
        )
    } else {
        Box(
            modifier = imageModifier
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.4f))))
                .border(1.dp, accent, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = questionnaire.title.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun PortugueseOnlyBadge() {
    Text(
        text = stringResource(R.string.questionnaires_portuguese_only),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 2.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Preview
@Composable
private fun QuestionnaireCardPreview() {
    AlarysTheme {
        AlarysBackground {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuestionnaireCard(
                    QuestionnaireItemUi("1", "Ética na IA", "Descubra como usar IA com responsabilidade.", null, true, 0),
                    onClick = {},
                )
                QuestionnaireCard(QuestionnaireItemUi("2", "Post para redes", null, null, false, 1), onClick = {})
            }
        }
    }
}
