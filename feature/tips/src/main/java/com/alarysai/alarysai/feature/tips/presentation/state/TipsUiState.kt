package com.alarysai.alarysai.feature.tips.presentation.state

import com.alarysai.alarysai.core.common.content.ContentLoadError

/** Tips of every active category, for the carousel at the top of the "Club AI" tab. */
data class TipsUiState(
    val content: TipsContent = TipsContent.Loading,
)

/** A tip ready to render: text already resolved to the user's language. */
data class TipItemUi(
    val id: String,
    val text: String,
    val imageUrl: String?,
    /** Category name in the user's language; null until the categories load (or if they fail). */
    val categoryName: String?,
    /** False when the tip is not fully translated: it shows in Portuguese, with a badge. */
    val isInUserLanguage: Boolean,
)

sealed interface TipsContent {
    data object Loading : TipsContent

    /** [isOffline]: the list came from the offline cache; show a discreet notice. */
    data class Success(
        val tips: List<TipItemUi>,
        val isOffline: Boolean,
    ) : TipsContent

    data class Empty(val isOffline: Boolean) : TipsContent

    data class Error(val error: ContentLoadError) : TipsContent
}
