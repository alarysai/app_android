package com.alarysai.alarysai.feature.splash.data.repository

import android.content.res.AssetManager
import com.alarysai.alarysai.feature.splash.domain.model.SplashVideo
import com.alarysai.alarysai.feature.splash.domain.repository.SplashVideoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * The opening video ships inside the APK: dropping `app/src/main/assets/splash/intro.mp4` turns
 * it on, removing the file turns it off. No code change either way.
 */
class AssetSplashVideoRepository internal constructor(
    private val assets: AssetManager,
    private val ioDispatcher: CoroutineDispatcher,
) : SplashVideoRepository {

    @Inject
    constructor(assets: AssetManager) : this(assets, Dispatchers.IO)

    override suspend fun findIntroVideo(): SplashVideo? = withContext(ioDispatcher) {
        val files = try {
            assets.list(VIDEO_FOLDER).orEmpty()
        } catch (_: IOException) {
            emptyArray()
        }
        if (VIDEO_FILE in files) SplashVideo("asset:///$VIDEO_FOLDER/$VIDEO_FILE") else null
    }

    companion object {
        const val VIDEO_FOLDER = "splash"
        const val VIDEO_FILE = "intro.mp4"
    }
}
