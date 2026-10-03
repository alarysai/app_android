package com.alarysai.alarysai.feature.home.presentation.action

import com.alarysai.alarysai.feature.home.presentation.state.CategoryItemUi

sealed interface HomeUiAction {
    data class SearchQueryChanged(val query: String) : HomeUiAction
    data class ChatMessageChanged(val message: String) : HomeUiAction
    data object ChatSendClicked : HomeUiAction
    data object NotificationsClicked : HomeUiAction
    data object BuyCreditsClicked : HomeUiAction
    data object RetryCategories : HomeUiAction
    data class CategoryClicked(val category: CategoryItemUi) : HomeUiAction
}
