package com.alarysai.alarysai.feature.history.data.repository

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.feature.history.data.model.CreditTransactionDto
import com.alarysai.alarysai.feature.history.data.model.HistoryEntryDto
import com.alarysai.alarysai.feature.history.data.model.UserCreditsDto
import com.alarysai.alarysai.feature.history.data.remote.UserActivityRemoteDataSource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class UserActivityRepositoriesTest {

    private class FakeDataSource(
        private val history: Flow<RemoteDocumentList<HistoryEntryDto>> = emptyFlow(),
        private val credits: Flow<RemoteDocument<UserCreditsDto>?> = emptyFlow(),
        private val transactions: Flow<RemoteDocumentList<CreditTransactionDto>> = emptyFlow(),
        private val delete: suspend () -> Unit = {},
    ) : UserActivityRemoteDataSource {
        val deleted = mutableListOf<Pair<String, String>>()

        override fun observeHistory(userId: String) = history
        override suspend fun deleteHistoryEntry(userId: String, entryId: String) {
            delete()
            deleted += userId to entryId
        }
        override fun observeUserCredits(userId: String) = credits
        override fun observeCreditTransactions(userId: String) = transactions
    }

    private fun at(millis: Long) = Timestamp(Date(millis))

    @Test
    fun `history is most recent first, pending timestamps on top`() = runTest {
        val remote = RemoteDocumentList(
            listOf(
                RemoteDocument("old", HistoryEntryDto(createdAt = at(1_000))),
                RemoteDocument("pending", HistoryEntryDto(createdAt = null)),
                RemoteDocument("new", HistoryEntryDto(createdAt = at(2_000))),
            ),
            isFromCache = true,
        )

        HistoryRepositoryImpl(FakeDataSource(history = flowOf(remote))).observeHistory("u1").test {
            val list = awaitItem()
            assertEquals(listOf("pending", "new", "old"), list.items.map { it.id })
            assertTrue(list.isFromCache)
            awaitComplete()
        }
    }

    @Test
    fun `deleting passes the owner and entry, failures become content load exceptions`() = runTest {
        val dataSource = FakeDataSource()
        HistoryRepositoryImpl(dataSource).deleteEntry("u1", "h1")
        assertEquals(listOf("u1" to "h1"), dataSource.deleted)

        val denied = FakeDataSource(delete = {
            throw FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        })
        val error = runCatching { HistoryRepositoryImpl(denied).deleteEntry("u1", "h1") }.exceptionOrNull()
        assertEquals(ContentLoadError.UNAVAILABLE, (error as ContentLoadException).error)
    }

    @Test
    fun `balance counts a missing user document as zero and skips repeated values`() = runTest {
        val credits = flowOf(null, RemoteDocument("u1", UserCreditsDto(40)), RemoteDocument("u1", UserCreditsDto(40)))

        CreditsRepositoryImpl(FakeDataSource(credits = credits)).observeBalance("u1").test {
            assertEquals(0, awaitItem())
            assertEquals(40, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `statement is most recent first and errors become content load exceptions`() = runTest {
        val remote = RemoteDocumentList(
            listOf(
                RemoteDocument("a", CreditTransactionDto(kind = "usage", amount = -3, createdAt = at(1_000))),
                RemoteDocument("b", CreditTransactionDto(kind = "monthly_grant", amount = 40, createdAt = at(2_000))),
            ),
            isFromCache = false,
        )
        CreditsRepositoryImpl(FakeDataSource(transactions = flowOf(remote))).observeTransactions("u1").test {
            assertEquals(listOf("b", "a"), awaitItem().items.map { it.id })
            awaitComplete()
        }

        val failing = flow<RemoteDocumentList<CreditTransactionDto>> {
            throw FirebaseFirestoreException("offline", FirebaseFirestoreException.Code.UNAVAILABLE)
        }
        CreditsRepositoryImpl(FakeDataSource(transactions = failing)).observeTransactions("u1").test {
            assertEquals(ContentLoadError.OFFLINE, (awaitError() as ContentLoadException).error)
        }
    }
}
