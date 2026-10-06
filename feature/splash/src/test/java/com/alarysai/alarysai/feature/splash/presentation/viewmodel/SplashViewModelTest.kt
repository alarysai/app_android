package com.alarysai.alarysai.feature.splash.presentation.viewmodel

import app.cash.turbine.test
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.splash.domain.model.SplashTiming
import com.alarysai.alarysai.feature.splash.domain.model.SplashVideo
import com.alarysai.alarysai.feature.splash.domain.repository.SplashVideoRepository
import com.alarysai.alarysai.feature.splash.presentation.action.SplashUiAction
import com.alarysai.alarysai.feature.splash.presentation.event.SplashUiEvent
import com.alarysai.alarysai.feature.splash.presentation.state.SplashUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val timing = SplashTiming(logoDurationMillis = 1_000, maxVideoDurationMillis = 5_000)
    private val video = SplashVideo("asset:///splash/intro.mp4")

    private class FakeRepository(private val video: suspend () -> SplashVideo?) : SplashVideoRepository {
        override suspend fun findIntroVideo() = video()
    }

    private fun viewModel(video: SplashVideo?) = SplashViewModel(FakeRepository { video }, timing)

    private fun test(body: suspend TestScope.() -> Unit) = runTest(dispatcher) { body() }

    @Test
    fun `starts preparing while the video is looked up`() = test {
        val lookup = CompletableDeferred<SplashVideo?>()
        val viewModel = SplashViewModel(FakeRepository { lookup.await() }, timing)
        runCurrent()

        assertEquals(SplashUiState.Preparing, viewModel.uiState.value)
    }

    @Test
    fun `without a video shows the logo and finishes after the logo duration`() = test {
        val viewModel = viewModel(video = null)
        runCurrent()
        assertEquals(SplashUiState.ShowingLogo, viewModel.uiState.value)

        viewModel.events.test {
            advanceTimeBy(timing.logoDurationMillis - 1)
            expectNoEvents()
            advanceTimeBy(2)
            assertEquals(SplashUiEvent.Finished, awaitItem())
        }
    }

    @Test
    fun `with a video plays it and finishes when it ends`() = test {
        val viewModel = viewModel(video)
        runCurrent()
        assertEquals(SplashUiState.PlayingVideo(video.uri), viewModel.uiState.value)

        viewModel.events.test {
            viewModel.onAction(SplashUiAction.VideoEnded)
            assertEquals(SplashUiEvent.Finished, awaitItem())
        }
    }

    @Test
    fun `skip finishes right away`() = test {
        val viewModel = viewModel(video)
        runCurrent()

        viewModel.events.test {
            viewModel.onAction(SplashUiAction.SkipClicked)
            assertEquals(SplashUiEvent.Finished, awaitItem())
        }
    }

    @Test
    fun `a video that cannot play does not block the app`() = test {
        val viewModel = viewModel(video)
        runCurrent()

        viewModel.events.test {
            viewModel.onAction(SplashUiAction.VideoFailed)
            assertEquals(SplashUiEvent.Finished, awaitItem())
        }
    }

    @Test
    fun `a video that never reports its end is cut by the safety timer`() = test {
        val viewModel = viewModel(video)
        runCurrent()

        viewModel.events.test {
            advanceTimeBy(timing.maxVideoDurationMillis - 1)
            expectNoEvents()
            advanceTimeBy(2)
            assertEquals(SplashUiEvent.Finished, awaitItem())
        }
    }

    @Test
    fun `finished is sent only once`() = test {
        val viewModel = viewModel(video)
        runCurrent()

        viewModel.events.test {
            viewModel.onAction(SplashUiAction.SkipClicked)
            viewModel.onAction(SplashUiAction.VideoEnded)
            advanceTimeBy(timing.maxVideoDurationMillis * 2)
            assertEquals(SplashUiEvent.Finished, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `finished is kept until the screen collects it`() = test {
        val viewModel = viewModel(video = null)
        advanceTimeBy(timing.logoDurationMillis + 1)

        viewModel.events.test {
            assertEquals(SplashUiEvent.Finished, awaitItem())
        }
    }
}
