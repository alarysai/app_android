package com.alarysai.alarysai.feature.tips.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.tips.presentation.action.TipsUiAction
import com.alarysai.alarysai.feature.tips.presentation.components.TIP_CATEGORY_CHIPS_TAG
import com.alarysai.alarysai.feature.tips.presentation.state.TipCategoryUi
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

    private fun setScreen(state: TipsUiState) {
        composeRule.setContent {
            AlarysTheme { TipsScreen(uiState = state, onAction = { actions += it }) }
        }
    }

    @Test
    fun showsChipsAndTipsWithCategoryAndBadge() {
        setScreen(
            TipsUiState(
                categories = listOf(TipCategoryUi("etica", "Ética")),
                content = TipsContent.Success(
                    tips = listOf(TipItemUi("t1", "Cite as fontes.", null, categoryName = "Conhecimento", isInUserLanguage = false)),
                    isOffline = false,
                ),
            ),
        )

        composeRule.onNodeWithText("Todas").assertIsDisplayed()
        composeRule.onNodeWithText("Cite as fontes.").assertIsDisplayed()
        composeRule.onNodeWithText("Conhecimento").assertIsDisplayed()
        composeRule.onNodeWithText("Em português").assertIsDisplayed()
    }

    @Test
    fun tappingAChipSelectsTheCategory() {
        setScreen(TipsUiState(categories = listOf(TipCategoryUi("etica", "Ética")), content = TipsContent.Loading))

        composeRule.onNodeWithText("Ética").performClick()
        composeRule.onNodeWithText("Todas").performClick()

        assertEquals(listOf(TipsUiAction.CategorySelected("etica"), TipsUiAction.CategorySelected(null)), actions)
    }

    @Test
    fun noCategoriesHidesTheChipRow() {
        setScreen(TipsUiState(content = TipsContent.Empty(isOffline = false)))

        composeRule.onNodeWithTag(TIP_CATEGORY_CHIPS_TAG).assertDoesNotExist()
        composeRule.onNodeWithText("Nenhuma dica por aqui ainda").assertIsDisplayed()
    }

    @Test
    fun errorRetrySendsRetry() {
        setScreen(TipsUiState(content = TipsContent.Error(ContentLoadError.UNKNOWN)))

        composeRule.onNodeWithText("Tentar de novo").performClick()

        assertEquals(listOf<TipsUiAction>(TipsUiAction.Retry), actions)
    }
}
