package com.alarysai.alarysai.feature.questionnaires.domain.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire
import com.alarysai.alarysai.feature.questionnaires.domain.model.QuestionnaireWithSteps
import kotlinx.coroutines.flow.Flow

interface QuestionnaireRepository {

    /**
     * Published questionnaires of [categoryId], sorted by `order` (ties broken by ID, like the
     * admin panel). Emits again whenever the panel changes them. Fails with `ContentLoadException`.
     */
    fun observePublishedQuestionnaires(categoryId: String): Flow<ContentList<Questionnaire>>

    /**
     * Reads the questionnaire and all its steps at once, so the flow knows the full order.
     * Fails with `ContentLoadException`: `UNAVAILABLE` when it was unpublished, removed, or has
     * no step the app can show.
     */
    suspend fun getQuestionnaireWithSteps(questionnaireId: String): QuestionnaireWithSteps
}
