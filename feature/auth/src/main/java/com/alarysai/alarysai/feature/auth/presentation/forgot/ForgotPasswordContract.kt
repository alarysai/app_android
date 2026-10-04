package com.alarysai.alarysai.feature.auth.presentation.forgot

import com.alarysai.alarysai.feature.auth.domain.model.AuthError

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val showEmailError: Boolean = false,
    val error: AuthError? = null,
    /** Set after the link was requested: the "Check your e-mail" screen shows. */
    val sentTo: String? = null,
    /** Seconds until "Resend" is allowed again; 0 = allowed. */
    val resendInSeconds: Int = 0,
) {
    val canSend: Boolean get() = email.isNotBlank() && !isLoading
}

sealed interface ForgotPasswordUiAction {
    data class EmailChanged(val email: String) : ForgotPasswordUiAction
    data object SendClicked : ForgotPasswordUiAction
    data object ResendClicked : ForgotPasswordUiAction
    data object OpenEmailAppClicked : ForgotPasswordUiAction
    data object BackClicked : ForgotPasswordUiAction
}

sealed interface ForgotPasswordUiEvent {
    data object OpenEmailApp : ForgotPasswordUiEvent
    data object BackToLogin : ForgotPasswordUiEvent
}
