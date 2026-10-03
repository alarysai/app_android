package com.alarysai.alarysai.feature.advertisers.presentation.event

sealed interface AdvertisersUiEvent {
    /** Opens the advertiser's `https://` link in a Custom Tab. */
    data class OpenLink(val url: String) : AdvertisersUiEvent
}
