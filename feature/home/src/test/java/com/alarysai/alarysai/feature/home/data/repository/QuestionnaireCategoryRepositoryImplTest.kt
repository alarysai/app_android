package com.alarysai.alarysai.feature.home.data.repository

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.home.data.model.QuestionnaireCategoryDto
import com.alarysai.alarysai.feature.home.data.remote.QuestionnaireCategoryRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionnaireCategoryRepositoryImplTest {

    private class FakeDataSource(
        private val flow: Flow<RemoteDocumentList<QuestionnaireCategoryDto>>,
    ) : QuestionnaireCategoryRemoteDataSource {
        override fun observeActiveCategories() = flow
    }

    private fun category(id: String, order: Long, status: String = "active", pt: String = "Cat $id") =
        RemoteDocument(id, QuestionnaireCategoryDto(name = LocalizedTextDto(pt = pt), order = order, status = status))

    @Test
    fun `sorts by order then by id and drops categories that cannot be shown`() = runTest {
        val remote = RemoteDocumentList(
            documents = listOf(
                category("b", order = 1),
                category("c", order = 0),
                category("a", order = 1),
                category("inactive", order = 0, status = "inactive"),
                category("nameless", order = 0, pt = ""),
            ),
            isFromCache = false,
        )
        val repository = QuestionnaireCategoryRepositoryImpl(FakeDataSource(flowOf(remote)))

        repository.observeActiveCategories().test {
            val list = awaitItem()
            assertEquals(listOf("c", "a", "b"), list.items.map { it.id })
            assertFalse(list.isFromCache)
            awaitComplete()
        }
    }

    @Test
    fun `keeps the offline cache flag`() = runTest {
        val remote = RemoteDocumentList(listOf(category("a", order = 0)), isFromCache = true)
        val repository = QuestionnaireCategoryRepositoryImpl(FakeDataSource(flowOf(remote)))

        repository.observeActiveCategories().test {
            assertTrue(awaitItem().isFromCache)
            awaitComplete()
        }
    }

    @Test
    fun `firestore failures become content load exceptions`() = runTest {
        val failing = flow<RemoteDocumentList<QuestionnaireCategoryDto>> {
            throw FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }
        val repository = QuestionnaireCategoryRepositoryImpl(FakeDataSource(failing))

        repository.observeActiveCategories().test {
            val error = awaitError()
            assertTrue(error is ContentLoadException)
            assertEquals(ContentLoadError.UNAVAILABLE, (error as ContentLoadException).error)
        }
    }
}
