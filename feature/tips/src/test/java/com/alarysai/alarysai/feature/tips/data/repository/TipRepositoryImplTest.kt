package com.alarysai.alarysai.feature.tips.data.repository

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.tips.data.model.TipCategoryDto
import com.alarysai.alarysai.feature.tips.data.model.TipDto
import com.alarysai.alarysai.feature.tips.data.remote.TipsRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TipRepositoryImplTest {

    private class FakeDataSource(
        private val categories: Flow<RemoteDocumentList<TipCategoryDto>> = emptyFlow(),
        private val tips: Flow<RemoteDocumentList<TipDto>> = emptyFlow(),
    ) : TipsRemoteDataSource {
        val requestedCategoryIds = mutableListOf<String?>()

        override fun observeActiveCategories() = categories

        override fun observeActiveTips(categoryId: String?): Flow<RemoteDocumentList<TipDto>> {
            requestedCategoryIds += categoryId
            return tips
        }
    }

    private fun category(id: String, order: Long, status: String = "active") =
        RemoteDocument(id, TipCategoryDto(name = LocalizedTextDto(pt = "Cat $id"), order = order, status = status))

    private fun tip(id: String, order: Long, categoryId: String = "etica", status: String = "active") =
        RemoteDocument(id, TipDto(categoryId = categoryId, text = LocalizedTextDto(pt = "Dica $id"), order = order, status = status))

    @Test
    fun `categories are sorted by order then id and invalid ones dropped`() = runTest {
        val remote = RemoteDocumentList(
            listOf(category("b", 1), category("c", 0), category("a", 1), category("off", 0, status = "inactive")),
            isFromCache = false,
        )
        val repository = TipRepositoryImpl(FakeDataSource(categories = flowOf(remote)))

        repository.observeActiveCategories().test {
            assertEquals(listOf("c", "a", "b"), awaitItem().items.map { it.id })
            awaitComplete()
        }
    }

    @Test
    fun `all tips are requested without a category and keep the cache flag`() = runTest {
        val remote = RemoteDocumentList(
            listOf(tip("b", 1, categoryId = "x"), tip("a", 1, categoryId = "y"), tip("off", 0, status = "inactive")),
            isFromCache = true,
        )
        val dataSource = FakeDataSource(tips = flowOf(remote))

        TipRepositoryImpl(dataSource).observeActiveTips(categoryId = null).test {
            val list = awaitItem()
            assertEquals(listOf("a", "b"), list.items.map { it.id })
            assertTrue(list.isFromCache)
            awaitComplete()
        }
        assertEquals(listOf<String?>(null), dataSource.requestedCategoryIds)
    }

    @Test
    fun `tips of a category drop entries from other categories`() = runTest {
        val remote = RemoteDocumentList(listOf(tip("a", 0), tip("stale", 0, categoryId = "other")), isFromCache = false)
        val dataSource = FakeDataSource(tips = flowOf(remote))

        TipRepositoryImpl(dataSource).observeActiveTips("etica").test {
            assertEquals(listOf("a"), awaitItem().items.map { it.id })
            awaitComplete()
        }
        assertEquals(listOf<String?>("etica"), dataSource.requestedCategoryIds)
    }

    @Test
    fun `firestore failures become content load exceptions`() = runTest {
        val failing = flow<RemoteDocumentList<TipDto>> {
            throw FirebaseFirestoreException("offline", FirebaseFirestoreException.Code.UNAVAILABLE)
        }

        TipRepositoryImpl(FakeDataSource(tips = failing)).observeActiveTips(null).test {
            val error = awaitError()
            assertTrue(error is ContentLoadException)
            assertEquals(ContentLoadError.OFFLINE, (error as ContentLoadException).error)
        }
    }
}
