package com.alarysai.alarysai.feature.history.data.remote

import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.document.deleteDocument
import com.alarysai.alarysai.core.firebase.document.observeDocuments
import com.alarysai.alarysai.core.firebase.document.observeRemoteDocument
import com.alarysai.alarysai.feature.history.data.model.CreditTransactionDto
import com.alarysai.alarysai.feature.history.data.model.HistoryEntryDto
import com.alarysai.alarysai.feature.history.data.model.UserCreditsDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Queries from `android-integration.md` section 8: most recent first, on single-field indexes
 * Firestore creates automatically.
 */
class FirestoreUserActivityDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserActivityRemoteDataSource {

    override fun observeHistory(userId: String): Flow<RemoteDocumentList<HistoryEntryDto>> =
        user(userId).collection(HISTORY)
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .observeDocuments(HistoryEntryDto::class.java)

    override suspend fun deleteHistoryEntry(userId: String, entryId: String) {
        user(userId).collection(HISTORY).document(entryId).deleteDocument()
    }

    override fun observeUserCredits(userId: String): Flow<RemoteDocument<UserCreditsDto>?> =
        user(userId).observeRemoteDocument(UserCreditsDto::class.java)

    override fun observeCreditTransactions(userId: String): Flow<RemoteDocumentList<CreditTransactionDto>> =
        user(userId).collection(CREDITS)
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .observeDocuments(CreditTransactionDto::class.java)

    private fun user(userId: String) = firestore.collection(USERS).document(userId)

    private companion object {
        const val USERS = "users"
        const val HISTORY = "history"
        const val CREDITS = "credits"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
