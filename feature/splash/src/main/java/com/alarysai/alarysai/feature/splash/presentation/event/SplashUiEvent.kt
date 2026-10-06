package com.alarysai.alarysai.feature.splash.presentation.event

sealed interface SplashUiEvent {
    /** The opening is over: show the app (login or home). Sent once. */
    data object Finished : SplashUiEvent
}
