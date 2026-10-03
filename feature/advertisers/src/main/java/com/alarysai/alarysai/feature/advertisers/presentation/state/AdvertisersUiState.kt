package com.alarysai.alarysai.feature.advertisers.presentation.state

import com.alarysai.alarysai.core.common.content.ContentLoadError

sealed interface AdvertisersUiState {
    data object Loading : AdvertisersUiState

    /** [isOffline]: the list came from the offline cache; show a discreet notice. */
    data class Success(
        val groups: List<AdvertiserGroupUi>,
        val isOffline: Boolean,
    ) : AdvertisersUiState

    data class Empty(val isOffline: Boolean) : AdvertisersUiState

    data class Error(val error: ContentLoadError) : AdvertisersUiState
}

/** [type] null is the group of advertisers without a type ("Others"). */
data class AdvertiserGroupUi(
    val type: String?,
    val advertisers: List<AdvertiserItemUi>,
)

data class AdvertiserItemUi(
    val id: String,
    /** Null for image-only advertisers; the screen then shows the link's host. */
    val name: String?,
    val imageUrl: String?,
    val link: String,
)
