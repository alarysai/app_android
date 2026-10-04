package com.alarysai.alarysai.feature.auth.presentation.login

import com.alarysai.alarysai.feature.auth.domain.model.AuthError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val keepSignedIn: Boolean = true,
    val isLoading: Boolean = false,
    /** Shown after an attempt with a malformed e-mail. */
    val showEmailError: Boolean = false,
    val error: AuthError? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotEmpty() && !isLoading
}

sealed interface LoginUiAction {
    data class EmailChanged(val email: String) : LoginUiAction
    data class PasswordChanged(val password: String) : LoginUiAction
    data object TogglePasswordVisibility : LoginUiAction
    data class KeepSignedInChanged(val keep: Boolean) : LoginUiAction
    data object SignInClicked : LoginUiAction

    /** Google returned an ID token through Credential Manager. */
    data class GoogleTokenReceived(val idToken: String) : LoginUiAction

    /** Credential Manager failed; null when the user just closed the account picker. */
    data class GoogleFailed(val error: AuthError?) : LoginUiAction
    data object SignUpClicked : LoginUiAction
    data object ForgotPasswordClicked : LoginUiAction
}

sealed interface LoginUiEvent {
    data object OpenSignUp : LoginUiEvent
    data object OpenForgotPassword : LoginUiEvent
}
