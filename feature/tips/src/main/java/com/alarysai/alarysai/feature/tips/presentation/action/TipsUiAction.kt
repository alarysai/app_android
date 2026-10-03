package com.alarysai.alarysai.feature.tips.presentation.action

sealed interface TipsUiAction {
    /** Null selects "All". */
    data class CategorySelected(val categoryId: String?) : TipsUiAction
    data object Retry : TipsUiAction
}
