package com.alarysai.alarysai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alarysai.alarysai.R
import com.alarysai.alarysai.core.ui.state.MessageContent

/** Placeholder for tabs whose feature module does not exist yet. */
@Composable
internal fun ComingSoonScreen(title: String) {
    MessageContent(title = title, body = stringResource(R.string.tab_coming_soon))
}
