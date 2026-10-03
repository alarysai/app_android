package com.alarysai.alarysai.feature.tips.data.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.error.toContentLoadError
import com.alarysai.alarysai.feature.tips.data.mapper.toDomainOrNull
import com.alarysai.alarysai.feature.tips.data.remote.TipsRemoteDataSource
import com.alarysai.alarysai.feature.tips.domain.model.Tip
import com.alarysai.alarysai.feature.tips.domain.model.TipCategory
import com.alarysai.alarysai.feature.tips.domain.repository.TipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TipRepositoryImpl @Inject constructor(
    private val remoteDataSource: TipsRemoteDataSource,
) : TipRepository {

    override fun observeActiveCategories(): Flow<ContentList<TipCategory>> =
        remoteDataSource.observeActiveCategories()
            .map { remote ->
                ContentList(
                    items = remote.documents
                        .mapNotNull { it.data.toDomainOrNull(it.id) }
                        .sortedWith(compareBy<TipCategory> { it.order }.thenBy { it.id }),
                    isFromCache = remote.isFromCache,
                )
            }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }

    override fun observeActiveTips(categoryId: String?): Flow<ContentList<Tip>> =
        remoteDataSource.observeActiveTips(categoryId)
            .map { remote ->
                ContentList(
                    items = remote.documents
                        .mapNotNull { it.data.toDomainOrNull(it.id) }
                        // The query already filters; this guards against a stale cache entry.
                        .filter { categoryId == null || it.categoryId == categoryId }
                        .sortedWith(compareBy<Tip> { it.order }.thenBy { it.id }),
                    isFromCache = remote.isFromCache,
                )
            }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }
}
