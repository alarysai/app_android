package com.alarysai.alarysai.feature.splash.presentation.state

sealed interface SplashUiState {
    /** Looking for the video; only the brand background shows (a few milliseconds). */
    data object Preparing : SplashUiState

    /** No video: the animated logo. */
    data object ShowingLogo : SplashUiState

    /** The opening video, full screen, with a "Skip" button. */
    data class PlayingVideo(val uri: String) : SplashUiState
}
