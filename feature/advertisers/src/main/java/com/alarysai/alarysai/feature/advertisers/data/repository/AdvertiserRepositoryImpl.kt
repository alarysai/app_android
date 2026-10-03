package com.alarysai.alarysai.feature.advertisers.data.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.error.toContentLoadError
import com.alarysai.alarysai.feature.advertisers.data.mapper.toDomainOrNull
import com.alarysai.alarysai.feature.advertisers.data.remote.AdvertisersRemoteDataSource
import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser
import com.alarysai.alarysai.feature.advertisers.domain.repository.AdvertiserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdvertiserRepositoryImpl @Inject constructor(
    private val remoteDataSource: AdvertisersRemoteDataSource,
) : AdvertiserRepository {

    override fun observeActiveAdvertisers(): Flow<ContentList<Advertiser>> =
        remoteDataSource.observeActiveAdvertisers()
            .map { remote ->
                ContentList(
                    items = remote.documents
                        .mapNotNull { it.data.toDomainOrNull(it.id) }
                        .sortedWith(compareBy<Advertiser> { it.order }.thenBy { it.id }),
                    isFromCache = remote.isFromCache,
                )
            }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }
}
