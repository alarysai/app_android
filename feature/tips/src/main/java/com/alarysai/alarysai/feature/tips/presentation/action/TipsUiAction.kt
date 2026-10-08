package com.alarysai.alarysai.feature.tips.presentation.action

sealed interface TipsUiAction {
    data object Retry : TipsUiAction
}
