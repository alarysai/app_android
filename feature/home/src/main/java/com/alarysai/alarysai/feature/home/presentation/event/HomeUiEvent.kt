package com.alarysai.alarysai.feature.home.presentation.event

sealed interface HomeUiEvent {
    /** The feature has no backend yet; the screen shows a "coming soon" snackbar. */
    data class ShowComingSoon(val feature: ComingSoonFeature) : HomeUiEvent

    /** Opens the questionnaires of a category; the name becomes the next screen's title. */
    data class OpenCategory(val categoryId: String, val categoryName: String) : HomeUiEvent
}

enum class ComingSoonFeature {
    CHAT,
    NOTIFICATIONS,
    CREDITS,
}
