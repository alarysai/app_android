package com.alarysai.alarysai.feature.splash.domain.repository

import com.alarysai.alarysai.feature.splash.domain.model.SplashVideo

interface SplashVideoRepository {
    /** The opening video, or null when there is none (the splash then shows the logo). */
    suspend fun findIntroVideo(): SplashVideo?
}
