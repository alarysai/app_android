package com.alarysai.alarysai.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.auth.R
import com.alarysai.alarysai.feature.auth.presentation.components.AuthBrand
import com.alarysai.alarysai.feature.auth.presentation.components.AuthErrorMessage
import com.alarysai.alarysai.feature.auth.presentation.components.AuthPrimaryButton
import com.alarysai.alarysai.feature.auth.presentation.components.AuthTextField
import com.alarysai.alarysai.feature.auth.presentation.components.AuthTitle
import com.alarysai.alarysai.feature.auth.presentation.components.GoogleButton
import com.alarysai.alarysai.feature.auth.presentation.components.OrDivider
import com.alarysai.alarysai.feature.auth.presentation.components.PasswordField

const val LOGIN_EMAIL_TAG = "login_email"
const val LOGIN_PASSWORD_TAG = "login_password"

@Composable
fun LoginScreenRoute(
    onOpenSignUp: () -> Unit,
    onOpenForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginUiEvent.OpenSignUp -> onOpenSignUp()
                LoginUiEvent.OpenForgotPassword -> onOpenForgotPassword()
            }
        }
    }
    val signInWithGoogle = rememberGoogleSignIn(
        webClientId = viewModel.googleWebClientId,
        onToken = { viewModel.onAction(LoginUiAction.GoogleTokenReceived(it)) },
        onFailure = { viewModel.onAction(LoginUiAction.GoogleFailed(it)) },
    )
    LoginScreen(uiState = uiState, onAction = viewModel::onAction, onGoogleClick = signInWithGoogle)
}

/** "Welcome back": e-mail and password, keep me signed in, forgot password, Google, and sign-up. */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onAction: (LoginUiAction) -> Unit,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        AuthBrand()
        Spacer(Modifier.height(12.dp))
        AuthTitle(stringResource(R.string.auth_login_title), stringResource(R.string.auth_login_subtitle))
        AuthTextField(
            label = stringResource(R.string.auth_email),
            value = uiState.email,
            onValueChange = { onAction(LoginUiAction.EmailChanged(it)) },
            placeholder = stringResource(R.string.auth_email_placeholder),
            errorText = if (uiState.showEmailError) stringResource(R.string.auth_error_invalid_email) else null,
            keyboardType = KeyboardType.Email,
            testTag = LOGIN_EMAIL_TAG,
        )
        PasswordField(
            label = stringResource(R.string.auth_password),
            value = uiState.password,
            onValueChange = { onAction(LoginUiAction.PasswordChanged(it)) },
            placeholder = stringResource(R.string.auth_password_placeholder),
            isVisible = uiState.isPasswordVisible,
            onToggleVisibility = { onAction(LoginUiAction.TogglePasswordVisibility) },
            imeAction = ImeAction.Done,
            onImeAction = { onAction(LoginUiAction.SignInClicked) },
            testTag = LOGIN_PASSWORD_TAG,
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = uiState.keepSignedIn, onCheckedChange = { onAction(LoginUiAction.KeepSignedInChanged(it)) })
            Text(stringResource(R.string.auth_keep_signed_in), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { onAction(LoginUiAction.ForgotPasswordClicked) }) {
                Text(stringResource(R.string.auth_forgot_password), fontWeight = FontWeight.SemiBold)
            }
        }
        AuthErrorMessage(uiState.error)
        AuthPrimaryButton(
            text = stringResource(R.string.auth_sign_in),
            enabled = uiState.canSubmit,
            isLoading = uiState.isLoading,
            onClick = { onAction(LoginUiAction.SignInClicked) },
        )
        OrDivider()
        GoogleButton(onClick = onGoogleClick, enabled = !uiState.isLoading)
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.auth_no_account), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { onAction(LoginUiAction.SignUpClicked) }) {
                Text(stringResource(R.string.auth_create_account), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(heightDp = 760)
@Composable
private fun LoginScreenPreview() {
    AlarysTheme { AlarysBackground { LoginScreen(LoginUiState(email = "voce@email.com"), onAction = {}, onGoogleClick = {}) } }
}
