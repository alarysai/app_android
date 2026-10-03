package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.Navy800
import com.alarysai.alarysai.core.ui.speech.rememberSpeechInput
import com.alarysai.alarysai.feature.home.R

const val CHAT_SEND_TAG = "chat_send"

/** Entry point for the Alarys chat. Sending is a "coming soon" until the generation service exists. */
@Composable
fun ChatPromptCard(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val speechInput = rememberSpeechInput(onResult = onMessageChange)
    GlassCard(modifier = modifier.fillMaxWidth(), accent = Navy800) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_chat_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            GlassTextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = stringResource(R.string.home_chat_placeholder),
                imeAction = ImeAction.Send,
                onImeAction = onSendClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = speechInput.launch?.let { launch ->
                    {
                        IconButton(onClick = launch) {
                            Icon(Icons.Outlined.Mic, contentDescription = stringResource(R.string.home_chat_voice))
                        }
                    }
                },
                trailingIcon = {
                    IconButton(onClick = onSendClick, modifier = Modifier.testTag(CHAT_SEND_TAG)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.home_chat_send),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }
    }
}
