package com.alarysai.alarysai.feature.splash.presentation.action

sealed interface SplashUiAction {
    data object SkipClicked : SplashUiAction

    /** The player reached the end of the video. */
    data object VideoEnded : SplashUiAction

    /** The player could not play the video (bad file, unsupported codec). */
    data object VideoFailed : SplashUiAction
}
