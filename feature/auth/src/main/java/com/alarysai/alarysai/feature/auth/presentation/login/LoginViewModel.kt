package com.alarysai.alarysai.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.feature.auth.di.GoogleSignInConfig
import com.alarysai.alarysai.feature.auth.domain.model.AuthException
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.alarysai.alarysai.feature.auth.domain.repository.KeepSignedInRepository
import com.alarysai.alarysai.feature.auth.domain.usecase.EmailValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
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
 * E-mail/password and Google sign-in. Success needs no handling here: the session changes and
 * the app root leaves the login flow on its own.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val keepSignedInRepository: KeepSignedInRepository,
    googleSignInConfig: GoogleSignInConfig,
) : ViewModel() {

    /** `serverClientId` for the Google account picker. */
    val googleWebClientId: String = googleSignInConfig.webClientId

    private val _uiState = MutableStateFlow(LoginUiState(keepSignedIn = keepSignedInRepository.isKeepSignedIn()))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginUiEvent>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<LoginUiEvent> = _events.asSharedFlow()

    fun onAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.EmailChanged -> _uiState.update { it.copy(email = action.email, showEmailError = false, error = null) }
            is LoginUiAction.PasswordChanged -> _uiState.update { it.copy(password = action.password, error = null) }
            LoginUiAction.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginUiAction.KeepSignedInChanged -> _uiState.update { it.copy(keepSignedIn = action.keep) }
            LoginUiAction.SignInClicked -> signIn()
            is LoginUiAction.GoogleTokenReceived -> launchSignIn { authRepository.signInWithGoogle(action.idToken) }
            is LoginUiAction.GoogleFailed -> _uiState.update { it.copy(error = action.error) }
            LoginUiAction.SignUpClicked -> _events.tryEmit(LoginUiEvent.OpenSignUp)
            LoginUiAction.ForgotPasswordClicked -> _events.tryEmit(LoginUiEvent.OpenForgotPassword)
        }
    }

    private fun signIn() {
        val state = _uiState.value
        if (!state.canSubmit) return
        if (!EmailValidator.isValid(state.email)) {
            _uiState.update { it.copy(showEmailError = true) }
            return
        }
        launchSignIn { authRepository.signIn(state.email, state.password) }
    }

    /** Saves "keep me signed in" before signing in, so the choice applies to this session. */
    private fun launchSignIn(request: suspend () -> Unit) {
        if (_uiState.value.isLoading) return
        keepSignedInRepository.setKeepSignedIn(_uiState.value.keepSignedIn)
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                request()
                _uiState.update { it.copy(isLoading = false) }
            } catch (failure: AuthException) {
                _uiState.update { it.copy(isLoading = false, error = failure.error) }
            }
        }
    }
}
