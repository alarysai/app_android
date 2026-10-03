package com.alarysai.alarysai.feature.questionnaires.data.repository

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.questionnaires.data.model.QuestionnaireDto
import com.alarysai.alarysai.feature.questionnaires.data.model.StepDto
import com.alarysai.alarysai.feature.questionnaires.data.remote.QuestionnaireRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionnaireRepositoryImplTest {

    private class FakeDataSource(
        private val flow: Flow<RemoteDocumentList<QuestionnaireDto>> = emptyFlow(),
        private val questionnaire: suspend () -> RemoteDocument<QuestionnaireDto>? = { null },
        private val steps: suspend () -> List<RemoteDocument<StepDto>> = { emptyList() },
    ) : QuestionnaireRemoteDataSource {
        var requestedCategoryId: String? = null

        override fun observePublishedQuestionnaires(categoryId: String): Flow<RemoteDocumentList<QuestionnaireDto>> {
            requestedCategoryId = categoryId
            return flow
        }

        override suspend fun getQuestionnaire(questionnaireId: String) = questionnaire()

        override suspend fun getSteps(questionnaireId: String) = steps()
    }

    private val publishedDocument = questionnaire("q1", order = 0)

    private fun videoStep(id: String, order: Long) = RemoteDocument(
        id,
        StepDto(order = order, type = "video", text = LocalizedTextDto(pt = "V $id"), videoUrl = "https://v/$id"),
    )

    private suspend fun loadError(dataSource: FakeDataSource): ContentLoadError =
        try {
            QuestionnaireRepositoryImpl(dataSource).getQuestionnaireWithSteps("q1")
            error("expected a failure")
        } catch (failure: ContentLoadException) {
            failure.error
        }

    @Test
    fun `loads the questionnaire with its valid steps sorted by order then id`() = runTest {
        val dataSource = FakeDataSource(
            questionnaire = { publishedDocument },
            steps = {
                listOf(
                    videoStep("b", 1),
                    videoStep("c", 0),
                    videoStep("a", 1),
                    RemoteDocument("broken", StepDto(order = 0, type = "video", text = LocalizedTextDto(pt = "x"))),
                )
            },
        )

        val loaded = QuestionnaireRepositoryImpl(dataSource).getQuestionnaireWithSteps("q1")

        assertEquals("q1", loaded.questionnaire.id)
        assertEquals(listOf("c", "a", "b"), loaded.steps.map { it.id })
    }

    @Test
    fun `missing, draft or stepless questionnaires are unavailable`() = runTest {
        assertEquals(ContentLoadError.UNAVAILABLE, loadError(FakeDataSource(questionnaire = { null })))
        assertEquals(
            ContentLoadError.UNAVAILABLE,
            loadError(FakeDataSource(questionnaire = { questionnaire("q1", order = 0, status = "draft") })),
        )
        assertEquals(
            ContentLoadError.UNAVAILABLE,
            loadError(FakeDataSource(questionnaire = { publishedDocument }, steps = { emptyList() })),
        )
    }

    @Test
    fun `unpublished questionnaire (permission denied) is unavailable, no network is offline`() = runTest {
        val denied = FakeDataSource(questionnaire = {
            throw FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        })
        val offline = FakeDataSource(
            questionnaire = { publishedDocument },
            steps = { throw FirebaseFirestoreException("offline", FirebaseFirestoreException.Code.UNAVAILABLE) },
        )

        assertEquals(ContentLoadError.UNAVAILABLE, loadError(denied))
        assertEquals(ContentLoadError.OFFLINE, loadError(offline))
    }

    private fun questionnaire(id: String, order: Long, categoryId: String = "cat1", status: String = "published") =
        RemoteDocument(
            id,
            QuestionnaireDto(
                title = LocalizedTextDto(pt = "Q $id"),
                categoryId = categoryId,
                languages = listOf("pt"),
                order = order,
                status = status,
            ),
        )

    @Test
    fun `asks for the category and sorts by order then id, dropping what cannot be shown`() = runTest {
        val remote = RemoteDocumentList(
            documents = listOf(
                questionnaire("b", order = 1),
                questionnaire("c", order = 0),
                questionnaire("a", order = 1),
                questionnaire("draft", order = 0, status = "draft"),
                questionnaire("other", order = 0, categoryId = "cat2"),
            ),
            isFromCache = true,
        )
        val dataSource = FakeDataSource(flow = flowOf(remote))
        val repository = QuestionnaireRepositoryImpl(dataSource)

        repository.observePublishedQuestionnaires("cat1").test {
            val list = awaitItem()
            assertEquals(listOf("c", "a", "b"), list.items.map { it.id })
            assertTrue(list.isFromCache)
            awaitComplete()
        }
        assertEquals("cat1", dataSource.requestedCategoryId)
    }

    @Test
    fun `firestore failures become content load exceptions`() = runTest {
        val failing = flow<RemoteDocumentList<QuestionnaireDto>> {
            throw FirebaseFirestoreException("offline", FirebaseFirestoreException.Code.UNAVAILABLE)
        }
        val repository = QuestionnaireRepositoryImpl(FakeDataSource(flow = failing))

        repository.observePublishedQuestionnaires("cat1").test {
            val error = awaitError()
            assertTrue(error is ContentLoadException)
            assertEquals(ContentLoadError.OFFLINE, (error as ContentLoadException).error)
        }
    }
}
