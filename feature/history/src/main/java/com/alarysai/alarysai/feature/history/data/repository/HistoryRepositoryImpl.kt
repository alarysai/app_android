package com.alarysai.alarysai.feature.history.data.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.error.toContentLoadError
import com.alarysai.alarysai.feature.history.data.mapper.toDomain
import com.alarysai.alarysai.feature.history.data.remote.UserActivityRemoteDataSource
import com.alarysai.alarysai.feature.history.domain.model.HistoryEntry
import com.alarysai.alarysai.feature.history.domain.repository.HistoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserActivityRemoteDataSource,
) : HistoryRepository {

    override fun observeHistory(userId: String): Flow<ContentList<HistoryEntry>> =
        remoteDataSource.observeHistory(userId)
            .map { remote ->
                ContentList(
                    items = remote.documents
                        .map { it.data.toDomain(it.id) }
                        .sortedWith(mostRecentFirst()),
                    isFromCache = remote.isFromCache,
                )
            }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }

    override suspend fun deleteEntry(userId: String, entryId: String) {
        try {
            remoteDataSource.deleteHistoryEntry(userId, entryId)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw ContentLoadException(error.toContentLoadError(), error)
        }
    }

    /** Entries still waiting for their server timestamp go first, like a just-created item. */
    private fun mostRecentFirst() =
        compareByDescending<HistoryEntry> { it.createdAtMillis ?: Long.MAX_VALUE }.thenBy { it.id }
}
