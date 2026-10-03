package com.alarysai.alarysai.feature.tips.data.remote

import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.document.observeDocuments
import com.alarysai.alarysai.feature.tips.data.model.TipCategoryDto
import com.alarysai.alarysai.feature.tips.data.model.TipDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Queries from `android-integration.md` section 4.4, each backed by a composite index:
 * `tipCategories (status, order)`, `tips (status, categoryId, order)` and `tips (status, order)`.
 * Keep the status filter: without it the security rules reject the whole query.
 */
class FirestoreTipsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : TipsRemoteDataSource {

    override fun observeActiveCategories(): Flow<RemoteDocumentList<TipCategoryDto>> =
        firestore.collection(CATEGORIES)
            .whereEqualTo(FirestoreContract.FIELD_STATUS, FirestoreContract.STATUS_ACTIVE)
            .orderBy(FirestoreContract.FIELD_ORDER, Query.Direction.ASCENDING)
            .observeDocuments(TipCategoryDto::class.java)

    override fun observeActiveTips(categoryId: String?): Flow<RemoteDocumentList<TipDto>> {
        val active = firestore.collection(TIPS)
            .whereEqualTo(FirestoreContract.FIELD_STATUS, FirestoreContract.STATUS_ACTIVE)
        val filtered = if (categoryId == null) active else active.whereEqualTo(FirestoreContract.FIELD_CATEGORY_ID, categoryId)
        return filtered
            .orderBy(FirestoreContract.FIELD_ORDER, Query.Direction.ASCENDING)
            .observeDocuments(TipDto::class.java)
    }

    private companion object {
        const val CATEGORIES = "tipCategories"
        const val TIPS = "tips"
    }
}
