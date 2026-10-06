package com.alarysai.alarysai.feature.splash.domain.model

/**
 * How long the opening may hold the user.
 *
 * @property logoDurationMillis without a video, the animated logo stays this long.
 * @property maxVideoDurationMillis safety net: the app opens after this even if the video has
 * not reported its end (stalled decoder, very long file).
 */
data class SplashTiming(
    val logoDurationMillis: Long = 1_800,
    val maxVideoDurationMillis: Long = 15_000,
)
