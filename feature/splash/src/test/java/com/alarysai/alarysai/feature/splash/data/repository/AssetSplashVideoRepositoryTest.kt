package com.alarysai.alarysai.feature.splash.data.repository

import android.content.res.AssetManager
import com.alarysai.alarysai.feature.splash.domain.model.SplashVideo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class AssetSplashVideoRepositoryTest {

    private val dispatcher = StandardTestDispatcher()
    private val assets = mockk<AssetManager>()

    private fun repository() = AssetSplashVideoRepository(assets, dispatcher)

    @Test
    fun `finds the intro video when the file is in the assets`() = runTest(dispatcher) {
        every { assets.list("splash") } returns arrayOf("readme.txt", "intro.mp4")

        assertEquals(SplashVideo("asset:///splash/intro.mp4"), repository().findIntroVideo())
    }

    @Test
    fun `no file means no video`() = runTest(dispatcher) {
        every { assets.list("splash") } returns arrayOf("other.mp4")

        assertNull(repository().findIntroVideo())
    }

    @Test
    fun `missing folder means no video`() = runTest(dispatcher) {
        every { assets.list("splash") } returns null

        assertNull(repository().findIntroVideo())
    }

    @Test
    fun `unreadable assets mean no video`() = runTest(dispatcher) {
        every { assets.list("splash") } throws IOException("broken")

        assertNull(repository().findIntroVideo())
    }
}
