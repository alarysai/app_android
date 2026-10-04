package com.alarysai.alarysai.feature.auth.presentation.forgot

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.theme.Violet500
import com.alarysai.alarysai.feature.auth.R
import com.alarysai.alarysai.feature.auth.presentation.components.AuthErrorMessage
import com.alarysai.alarysai.feature.auth.presentation.components.AuthFieldShape
import com.alarysai.alarysai.feature.auth.presentation.components.AuthPrimaryButton
import com.alarysai.alarysai.feature.auth.presentation.components.AuthTextField
import com.alarysai.alarysai.feature.auth.presentation.components.AuthTitle

@Composable
fun ForgotPasswordScreenRoute(
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ForgotPasswordUiEvent.OpenEmailApp -> openEmailApp(context)
                ForgotPasswordUiEvent.BackToLogin -> onBackToLogin()
            }
        }
    }
    ForgotPasswordScreen(uiState = uiState, onAction = viewModel::onAction)
}

/** "Recover password" form, then "Check your e-mail" once the link was requested. */
@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onAction: (ForgotPasswordUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sentTo = uiState.sentTo
    if (sentTo == null) {
        RequestForm(uiState, onAction, modifier)
    } else {
        LinkSent(sentTo, uiState.resendInSeconds, onAction, modifier)
    }
}

@Composable
private fun RequestForm(uiState: ForgotPasswordUiState, onAction: (ForgotPasswordUiAction) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        IconButton(onClick = { onAction(ForgotPasswordUiAction.BackClicked) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.auth_back))
        }
        IconBadge(Icons.Outlined.Key)
        AuthTitle(stringResource(R.string.auth_forgot_title), stringResource(R.string.auth_forgot_subtitle))
        AuthTextField(
            label = stringResource(R.string.auth_email),
            value = uiState.email,
            onValueChange = { onAction(ForgotPasswordUiAction.EmailChanged(it)) },
            placeholder = stringResource(R.string.auth_email_placeholder),
            errorText = if (uiState.showEmailError) stringResource(R.string.auth_error_invalid_email) else null,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Send,
            onImeAction = { onAction(ForgotPasswordUiAction.SendClicked) },
        )
        AuthErrorMessage(uiState.error)
        AuthPrimaryButton(
            text = stringResource(R.string.auth_send_link),
            enabled = uiState.canSend,
            isLoading = uiState.isLoading,
            onClick = { onAction(ForgotPasswordUiAction.SendClicked) },
        )
    }
}

/** Never confirms whether the account exists: "If there is an account for…". */
@Composable
private fun LinkSent(email: String, resendInSeconds: Int, onAction: (ForgotPasswordUiAction) -> Unit, modifier: Modifier) {
    val template = stringResource(R.string.auth_link_sent_body, EMAIL_MARK)
    val (before, after) = template.split(EMAIL_MARK).let { it.first() to it.getOrElse(1) { "" } }
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        IconBadge(Icons.Outlined.MarkEmailRead)
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.auth_link_sent_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            text = buildAnnotatedString {
                append(before)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) { append(email) }
                append(after)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { onAction(ForgotPasswordUiAction.OpenEmailAppClicked) },
            shape = AuthFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.auth_open_email_app), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
        }
        TextButton(onClick = { onAction(ForgotPasswordUiAction.ResendClicked) }, enabled = resendInSeconds == 0) {
            Text(
                text = if (resendInSeconds > 0) {
                    stringResource(R.string.auth_resend_in, resendInSeconds / 60, resendInSeconds % 60)
                } else {
                    stringResource(R.string.auth_resend)
                },
            )
        }
        TextButton(onClick = { onAction(ForgotPasswordUiAction.BackClicked) }) {
            Text(stringResource(R.string.auth_back_to_login), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(Violet500.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
    }
}

private const val EMAIL_MARK = "§EMAIL§"

/** Opens the user's e-mail app on its inbox; does nothing when there is none. */
private fun openEmailApp(context: Context) {
    val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No e-mail app installed: the user opens it on their own.
    }
}
