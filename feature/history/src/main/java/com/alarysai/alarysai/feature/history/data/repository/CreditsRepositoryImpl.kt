package com.alarysai.alarysai.feature.history.data.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.error.toContentLoadError
import com.alarysai.alarysai.feature.history.data.mapper.toBalance
import com.alarysai.alarysai.feature.history.data.mapper.toDomain
import com.alarysai.alarysai.feature.history.data.remote.UserActivityRemoteDataSource
import com.alarysai.alarysai.feature.history.domain.model.CreditTransaction
import com.alarysai.alarysai.feature.history.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CreditsRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserActivityRemoteDataSource,
) : CreditsRepository {

    override fun observeBalance(userId: String): Flow<Int> =
        remoteDataSource.observeUserCredits(userId)
            .map { it?.data.toBalance() }
            .distinctUntilChanged()
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }

    override fun observeTransactions(userId: String): Flow<ContentList<CreditTransaction>> =
        remoteDataSource.observeCreditTransactions(userId)
            .map { remote ->
                ContentList(
                    items = remote.documents
                        .map { it.data.toDomain(it.id) }
                        .sortedWith(compareByDescending<CreditTransaction> { it.createdAtMillis ?: Long.MAX_VALUE }.thenBy { it.id }),
                    isFromCache = remote.isFromCache,
                )
            }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }
}
