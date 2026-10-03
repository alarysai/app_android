package com.alarysai.alarysai.feature.advertisers.domain.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser
import kotlinx.coroutines.flow.Flow

interface AdvertiserRepository {

    /** Active advertisers by `order` (ties by ID). Fails with `ContentLoadException`. */
    fun observeActiveAdvertisers(): Flow<ContentList<Advertiser>>
}
