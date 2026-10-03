package com.alarysai.alarysai.feature.history.presentation.event

sealed interface HistoryUiEvent {
    /** Opens the generated file (`https://` only). */
    data class OpenResult(val url: String) : HistoryUiEvent

    /** The entry could not be deleted; it stays in the list. */
    data object DeleteFailed : HistoryUiEvent
}
