package com.alarysai.alarysai.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.language.PreferredLanguageHolder
import com.alarysai.alarysai.feature.auth.domain.model.AuthGate
import com.alarysai.alarysai.feature.auth.domain.usecase.ApplyKeepSignedInUseCase
import com.alarysai.alarysai.feature.auth.domain.usecase.ObserveAuthGateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Decides what the app shows: login, the first-access profile, or the app itself. Also keeps the
 * profile language as the content language. Null state = still checking the session.
 */
@HiltViewModel
class AppRootViewModel @Inject constructor(
    applyKeepSignedIn: ApplyKeepSignedInUseCase,
    observeAuthGate: ObserveAuthGateUseCase,
    private val preferredLanguage: PreferredLanguageHolder,
) : ViewModel() {

    private val _gate = MutableStateFlow<AuthGate?>(null)
    val gate: StateFlow<AuthGate?> = _gate.asStateFlow()

    init {
        // Before observing: a session the user chose not to keep is ended right away.
        applyKeepSignedIn()
        observeAuthGate()
            .onEach { gate ->
                preferredLanguage.language = (gate as? AuthGate.Ready)?.profile?.language
                _gate.value = gate
            }
            .launchIn(viewModelScope)
    }
}
