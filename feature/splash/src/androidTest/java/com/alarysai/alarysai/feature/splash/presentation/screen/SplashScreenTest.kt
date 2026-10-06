package com.alarysai.alarysai.feature.splash.presentation.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.splash.presentation.action.SplashUiAction
import com.alarysai.alarysai.feature.splash.presentation.state.SplashUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<SplashUiAction>()

    private fun setScreen(uiState: SplashUiState) {
        composeRule.setContent {
            AlarysTheme { SplashScreen(uiState = uiState, onAction = { actions += it }) }
        }
    }

    @Test
    fun logoOpeningHasNoSkipButton() {
        setScreen(SplashUiState.ShowingLogo)

        composeRule.onNodeWithText("Pular").assertDoesNotExist()
    }

    @Test
    fun videoOpeningCanBeSkipped() {
        // The asset does not exist in the test APK: the player may also report VideoFailed.
        setScreen(SplashUiState.PlayingVideo("asset:///splash/missing.mp4"))

        composeRule.onNodeWithText("Pular").performClick()

        assertTrue(SplashUiAction.SkipClicked in actions)
    }
}
