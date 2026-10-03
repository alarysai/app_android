package com.alarysai.alarysai.core.ui.state

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
