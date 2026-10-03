package com.alarysai.alarysai.feature.advertisers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.toContentLoadErrorOrUnknown
import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser
import com.alarysai.alarysai.feature.advertisers.domain.repository.AdvertiserRepository
import com.alarysai.alarysai.feature.advertisers.domain.usecase.GroupAdvertisersByTypeUseCase
import com.alarysai.alarysai.feature.advertisers.presentation.action.AdvertisersUiAction
import com.alarysai.alarysai.feature.advertisers.presentation.event.AdvertisersUiEvent
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserGroupUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserItemUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertisersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class AdvertisersViewModel @Inject constructor(
    private val repository: AdvertiserRepository,
    private val groupByType: GroupAdvertisersByTypeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdvertisersUiState>(AdvertisersUiState.Loading)
    val uiState: StateFlow<AdvertisersUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AdvertisersUiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<AdvertisersUiEvent> = _events.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observeAdvertisers()
    }

    fun onAction(action: AdvertisersUiAction) {
        when (action) {
            is AdvertisersUiAction.AdvertiserClicked -> _events.tryEmit(AdvertisersUiEvent.OpenLink(action.advertiser.link))
            AdvertisersUiAction.Retry -> observeAdvertisers()
        }
    }

    /** A failed listener stops emitting, so retrying means subscribing again. */
    private fun observeAdvertisers() {
        observeJob?.cancel()
        _uiState.value = AdvertisersUiState.Loading
        observeJob = repository.observeActiveAdvertisers()
            .onEach { _uiState.value = it.toUiState() }
            .catch { _uiState.value = AdvertisersUiState.Error(it.toContentLoadErrorOrUnknown()) }
            .launchIn(viewModelScope)
    }

    private fun ContentList<Advertiser>.toUiState(): AdvertisersUiState =
        if (items.isEmpty()) {
            AdvertisersUiState.Empty(isOffline = isFromCache)
        } else {
            AdvertisersUiState.Success(
                groups = groupByType(items).map { group ->
                    AdvertiserGroupUi(type = group.type, advertisers = group.advertisers.map { it.toItemUi() })
                },
                isOffline = isFromCache,
            )
        }

    private fun Advertiser.toItemUi() = AdvertiserItemUi(id = id, name = name, imageUrl = imageUrl, link = link)
}
