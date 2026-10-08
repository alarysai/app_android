package com.alarysai.alarysai.feature.tips.presentation.screen

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.tips.presentation.action.TipsUiAction
import com.alarysai.alarysai.feature.tips.presentation.components.TIPS_CAROUSEL_DOTS_TAG
import com.alarysai.alarysai.feature.tips.presentation.components.TIPS_CAROUSEL_TAG
import com.alarysai.alarysai.feature.tips.presentation.components.TIP_AUTO_ADVANCE_MILLIS
import com.alarysai.alarysai.feature.tips.presentation.state.TipItemUi
import com.alarysai.alarysai.feature.tips.presentation.state.TipsContent
import com.alarysai.alarysai.feature.tips.presentation.state.TipsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class TipsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<TipsUiAction>()

    private val tips = listOf(
        TipItemUi("t1", "Cite as fontes.", null, categoryName = "Ética", isInUserLanguage = false),
        TipItemUi("t2", "Peça exemplos.", null, categoryName = "Conhecimento", isInUserLanguage = true),
        TipItemUi("t3", "Revise antes de publicar.", null, categoryName = null, isInUserLanguage = true),
    )

    private fun setScreen(state: TipsUiState) {
        composeRule.setContent {
            AlarysTheme { TipsScreen(uiState = state, onAction = { actions += it }) }
        }
    }

    private fun success(list: List<TipItemUi> = tips) = TipsUiState(TipsContent.Success(list, isOffline = false))

    private fun assertPosition(position: Int) {
        composeRule.onNodeWithTag(TIPS_CAROUSEL_DOTS_TAG).assertContentDescriptionEquals("Dica $position de ${tips.size}")
    }

    @Test
    fun showsTheFirstTipWithCategoryBadgeAndDots() {
        setScreen(success())

        composeRule.onNodeWithText("Cite as fontes.").assertIsDisplayed()
        composeRule.onNodeWithText("Ética").assertIsDisplayed()
        composeRule.onNodeWithText("Em português").assertIsDisplayed()
        assertPosition(1)
    }

    @Test
    fun movesToTheNextTipAfterFiveSecondsAndLoopsBackToTheFirst() {
        composeRule.mainClock.autoAdvance = false
        setScreen(success())

        repeat(tips.size) {
            composeRule.mainClock.advanceTimeBy(TIP_AUTO_ADVANCE_MILLIS + 1_000)
        }
        composeRule.mainClock.autoAdvance = true

        assertPosition(1)
        composeRule.onNodeWithText("Cite as fontes.").assertIsDisplayed()
    }

    @Test
    fun theUserSwipesBothWaysEndlessly() {
        setScreen(success())

        composeRule.onNodeWithTag(TIPS_CAROUSEL_TAG).performTouchInput { swipeLeft() }
        assertPosition(2)
        composeRule.onNodeWithTag(TIPS_CAROUSEL_TAG).performTouchInput { swipeRight() }
        composeRule.onNodeWithTag(TIPS_CAROUSEL_TAG).performTouchInput { swipeRight() }
        assertPosition(3)
    }

    @Test
    fun aSingleTipHasNoDots() {
        setScreen(success(tips.take(1)))

        composeRule.onNodeWithText("Cite as fontes.").assertIsDisplayed()
        composeRule.onNodeWithTag(TIPS_CAROUSEL_DOTS_TAG).assertDoesNotExist()
    }

    @Test
    fun noTipsTakesNoSpace() {
        setScreen(TipsUiState(TipsContent.Empty(isOffline = false)))

        composeRule.onNodeWithTag(TIPS_CAROUSEL_TAG).assertDoesNotExist()
    }

    @Test
    fun errorRetrySendsRetry() {
        setScreen(TipsUiState(TipsContent.Error(ContentLoadError.UNKNOWN)))

        composeRule.onNodeWithText("Tentar de novo").performClick()

        assertEquals(listOf<TipsUiAction>(TipsUiAction.Retry), actions)
    }
}
