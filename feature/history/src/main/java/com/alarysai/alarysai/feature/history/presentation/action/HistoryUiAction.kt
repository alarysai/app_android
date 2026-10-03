package com.alarysai.alarysai.feature.history.presentation.action

import com.alarysai.alarysai.feature.history.presentation.state.HistoryItemUi
import com.alarysai.alarysai.feature.history.presentation.state.HistoryTab

sealed interface HistoryUiAction {
    data class TabSelected(val tab: HistoryTab) : HistoryUiAction
    data class OpenResultClicked(val item: HistoryItemUi) : HistoryUiAction

    /** Asks for confirmation first; nothing is deleted yet. */
    data class DeleteClicked(val item: HistoryItemUi) : HistoryUiAction
    data object DeleteConfirmed : HistoryUiAction
    data object DeleteDismissed : HistoryUiAction
    data object Retry : HistoryUiAction
}
