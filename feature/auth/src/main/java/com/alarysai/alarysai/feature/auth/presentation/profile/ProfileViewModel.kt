package com.alarysai.alarysai.feature.auth.presentation.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.common.language.PreferredLanguageHolder
import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.SessionUser
import com.alarysai.alarysai.core.common.session.UserProfile
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Name and language of the signed-in user (`users/{uid}`). In [ProfileMode.SETUP] it creates the
 * document; the app root then opens the app. Values typed by the user are never overwritten by
 * late session or profile updates.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val profileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
    private val preferredLanguage: PreferredLanguageHolder,
    languageProvider: LanguageProvider,
) : ViewModel() {

    private val mode = if (savedStateHandle.get<String>(ARG_MODE) == MODE_EDIT) ProfileMode.EDIT else ProfileMode.SETUP

    private val _uiState = MutableStateFlow(ProfileUiState(mode = mode, language = languageProvider.currentLanguage()))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileUiEvent>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<ProfileUiEvent> = _events.asSharedFlow()

    private var user: SessionUser? = null
    private var nameEdited = false
    private var languageEdited = false

    init {
        observeUser()
    }

    fun onAction(action: ProfileUiAction) {
        when (action) {
            is ProfileUiAction.NameChanged -> {
                nameEdited = true
                _uiState.update { it.copy(name = action.name, showNameError = false, saveFailed = false) }
            }
            is ProfileUiAction.LanguageSelected -> {
                languageEdited = true
                _uiState.update { it.copy(language = action.language, saveFailed = false) }
            }
            ProfileUiAction.SaveClicked -> save()
            ProfileUiAction.SignOutClicked -> authRepository.signOut()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUser() {
        sessionRepository.observeUser()
            .filterNotNull()
            .onEach { signedIn ->
                user = signedIn
                _uiState.update { state ->
                    state.copy(
                        email = signedIn.email,
                        name = if (nameEdited || state.name.isNotBlank()) state.name else signedIn.displayName.orEmpty(),
                    )
                }
            }
            .flatMapLatest { signedIn -> profileRepository.observeProfile(signedIn.uid) }
            .catch { emit(null) }
            .onEach { profile -> applyProfile(profile) }
            .launchIn(viewModelScope)
    }

    private fun applyProfile(profile: UserProfile?) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                name = if (nameEdited) state.name else profile?.displayName ?: state.name,
                language = if (languageEdited) state.language else profile?.language ?: state.language,
            )
        }
    }

    private fun save() {
        val state = _uiState.value
        val uid = user?.uid ?: return
        if (state.isSaving) return
        if (state.name.isBlank()) {
            _uiState.update { it.copy(showNameError = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                profileRepository.saveProfile(uid, state.name, state.language, isNew = mode == ProfileMode.SETUP)
                preferredLanguage.language = state.language
                nameEdited = false
                languageEdited = false
                _uiState.update { it.copy(isSaving = false) }
                if (mode == ProfileMode.EDIT) _events.tryEmit(ProfileUiEvent.Saved)
            } catch (failure: ContentLoadException) {
                _uiState.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    companion object {
        const val ARG_MODE = "mode"
        const val MODE_EDIT = "edit"
    }
}
