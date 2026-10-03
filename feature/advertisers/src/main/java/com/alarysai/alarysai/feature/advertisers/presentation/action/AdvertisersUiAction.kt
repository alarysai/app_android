package com.alarysai.alarysai.feature.advertisers.presentation.action

import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserItemUi

sealed interface AdvertisersUiAction {
    data class AdvertiserClicked(val advertiser: AdvertiserItemUi) : AdvertisersUiAction
    data object Retry : AdvertisersUiAction
}
