package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.ui.speech.rememberSpeechInput
import com.alarysai.alarysai.feature.home.R

const val HOME_SEARCH_TAG = "home_search"

/** Filters the category cards. The mic fills the query by voice when the device has a recognizer. */
@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val speechInput = rememberSpeechInput(onResult = onQueryChange)
    GlassTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.home_search_placeholder),
        imeAction = ImeAction.Search,
        onImeAction = { focusManager.clearFocus() },
        modifier = modifier
            .fillMaxWidth()
            .testTag(HOME_SEARCH_TAG),
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = speechInput.launch?.let { launch ->
            {
                MicButton(
                    contentDescription = stringResource(R.string.home_search_voice),
                    onClick = launch,
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
        },
    )
}
