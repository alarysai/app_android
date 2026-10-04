package com.alarysai.alarysai.feature.auth.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
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
import com.alarysai.alarysai.feature.auth.domain.usecase.ValidateSignUpUseCase
import com.alarysai.alarysai.feature.auth.presentation.components.AuthErrorMessage
import com.alarysai.alarysai.feature.auth.presentation.components.AuthFieldShape
import com.alarysai.alarysai.feature.auth.presentation.components.AuthPrimaryButton
import com.alarysai.alarysai.feature.auth.presentation.components.AuthTextField
import com.alarysai.alarysai.feature.auth.presentation.components.AuthTitle
import com.alarysai.alarysai.feature.auth.presentation.components.PasswordField

@Composable
fun SignUpScreenRoute(
    onBackToLogin: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SignUpUiEvent.BackToLogin -> onBackToLogin()
            }
        }
    }
    SignUpScreen(uiState = uiState, onAction = viewModel::onAction)
}

/**
 * "Create account": name, e-mail, password twice and the LGPD acceptance. The marketing opt-in
 * of the mockup is left out until `users/{uid}` can store it (see docs/proposta-usuarios-login.md).
 */
@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    onAction: (SignUpUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val errors = uiState.errors
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = { onAction(SignUpUiAction.BackClicked) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.auth_back))
            }
            AuthTitle(stringResource(R.string.auth_signup_title), stringResource(R.string.auth_signup_subtitle))
            AuthTextField(
                label = stringResource(R.string.auth_name),
                value = uiState.name,
                onValueChange = { onAction(SignUpUiAction.NameChanged(it)) },
                placeholder = stringResource(R.string.auth_name_placeholder),
                errorText = if (errors.nameMissing) stringResource(R.string.auth_error_name_missing) else null,
            )
            AuthTextField(
                label = stringResource(R.string.auth_email),
                value = uiState.email,
                onValueChange = { onAction(SignUpUiAction.EmailChanged(it)) },
                placeholder = stringResource(R.string.auth_email_placeholder),
                errorText = if (errors.emailInvalid) stringResource(R.string.auth_error_invalid_email) else null,
                keyboardType = KeyboardType.Email,
            )
            PasswordField(
                label = stringResource(R.string.auth_password),
                value = uiState.password,
                onValueChange = { onAction(SignUpUiAction.PasswordChanged(it)) },
                placeholder = stringResource(R.string.auth_password_rule, ValidateSignUpUseCase.MIN_PASSWORD_LENGTH),
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = { onAction(SignUpUiAction.TogglePasswordVisibility) },
                errorText = if (errors.passwordTooShort) {
                    stringResource(R.string.auth_password_rule, ValidateSignUpUseCase.MIN_PASSWORD_LENGTH)
                } else {
                    null
                },
            )
            PasswordField(
                label = stringResource(R.string.auth_confirm_password),
                value = uiState.confirmation,
                onValueChange = { onAction(SignUpUiAction.ConfirmationChanged(it)) },
                placeholder = stringResource(R.string.auth_confirm_password_placeholder),
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = { onAction(SignUpUiAction.TogglePasswordVisibility) },
                errorText = if (errors.passwordsDiffer) stringResource(R.string.auth_error_passwords_differ) else null,
                imeAction = ImeAction.Done,
            )
            TermsBox(accepted = uiState.acceptedTerms, onChange = { onAction(SignUpUiAction.TermsChanged(it)) })
            AuthErrorMessage(uiState.error)
        }
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AuthPrimaryButton(
                text = stringResource(R.string.auth_create_account),
                enabled = uiState.canSubmit,
                isLoading = uiState.isLoading,
                onClick = { onAction(SignUpUiAction.CreateAccountClicked) },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.auth_have_account), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { onAction(SignUpUiAction.BackClicked) }) {
                    Text(stringResource(R.string.auth_sign_in), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * LGPD acceptance. The policy and terms are plain text until their pages exist; the links go
 * here when they do.
 */
@Composable
private fun TermsBox(accepted: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), AuthFieldShape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), AuthFieldShape)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = accepted, onCheckedChange = onChange)
        Text(stringResource(R.string.auth_terms), style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(heightDp = 820)
@Composable
private fun SignUpScreenPreview() {
    AlarysTheme { AlarysBackground { SignUpScreen(SignUpUiState(name = "Marina"), onAction = {}) } }
}
