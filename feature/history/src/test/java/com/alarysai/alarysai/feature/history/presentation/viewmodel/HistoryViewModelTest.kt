package com.alarysai.alarysai.feature.history.presentation.viewmodel

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.SessionUser
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.history.domain.model.CreditKind
import com.alarysai.alarysai.feature.history.domain.model.CreditTransaction
import com.alarysai.alarysai.feature.history.domain.model.HistoryEntry
import com.alarysai.alarysai.feature.history.domain.model.OutputType
import com.alarysai.alarysai.feature.history.domain.repository.CreditsRepository
import com.alarysai.alarysai.feature.history.domain.repository.HistoryRepository
import com.alarysai.alarysai.feature.history.presentation.action.HistoryUiAction
import com.alarysai.alarysai.feature.history.presentation.event.HistoryUiEvent
import com.alarysai.alarysai.feature.history.presentation.state.CreditItemUi
import com.alarysai.alarysai.feature.history.presentation.state.HistoryItemUi
import com.alarysai.alarysai.feature.history.presentation.state.HistoryTab
import com.alarysai.alarysai.feature.history.presentation.state.HistoryUiState
import com.alarysai.alarysai.feature.history.presentation.state.ListContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val entry = HistoryEntry("h1", "Ética na IA", OutputType.TEXT, "Um texto.", "https://x/r.pdf", 3, 2_000)
    private val entryUi = HistoryItemUi("h1", "Ética na IA", OutputType.TEXT, "Um texto.", "https://x/r.pdf", 3, 2_000)
    private val usage = CreditTransaction("c1", CreditKind.USAGE, -3, 37, 2_000)

    private class FakeSession(initial: String?) : SessionRepository {
        val userId = MutableStateFlow(initial)
        override fun observeUserId(): Flow<String?> = userId
        override fun observeUser(): Flow<SessionUser?> = userId.map { uid -> uid?.let { SessionUser(it, null, null) } }
    }

    /** Buffered so tryEmit succeeds while the ViewModel is subscribed. */
    private fun <T> updates() = MutableSharedFlow<T>(extraBufferCapacity = 8)

    private class FakeHistory(
        private val flows: ArrayDeque<Flow<ContentList<HistoryEntry>>>,
        private val delete: suspend () -> Unit = {},
    ) : HistoryRepository {
        val observedUsers = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        override fun observeHistory(userId: String): Flow<ContentList<HistoryEntry>> {
            observedUsers += userId
            return flows.removeFirst()
        }

        override suspend fun deleteEntry(userId: String, entryId: String) {
            delete()
            deleted += entryId
        }
    }

    private class FakeCredits(
        private val balance: Flow<Int>,
        private val transactions: Flow<ContentList<CreditTransaction>>,
    ) : CreditsRepository {
        override fun observeBalance(userId: String) = balance
        override fun observeTransactions(userId: String) = transactions
    }

    private val history = updates<ContentList<HistoryEntry>>()
    private val balance = updates<Int>()
    private val transactions = updates<ContentList<CreditTransaction>>()

    private fun viewModel(
        session: SessionRepository = FakeSession("u1"),
        historyRepository: HistoryRepository = FakeHistory(ArrayDeque(listOf(history))),
        creditsRepository: CreditsRepository = FakeCredits(balance, transactions),
    ) = HistoryViewModel(session, historyRepository, creditsRepository)

    private fun HistoryViewModel.signedIn() = uiState.value as HistoryUiState.SignedIn

    @Test
    fun `signed out shows the sign in message and queries nothing`() {
        val historyRepository = FakeHistory(ArrayDeque())
        val viewModel = viewModel(session = FakeSession(null), historyRepository = historyRepository)

        assertEquals(HistoryUiState.SignedOut, viewModel.uiState.value)
        assertEquals(emptyList<String>(), historyRepository.observedUsers)
    }

    @Test
    fun `signed in shows balance, generations and statement of that user`() {
        val historyRepository = FakeHistory(ArrayDeque(listOf(history)))
        val viewModel = viewModel(historyRepository = historyRepository)
        assertEquals(HistoryUiState.SignedIn(), viewModel.uiState.value)

        balance.tryEmit(37)
        history.tryEmit(ContentList(listOf(entry), isFromCache = false))
        transactions.tryEmit(ContentList(listOf(usage), isFromCache = true))

        assertEquals(listOf("u1"), historyRepository.observedUsers)
        assertEquals(
            HistoryUiState.SignedIn(
                balance = 37,
                generations = ListContent.Success(listOf(entryUi), isOffline = false),
                statement = ListContent.Success(listOf(CreditItemUi("c1", CreditKind.USAGE, -3, 37, 2_000)), isOffline = true),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `empty lists and failures are shown per tab`() {
        val failingTransactions = flow<ContentList<CreditTransaction>> { throw ContentLoadException(ContentLoadError.OFFLINE) }
        val viewModel = viewModel(creditsRepository = FakeCredits(balance, failingTransactions))

        history.tryEmit(ContentList(emptyList(), isFromCache = false))

        assertEquals(ListContent.Empty(isOffline = false), viewModel.signedIn().generations)
        assertEquals(ListContent.Error(ContentLoadError.OFFLINE), viewModel.signedIn().statement)
    }

    @Test
    fun `switching tabs keeps the data`() {
        val viewModel = viewModel()
        history.tryEmit(ContentList(listOf(entry), isFromCache = false))

        viewModel.onAction(HistoryUiAction.TabSelected(HistoryTab.STATEMENT))

        assertEquals(HistoryTab.STATEMENT, viewModel.signedIn().selectedTab)
        assertEquals(ListContent.Success(listOf(entryUi), isOffline = false), viewModel.signedIn().generations)
    }

    @Test
    fun `deleting asks for confirmation, dismissing keeps the entry`() {
        val historyRepository = FakeHistory(ArrayDeque(listOf(history)))
        val viewModel = viewModel(historyRepository = historyRepository)

        viewModel.onAction(HistoryUiAction.DeleteClicked(entryUi))
        assertEquals(entryUi, viewModel.signedIn().pendingDeletion)

        viewModel.onAction(HistoryUiAction.DeleteDismissed)
        assertEquals(null, viewModel.signedIn().pendingDeletion)
        assertEquals(emptyList<String>(), historyRepository.deleted)
    }

    @Test
    fun `confirming deletes the entry`() {
        val historyRepository = FakeHistory(ArrayDeque(listOf(history)))
        val viewModel = viewModel(historyRepository = historyRepository)

        viewModel.onAction(HistoryUiAction.DeleteClicked(entryUi))
        viewModel.onAction(HistoryUiAction.DeleteConfirmed)

        assertEquals(listOf("h1"), historyRepository.deleted)
        assertEquals(null, viewModel.signedIn().pendingDeletion)
    }

    @Test
    fun `a failed deletion is reported`() = runTest {
        val failing = FakeHistory(ArrayDeque(listOf(history)), delete = { throw ContentLoadException(ContentLoadError.UNKNOWN) })
        val viewModel = viewModel(historyRepository = failing)

        viewModel.events.test {
            viewModel.onAction(HistoryUiAction.DeleteClicked(entryUi))
            viewModel.onAction(HistoryUiAction.DeleteConfirmed)
            assertEquals(HistoryUiEvent.DeleteFailed, awaitItem())
        }
    }

    @Test
    fun `opening a result emits its link, entries without link do nothing`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(HistoryUiAction.OpenResultClicked(entryUi.copy(resultUrl = null)))
            expectNoEvents()
            viewModel.onAction(HistoryUiAction.OpenResultClicked(entryUi))
            assertEquals(HistoryUiEvent.OpenResult("https://x/r.pdf"), awaitItem())
        }
    }

    @Test
    fun `signing out stops showing the user data`() {
        val session = FakeSession("u1")
        val viewModel = viewModel(session = session)
        history.tryEmit(ContentList(listOf(entry), isFromCache = false))

        session.userId.value = null

        assertEquals(HistoryUiState.SignedOut, viewModel.uiState.value)
    }

    @Test
    fun `retry subscribes again for the same user`() {
        val failing = flow<ContentList<HistoryEntry>> { throw ContentLoadException(ContentLoadError.UNKNOWN) }
        val recovered = updates<ContentList<HistoryEntry>>()
        val historyRepository = FakeHistory(ArrayDeque(listOf(failing, recovered)))
        val viewModel = viewModel(historyRepository = historyRepository)
        assertEquals(ListContent.Error(ContentLoadError.UNKNOWN), viewModel.signedIn().generations)

        viewModel.onAction(HistoryUiAction.Retry)
        recovered.tryEmit(ContentList(listOf(entry), isFromCache = false))

        assertEquals(listOf("u1", "u1"), historyRepository.observedUsers)
        assertEquals(ListContent.Success(listOf(entryUi), isOffline = false), viewModel.signedIn().generations)
    }
}
