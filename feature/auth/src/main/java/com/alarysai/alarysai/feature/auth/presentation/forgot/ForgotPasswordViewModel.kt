package com.alarysai.alarysai.feature.auth.presentation.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.feature.auth.domain.model.AuthException
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.alarysai.alarysai.feature.auth.domain.usecase.EmailValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sends the Firebase password-reset link. The confirmation never says whether the account exists
 * ("If there is an account for…"), and resending waits [RESEND_COOLDOWN_SECONDS].
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordUiEvent>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<ForgotPasswordUiEvent> = _events.asSharedFlow()

    private var countdown: Job? = null

    fun onAction(action: ForgotPasswordUiAction) {
        when (action) {
            is ForgotPasswordUiAction.EmailChanged -> _uiState.update { it.copy(email = action.email, showEmailError = false, error = null) }
            ForgotPasswordUiAction.SendClicked -> send()
            ForgotPasswordUiAction.ResendClicked -> if (_uiState.value.resendInSeconds == 0) send()
            ForgotPasswordUiAction.OpenEmailAppClicked -> _events.tryEmit(ForgotPasswordUiEvent.OpenEmailApp)
            ForgotPasswordUiAction.BackClicked -> _events.tryEmit(ForgotPasswordUiEvent.BackToLogin)
        }
    }

    private fun send() {
        val state = _uiState.value
        if (!state.canSend) return
        if (!EmailValidator.isValid(state.email)) {
            _uiState.update { it.copy(showEmailError = true) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.sendPasswordReset(state.email)
                _uiState.update { it.copy(isLoading = false, sentTo = state.email.trim()) }
                startCountdown()
            } catch (failure: AuthException) {
                _uiState.update { it.copy(isLoading = false, error = failure.error) }
            }
        }
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            for (remaining in RESEND_COOLDOWN_SECONDS downTo 0) {
                _uiState.update { it.copy(resendInSeconds = remaining) }
                if (remaining > 0) delay(1_000)
            }
        }
    }

    companion object {
        const val RESEND_COOLDOWN_SECONDS = 30
    }
}
