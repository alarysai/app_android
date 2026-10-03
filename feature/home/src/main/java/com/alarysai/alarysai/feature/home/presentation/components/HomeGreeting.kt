package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.alarysai.alarysai.feature.home.R

/** "Olá, Diego" with the greeting in cyan; just "Olá!" while there is no logged-in user. */
@Composable
fun HomeGreeting(
    userName: String?,
    modifier: Modifier = Modifier,
) {
    val hello = stringResource(R.string.home_greeting_hello)
    val greeting = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(if (userName == null) "$hello!" else "$hello, ")
        }
        if (userName != null) append(userName)
    }
    Column(modifier = modifier) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.home_greeting_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
