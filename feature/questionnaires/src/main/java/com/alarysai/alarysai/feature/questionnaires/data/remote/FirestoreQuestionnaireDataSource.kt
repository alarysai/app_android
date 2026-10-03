package com.alarysai.alarysai.feature.questionnaires.data.remote

import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.document.getRemoteDocumentOrNull
import com.alarysai.alarysai.core.firebase.document.getRemoteDocuments
import com.alarysai.alarysai.core.firebase.document.observeDocuments
import com.alarysai.alarysai.feature.questionnaires.data.model.QuestionnaireDto
import com.alarysai.alarysai.feature.questionnaires.data.model.StepDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Query from `android-integration.md` section 4.2, backed by the composite index
 * `questionnaires (status, categoryId, order)`. Keep the status filter (the rules reject the
 * query without it) and do not add `whereArrayContains("languages", …)`: it needs another index.
 */
class FirestoreQuestionnaireDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : QuestionnaireRemoteDataSource {

    override fun observePublishedQuestionnaires(categoryId: String): Flow<RemoteDocumentList<QuestionnaireDto>> =
        firestore.collection(COLLECTION)
            .whereEqualTo(FirestoreContract.FIELD_STATUS, FirestoreContract.STATUS_PUBLISHED)
            .whereEqualTo(FirestoreContract.FIELD_CATEGORY_ID, categoryId)
            .orderBy(FirestoreContract.FIELD_ORDER, Query.Direction.ASCENDING)
            .observeDocuments(QuestionnaireDto::class.java)

    /** Section 4.3. Fails with PERMISSION_DENIED once the questionnaire is unpublished. */
    override suspend fun getQuestionnaire(questionnaireId: String): RemoteDocument<QuestionnaireDto>? =
        firestore.collection(COLLECTION).document(questionnaireId)
            .getRemoteDocumentOrNull(QuestionnaireDto::class.java)

    /** All steps at once; no status filter here, the rules check the parent questionnaire. */
    override suspend fun getSteps(questionnaireId: String): List<RemoteDocument<StepDto>> =
        firestore.collection(COLLECTION).document(questionnaireId).collection(STEPS_COLLECTION)
            .orderBy(FirestoreContract.FIELD_ORDER, Query.Direction.ASCENDING)
            .getRemoteDocuments(StepDto::class.java)

    private companion object {
        const val COLLECTION = "questionnaires"
        const val STEPS_COLLECTION = "steps"
    }
}
