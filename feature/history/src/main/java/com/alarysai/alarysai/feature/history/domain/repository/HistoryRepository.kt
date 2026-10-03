package com.alarysai.alarysai.feature.history.domain.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.feature.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    /** The user's history, most recent first. Fails with `ContentLoadException`. */
    fun observeHistory(userId: String): Flow<ContentList<HistoryEntry>>

    /** The owner may delete their own entries (the only write the rules allow). Fails with `ContentLoadException`. */
    suspend fun deleteEntry(userId: String, entryId: String)
}
