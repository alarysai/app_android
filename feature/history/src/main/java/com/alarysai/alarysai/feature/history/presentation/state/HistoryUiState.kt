package com.alarysai.alarysai.feature.history.presentation.state

import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.feature.history.domain.model.CreditKind
import com.alarysai.alarysai.feature.history.domain.model.OutputType

sealed interface HistoryUiState {
    /** Waiting to know whether someone is signed in. */
    data object Loading : HistoryUiState

    /** History and credits belong to a user; there is nothing to show without one. */
    data object SignedOut : HistoryUiState

    data class SignedIn(
        /** Null while loading or when it could not be read. */
        val balance: Int? = null,
        val selectedTab: HistoryTab = HistoryTab.GENERATIONS,
        val generations: ListContent<HistoryItemUi> = ListContent.Loading,
        val statement: ListContent<CreditItemUi> = ListContent.Loading,
        /** Entry waiting for the user to confirm its deletion. */
        val pendingDeletion: HistoryItemUi? = null,
    ) : HistoryUiState
}

enum class HistoryTab { GENERATIONS, STATEMENT }

/** Loading, list, empty and error states shared by both tabs. */
sealed interface ListContent<out T> {
    data object Loading : ListContent<Nothing>
    data class Success<T>(val items: List<T>, val isOffline: Boolean) : ListContent<T>
    data class Empty(val isOffline: Boolean) : ListContent<Nothing>
    data class Error(val error: ContentLoadError) : ListContent<Nothing>
}

data class HistoryItemUi(
    val id: String,
    /** Null when the server did not copy a title; the screen shows a generic one. */
    val title: String?,
    val outputType: OutputType,
    val resultText: String?,
    val resultUrl: String?,
    val creditsSpent: Int,
    /** Formatted by the screen in the device locale. */
    val createdAtMillis: Long?,
)

data class CreditItemUi(
    val id: String,
    val kind: CreditKind,
    val amount: Int,
    val balanceAfter: Int,
    val createdAtMillis: Long?,
)
