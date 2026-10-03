package com.alarysai.alarysai.feature.history.data.remote

import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.feature.history.data.model.CreditTransactionDto
import com.alarysai.alarysai.feature.history.data.model.HistoryEntryDto
import com.alarysai.alarysai.feature.history.data.model.UserCreditsDto
import kotlinx.coroutines.flow.Flow

/** Everything under `users/{uid}` this feature reads; the rules let only the owner read it. */
interface UserActivityRemoteDataSource {
    fun observeHistory(userId: String): Flow<RemoteDocumentList<HistoryEntryDto>>
    suspend fun deleteHistoryEntry(userId: String, entryId: String)
    fun observeUserCredits(userId: String): Flow<RemoteDocument<UserCreditsDto>?>
    fun observeCreditTransactions(userId: String): Flow<RemoteDocumentList<CreditTransactionDto>>
}
