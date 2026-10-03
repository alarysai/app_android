package com.alarysai.alarysai.feature.home.presentation.state

import com.alarysai.alarysai.core.common.content.ContentLoadError

/** Single source of truth for the home screen. Each section renders independently. */
data class HomeUiState(
    /** Null until login exists: the greeting shows "Olá!" without a name. */
    val userName: String? = null,
    val searchQuery: String = "",
    val categories: CategoriesSection = CategoriesSection.Loading,
    /** Null until the credits service exists: the card explains where credits will show up. */
    val planUsage: PlanUsageUi? = null,
    val chatMessage: String = "",
)

/** A category ready to render: name already in the user's language. */
data class CategoryItemUi(
    val id: String,
    val name: String,
    val iconUrl: String?,
    /** Position in the full list, so a card keeps its color while the search filters others out. */
    val accentIndex: Int,
)

data class PlanUsageUi(
    val usedPercent: Int,
    val availableCredits: Int,
)

sealed interface CategoriesSection {
    data object Loading : CategoriesSection

    /** [categories] is already filtered by the search. [isOffline]: list came from the offline cache. */
    data class Success(
        val categories: List<CategoryItemUi>,
        val isOffline: Boolean,
    ) : CategoriesSection

    /** There are categories, but none matches the search. */
    data class NoSearchResults(val query: String) : CategoriesSection

    data class Empty(val isOffline: Boolean) : CategoriesSection

    data class Error(val error: ContentLoadError) : CategoriesSection
}
