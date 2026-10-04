package com.alarysai.alarysai.feature.auth.presentation.profile

import com.alarysai.alarysai.core.common.language.Language

/** [SETUP]: first sign-in ("Your profile was created"). [EDIT]: the Profile tab, with sign-out. */
enum class ProfileMode { SETUP, EDIT }

data class ProfileUiState(
    val mode: ProfileMode,
    val isLoading: Boolean = true,
    val email: String? = null,
    val name: String = "",
    val language: Language = Language.PT,
    val isSaving: Boolean = false,
    val showNameError: Boolean = false,
    val saveFailed: Boolean = false,
) {
    /** "Marina Alves" -> "MA"; empty name -> "". */
    val initials: String
        get() = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            .let { parts -> listOfNotNull(parts.firstOrNull(), parts.drop(1).lastOrNull()) }
            .joinToString("") { it.take(1).uppercase() }
}

sealed interface ProfileUiAction {
    data class NameChanged(val name: String) : ProfileUiAction
    data class LanguageSelected(val language: Language) : ProfileUiAction
    data object SaveClicked : ProfileUiAction
    data object SignOutClicked : ProfileUiAction
}

sealed interface ProfileUiEvent {
    /** EDIT mode: changes saved. */
    data object Saved : ProfileUiEvent
}
