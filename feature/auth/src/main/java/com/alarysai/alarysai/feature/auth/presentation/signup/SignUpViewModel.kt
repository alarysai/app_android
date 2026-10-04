package com.alarysai.alarysai.feature.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.feature.auth.domain.model.AuthException
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.alarysai.alarysai.feature.auth.domain.repository.KeepSignedInRepository
import com.alarysai.alarysai.feature.auth.domain.usecase.ValidateSignUpUseCase
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

/** Creates the account; the app root then shows the profile setup ("Your profile was created"). */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val keepSignedInRepository: KeepSignedInRepository,
    private val validateSignUp: ValidateSignUpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SignUpUiEvent>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<SignUpUiEvent> = _events.asSharedFlow()

    private var attempted = false

    fun onAction(action: SignUpUiAction) {
        when (action) {
            is SignUpUiAction.NameChanged -> edit { it.copy(name = action.name) }
            is SignUpUiAction.EmailChanged -> edit { it.copy(email = action.email) }
            is SignUpUiAction.PasswordChanged -> edit { it.copy(password = action.password) }
            is SignUpUiAction.ConfirmationChanged -> edit { it.copy(confirmation = action.confirmation) }
            SignUpUiAction.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is SignUpUiAction.TermsChanged -> edit { it.copy(acceptedTerms = action.accepted) }
            SignUpUiAction.CreateAccountClicked -> createAccount()
            SignUpUiAction.BackClicked -> _events.tryEmit(SignUpUiEvent.BackToLogin)
        }
    }

    /** After the first attempt, errors follow the typing so they disappear as soon as they are fixed. */
    private fun edit(transform: (SignUpUiState) -> SignUpUiState) {
        _uiState.update { state ->
            val edited = transform(state).copy(error = null)
            if (attempted) edited.copy(errors = edited.validate()) else edited
        }
    }

    private fun createAccount() {
        val state = _uiState.value
        if (!state.canSubmit) return
        attempted = true
        val errors = state.validate()
        if (!errors.isEmpty) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        keepSignedInRepository.setKeepSignedIn(true)
        _uiState.update { it.copy(isLoading = true, errors = errors, error = null) }
        viewModelScope.launch {
            try {
                authRepository.signUp(state.name, state.email, state.password)
                _uiState.update { it.copy(isLoading = false) }
            } catch (failure: AuthException) {
                _uiState.update { it.copy(isLoading = false, error = failure.error) }
            }
        }
    }

    private fun SignUpUiState.validate() = validateSignUp(name, email, password, confirmation, acceptedTerms)
}
