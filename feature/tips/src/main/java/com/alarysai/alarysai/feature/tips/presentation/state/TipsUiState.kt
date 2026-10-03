package com.alarysai.alarysai.feature.tips.presentation.state

import com.alarysai.alarysai.core.common.content.ContentLoadError

data class TipsUiState(
    /** Category chips; empty (no chip row) while loading or when categories could not be read. */
    val categories: List<TipCategoryUi> = emptyList(),
    /** Null means "All". */
    val selectedCategoryId: String? = null,
    val content: TipsContent = TipsContent.Loading,
)

/** Name already resolved to the user's language. */
data class TipCategoryUi(
    val id: String,
    val name: String,
)

/** A tip ready to render: text already resolved to the user's language. */
data class TipItemUi(
    val id: String,
    val text: String,
    val imageUrl: String?,
    /** Shown above the text when listing all categories; null inside a category. */
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
