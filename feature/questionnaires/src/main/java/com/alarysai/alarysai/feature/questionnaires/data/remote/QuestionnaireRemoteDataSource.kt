package com.alarysai.alarysai.feature.questionnaires.data.remote

import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.feature.questionnaires.data.model.QuestionnaireDto
import com.alarysai.alarysai.feature.questionnaires.data.model.StepDto
import kotlinx.coroutines.flow.Flow

interface QuestionnaireRemoteDataSource {
    fun observePublishedQuestionnaires(categoryId: String): Flow<RemoteDocumentList<QuestionnaireDto>>

    /** Null when the document does not exist. */
    suspend fun getQuestionnaire(questionnaireId: String): RemoteDocument<QuestionnaireDto>?

    suspend fun getSteps(questionnaireId: String): List<RemoteDocument<StepDto>>
}
