package com.alarysai.alarysai.feature.questionnaires.presentation.run.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.theme.Violet500
import com.alarysai.alarysai.core.ui.speech.rememberSpeechInput
import com.alarysai.alarysai.feature.questionnaires.R

const val OPEN_TEXT_FIELD_TAG = "open_text_field"

/** Free-text answer with dictation, a character counter and the "optional" hint. */
@Composable
fun OpenTextAnswer(
    text: String,
    maxLength: Int,
    placeholder: String?,
    required: Boolean,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Dictation appends to what is already written.
    val speechInput = rememberSpeechInput { spoken ->
        onTextChange(listOf(text.trimEnd(), spoken).filter { it.isNotEmpty() }.joinToString(" "))
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            minLines = 5,
            placeholder = placeholder?.let { { Text(it) } },
            shape = RoundedCornerShape(16.dp),
            trailingIcon = speechInput.launch?.let { launch ->
                {
                    IconButton(
                        onClick = launch,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Violet500.copy(alpha = 0.25f), CircleShape),
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = stringResource(R.string.run_dictate))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.07f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(OPEN_TEXT_FIELD_TAG),
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!required) {
                Text(
                    text = stringResource(R.string.run_optional_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            Text(
                text = "${text.length}/$maxLength",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
