package com.alarysai.alarysai.feature.splash.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.feature.splash.domain.model.SplashTiming
import com.alarysai.alarysai.feature.splash.domain.repository.SplashVideoRepository
import com.alarysai.alarysai.feature.splash.presentation.action.SplashUiAction
import com.alarysai.alarysai.feature.splash.presentation.event.SplashUiEvent
import com.alarysai.alarysai.feature.splash.presentation.state.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The opening: plays the video when there is one, otherwise shows the logo for a moment, then
 * sends [SplashUiEvent.Finished] exactly once. A timer guarantees the user is never stuck here.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val videoRepository: SplashVideoRepository,
    private val timing: SplashTiming,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Preparing)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    // A channel keeps Finished until the screen collects it: losing it would freeze the app here.
    private val _events = Channel<SplashUiEvent>(Channel.BUFFERED)
    val events: Flow<SplashUiEvent> = _events.receiveAsFlow()

    private var isFinished = false
    private val openingJob: Job = viewModelScope.launch { runOpening() }

    fun onAction(action: SplashUiAction) {
        when (action) {
            SplashUiAction.SkipClicked,
            SplashUiAction.VideoEnded,
            SplashUiAction.VideoFailed,
            -> finish()
        }
    }

    private suspend fun runOpening() {
        val video = videoRepository.findIntroVideo()
        if (video == null) {
            _uiState.value = SplashUiState.ShowingLogo
            delay(timing.logoDurationMillis)
        } else {
            _uiState.value = SplashUiState.PlayingVideo(video.uri)
            delay(timing.maxVideoDurationMillis)
        }
        finish()
    }

    private fun finish() {
        if (isFinished) return
        isFinished = true
        openingJob.cancel()
        _events.trySend(SplashUiEvent.Finished)
    }
}
