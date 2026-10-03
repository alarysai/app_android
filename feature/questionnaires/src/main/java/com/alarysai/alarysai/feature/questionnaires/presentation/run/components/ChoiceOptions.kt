package com.alarysai.alarysai.feature.questionnaires.presentation.run.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alarysai.alarysai.core.designsystem.theme.Navy950
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepOptionUi

private val OptionShape = RoundedCornerShape(14.dp)

/**
 * The options of a choice question, laid out by type: side by side for yes/no, a two-column
 * grid of image cards when every option has an image, otherwise a list. Radio marks for single
 * choice, check boxes for multiple choice.
 */
@Composable
fun ChoiceOptions(
    options: List<StepOptionUi>,
    answerType: AnswerType,
    showsAsGrid: Boolean,
    selectedIds: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val multiple = answerType == AnswerType.MULTIPLE_CHOICE
    when {
        answerType == AnswerType.YES_NO -> Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                OptionRow(option, option.id in selectedIds, multiple = false, onToggle, Modifier.weight(1f))
            }
        }
        showsAsGrid -> Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { option ->
                        OptionImageCard(option, option.id in selectedIds, multiple, onToggle, Modifier.weight(1f))
                    }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
        else -> Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                OptionRow(option, option.id in selectedIds, multiple, onToggle, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun OptionRow(
    option: StepOptionUi,
    selected: Boolean,
    multiple: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .optionSurface(selected, OptionShape)
            .selection(selected, multiple) { onToggle(option.id) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SelectionMark(selected, multiple)
        if (option.imageUrl != null) {
            AsyncImage(
                model = option.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
        Text(
            text = option.text.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun OptionImageCard(
    option: StepOptionUi,
    selected: Boolean,
    multiple: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .optionSurface(selected, OptionShape)
            .selection(selected, multiple) { onToggle(option.id) },
    ) {
        AsyncImage(
            model = option.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Text(
            text = option.text.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(12.dp),
        )
    }
}

/** Empty circle / check box, filled with the brand color and a check when selected. */
@Composable
private fun SelectionMark(selected: Boolean, multiple: Boolean) {
    val shape = if (multiple) RoundedCornerShape(6.dp) else CircleShape
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(1.5.dp, if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Navy950, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun Modifier.optionSurface(selected: Boolean, shape: Shape): Modifier {
    val primary = MaterialTheme.colorScheme.primary
    return clip(shape)
        .background(if (selected) primary.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f))
        .border(if (selected) 1.5.dp else 1.dp, if (selected) primary else Color.White.copy(alpha = 0.14f), shape)
}

private fun Modifier.selection(selected: Boolean, multiple: Boolean, onClick: () -> Unit): Modifier =
    if (multiple) {
        toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    } else {
        selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    }
