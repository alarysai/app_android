package com.alarysai.alarysai.feature.advertisers.data.repository

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.feature.advertisers.data.model.AdvertiserDto
import com.alarysai.alarysai.feature.advertisers.data.remote.AdvertisersRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvertiserRepositoryImplTest {

    private class FakeDataSource(private val flow: Flow<RemoteDocumentList<AdvertiserDto>>) : AdvertisersRemoteDataSource {
        override fun observeActiveAdvertisers() = flow
    }

    private fun advertiser(id: String, order: Long, status: String = "active", link: String = "https://x/$id") =
        RemoteDocument(id, AdvertiserDto(name = "A $id", type = "Parceiro", link = link, order = order, status = status))

    @Test
    fun `sorts by order then id and drops what cannot be shown`() = runTest {
        val remote = RemoteDocumentList(
            listOf(
                advertiser("b", 1),
                advertiser("c", 0),
                advertiser("a", 1),
                advertiser("off", 0, status = "inactive"),
                advertiser("unsafe", 0, link = "http://x"),
            ),
            isFromCache = true,
        )

        AdvertiserRepositoryImpl(FakeDataSource(flowOf(remote))).observeActiveAdvertisers().test {
            val list = awaitItem()
            assertEquals(listOf("c", "a", "b"), list.items.map { it.id })
            assertTrue(list.isFromCache)
            awaitComplete()
        }
    }

    @Test
    fun `firestore failures become content load exceptions`() = runTest {
        val failing = flow<RemoteDocumentList<AdvertiserDto>> {
            throw FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }

        AdvertiserRepositoryImpl(FakeDataSource(failing)).observeActiveAdvertisers().test {
            val error = awaitError()
            assertTrue(error is ContentLoadException)
            assertEquals(ContentLoadError.UNAVAILABLE, (error as ContentLoadException).error)
        }
    }
}
