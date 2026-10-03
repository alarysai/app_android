package com.alarysai.alarysai.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.toContentLoadErrorOrUnknown
import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.feature.history.domain.model.CreditTransaction
import com.alarysai.alarysai.feature.history.domain.model.HistoryEntry
import com.alarysai.alarysai.feature.history.domain.repository.CreditsRepository
import com.alarysai.alarysai.feature.history.domain.repository.HistoryRepository
import com.alarysai.alarysai.feature.history.presentation.action.HistoryUiAction
import com.alarysai.alarysai.feature.history.presentation.event.HistoryUiEvent
import com.alarysai.alarysai.feature.history.presentation.state.CreditItemUi
import com.alarysai.alarysai.feature.history.presentation.state.HistoryItemUi
import com.alarysai.alarysai.feature.history.presentation.state.HistoryTab
import com.alarysai.alarysai.feature.history.presentation.state.HistoryUiState
import com.alarysai.alarysai.feature.history.presentation.state.ListContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * History, statement and balance of the signed-in user. Follows the session: signing out stops
 * every query and shows the signed-out message; signing in (Task 9) starts them.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val historyRepository: HistoryRepository,
    private val creditsRepository: CreditsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HistoryUiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<HistoryUiEvent> = _events.asSharedFlow()

    private var userId: String? = null
    private val userJobs = mutableListOf<Job>()

    init {
        sessionRepository.observeUserId()
            .onEach(::onUserChanged)
            .launchIn(viewModelScope)
    }

    fun onAction(action: HistoryUiAction) {
        when (action) {
            is HistoryUiAction.TabSelected -> updateSignedIn { it.copy(selectedTab = action.tab) }
            is HistoryUiAction.OpenResultClicked -> action.item.resultUrl?.let { _events.tryEmit(HistoryUiEvent.OpenResult(it)) }
            is HistoryUiAction.DeleteClicked -> updateSignedIn { it.copy(pendingDeletion = action.item) }
            HistoryUiAction.DeleteDismissed -> updateSignedIn { it.copy(pendingDeletion = null) }
            HistoryUiAction.DeleteConfirmed -> deletePending()
            HistoryUiAction.Retry -> userId?.let(::observeUser)
        }
    }

    private fun onUserChanged(newUserId: String?) {
        userId = newUserId
        if (newUserId == null) {
            stopUserJobs()
            _uiState.value = HistoryUiState.SignedOut
        } else {
            observeUser(newUserId)
        }
    }

    /** A failed listener stops emitting, so retrying means subscribing again. */
    private fun observeUser(userId: String) {
        stopUserJobs()
        val selectedTab = (_uiState.value as? HistoryUiState.SignedIn)?.selectedTab ?: HistoryTab.GENERATIONS
        _uiState.value = HistoryUiState.SignedIn(selectedTab = selectedTab)
        userJobs += creditsRepository.observeBalance(userId)
            .onEach { balance -> updateSignedIn { it.copy(balance = balance) } }
            .catch { updateSignedIn { it.copy(balance = null) } }
            .launchIn(viewModelScope)
        userJobs += historyRepository.observeHistory(userId)
            .onEach { list -> updateSignedIn { it.copy(generations = list.toContent { entry -> entry.toItemUi() }) } }
            .catch { error -> updateSignedIn { it.copy(generations = ListContent.Error(error.toContentLoadErrorOrUnknown())) } }
            .launchIn(viewModelScope)
        userJobs += creditsRepository.observeTransactions(userId)
            .onEach { list -> updateSignedIn { it.copy(statement = list.toContent { transaction -> transaction.toItemUi() }) } }
            .catch { error -> updateSignedIn { it.copy(statement = ListContent.Error(error.toContentLoadErrorOrUnknown())) } }
            .launchIn(viewModelScope)
    }

    /** The live history query removes the entry from the list on its own once it is deleted. */
    private fun deletePending() {
        val state = _uiState.value as? HistoryUiState.SignedIn ?: return
        val entry = state.pendingDeletion ?: return
        val owner = userId ?: return
        updateSignedIn { it.copy(pendingDeletion = null) }
        viewModelScope.launch {
            try {
                historyRepository.deleteEntry(owner, entry.id)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _events.tryEmit(HistoryUiEvent.DeleteFailed)
            }
        }
    }

    private fun stopUserJobs() {
        userJobs.forEach { it.cancel() }
        userJobs.clear()
    }

    private fun updateSignedIn(transform: (HistoryUiState.SignedIn) -> HistoryUiState.SignedIn) {
        _uiState.update { state -> if (state is HistoryUiState.SignedIn) transform(state) else state }
    }

    private fun <T, R> ContentList<T>.toContent(transform: (T) -> R): ListContent<R> =
        if (items.isEmpty()) {
            ListContent.Empty(isOffline = isFromCache)
        } else {
            ListContent.Success(items.map(transform), isOffline = isFromCache)
        }

    private fun HistoryEntry.toItemUi() = HistoryItemUi(
        id = id,
        title = questionnaireTitle,
        outputType = outputType,
        resultText = resultText,
        resultUrl = resultUrl,
        creditsSpent = creditsSpent,
        createdAtMillis = createdAtMillis,
    )

    private fun CreditTransaction.toItemUi() = CreditItemUi(
        id = id,
        kind = kind,
        amount = amount,
        balanceAfter = balanceAfter,
        createdAtMillis = createdAtMillis,
    )
}
