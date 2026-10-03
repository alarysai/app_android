package com.alarysai.alarysai.feature.home.data.remote

import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.document.observeDocuments
import com.alarysai.alarysai.feature.home.data.model.QuestionnaireCategoryDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Query from `android-integration.md` section 4.1, backed by the composite index
 * `questionnaireCategories (status, order)`. Keep the status filter: without it the
 * security rules reject the whole query with PERMISSION_DENIED.
 */
class FirestoreQuestionnaireCategoryDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : QuestionnaireCategoryRemoteDataSource {

    override fun observeActiveCategories(): Flow<RemoteDocumentList<QuestionnaireCategoryDto>> =
        firestore.collection(COLLECTION)
            .whereEqualTo(FirestoreContract.FIELD_STATUS, FirestoreContract.STATUS_ACTIVE)
            .orderBy(FirestoreContract.FIELD_ORDER, Query.Direction.ASCENDING)
            .observeDocuments(QuestionnaireCategoryDto::class.java)

    private companion object {
        const val COLLECTION = "questionnaireCategories"
    }
}
