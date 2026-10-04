package com.alarysai.alarysai.feature.auth.presentation.signup

import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import com.alarysai.alarysai.feature.auth.domain.usecase.SignUpErrors

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val isPasswordVisible: Boolean = false,
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    /** Field errors, shown only after the first attempt to create the account. */
    val errors: SignUpErrors = SignUpErrors(),
    val error: AuthError? = null,
) {
    /** The button stays grey until everything is filled and the terms are accepted (mockup). */
    val canSubmit: Boolean
        get() = name.isNotBlank() && email.isNotBlank() && password.isNotEmpty() && confirmation.isNotEmpty() &&
            acceptedTerms && !isLoading
}

sealed interface SignUpUiAction {
    data class NameChanged(val name: String) : SignUpUiAction
    data class EmailChanged(val email: String) : SignUpUiAction
    data class PasswordChanged(val password: String) : SignUpUiAction
    data class ConfirmationChanged(val confirmation: String) : SignUpUiAction
    data object TogglePasswordVisibility : SignUpUiAction
    data class TermsChanged(val accepted: Boolean) : SignUpUiAction
    data object CreateAccountClicked : SignUpUiAction
    data object BackClicked : SignUpUiAction
}

sealed interface SignUpUiEvent {
    data object BackToLogin : SignUpUiEvent
}
