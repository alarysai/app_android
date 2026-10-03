package com.alarysai.alarysai.feature.questionnaires.data.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.error.toContentLoadError
import com.alarysai.alarysai.feature.questionnaires.data.mapper.toDomainOrNull
import com.alarysai.alarysai.feature.questionnaires.data.remote.QuestionnaireRemoteDataSource
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire
import com.alarysai.alarysai.feature.questionnaires.domain.model.QuestionnaireWithSteps
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.repository.QuestionnaireRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestionnaireRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuestionnaireRemoteDataSource,
) : QuestionnaireRepository {

    override fun observePublishedQuestionnaires(categoryId: String): Flow<ContentList<Questionnaire>> =
        remoteDataSource.observePublishedQuestionnaires(categoryId)
            .map { remote ->
                ContentList(
                    items = remote.documents
                        .mapNotNull { it.data.toDomainOrNull(it.id) }
                        // The query already filters by category; this guards against a stale cache entry.
                        .filter { it.categoryId == categoryId }
                        .sortedWith(compareBy<Questionnaire> { it.order }.thenBy { it.id }),
                    isFromCache = remote.isFromCache,
                )
            }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }

    override suspend fun getQuestionnaireWithSteps(questionnaireId: String): QuestionnaireWithSteps {
        val (questionnaire, steps) = try {
            val questionnaire = remoteDataSource.getQuestionnaire(questionnaireId)
                ?.let { it.data.toDomainOrNull(it.id) }
                ?: throw unavailable()
            questionnaire to remoteDataSource.getSteps(questionnaireId).mapNotNull { it.data.toDomainOrNull(it.id) }
        } catch (error: ContentLoadException) {
            throw error
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw ContentLoadException(error.toContentLoadError(), error)
        }
        if (steps.isEmpty()) throw unavailable()
        return QuestionnaireWithSteps(
            questionnaire = questionnaire,
            steps = steps.sortedWith(compareBy<Step> { it.order }.thenBy { it.id }),
        )
    }

    private fun unavailable() = ContentLoadException(ContentLoadError.UNAVAILABLE)
}
