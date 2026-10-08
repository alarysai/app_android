package com.alarysai.alarysai.core.ui.state

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.ui.R

/** Error state for any content screen, with "Try again". */
@Composable
fun ContentLoadErrorContent(
    error: ContentLoadError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageContent(
        title = stringResource(error.titleRes()),
        body = stringResource(error.bodyRes()),
        actionLabel = stringResource(R.string.core_ui_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

/** One-line error with "Try again", for a section that shares the screen with other content. */
@Composable
fun ContentLoadErrorBanner(
    error: ContentLoadError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(error.titleRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) { Text(stringResource(R.string.core_ui_retry)) }
    }
}

@StringRes
private fun ContentLoadError.titleRes(): Int = when (this) {
    ContentLoadError.OFFLINE -> R.string.core_ui_error_offline_title
    ContentLoadError.UNAVAILABLE -> R.string.core_ui_error_unavailable_title
    ContentLoadError.UNKNOWN -> R.string.core_ui_error_unknown_title
}

@StringRes
private fun ContentLoadError.bodyRes(): Int = when (this) {
    ContentLoadError.OFFLINE -> R.string.core_ui_error_offline_body
    ContentLoadError.UNAVAILABLE -> R.string.core_ui_error_unavailable_body
    ContentLoadError.UNKNOWN -> R.string.core_ui_error_unknown_body
}
