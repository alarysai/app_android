package com.alarysai.alarysai.feature.home.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.home.presentation.action.HomeUiAction
import com.alarysai.alarysai.feature.home.presentation.components.CATEGORY_GRID_TAG
import com.alarysai.alarysai.feature.home.presentation.components.CHAT_SEND_TAG
import com.alarysai.alarysai.feature.home.presentation.components.HOME_SEARCH_TAG
import com.alarysai.alarysai.feature.home.presentation.state.CategoriesSection
import com.alarysai.alarysai.feature.home.presentation.state.CategoryItemUi
import com.alarysai.alarysai.feature.home.presentation.state.HomeUiState
import com.alarysai.alarysai.feature.home.presentation.state.PlanUsageUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val categories = CategoriesSection.Success(
        categories = listOf(CategoryItemUi("1", "Imagem", null, 0), CategoryItemUi("2", "Texto", null, 1)),
        isOffline = false,
    )

    private fun setScreen(state: HomeUiState, onAction: (HomeUiAction) -> Unit = {}) {
        composeRule.setContent {
            AlarysTheme { HomeScreen(uiState = state, onAction = onAction) }
        }
    }

    @Test
    fun showsGreetingWithoutNameAndExplainsCreditsWithoutPlan() {
        setScreen(HomeUiState(categories = categories))

        composeRule.onNodeWithText("Olá!").assertIsDisplayed()
        composeRule.onNodeWithText("Seus créditos aparecem aqui quando o seu plano estiver ativo.").assertIsDisplayed()
    }

    @Test
    fun showsNameAndCreditsWhenKnown() {
        setScreen(
            HomeUiState(
                userName = "Diego",
                planUsage = PlanUsageUi(usedPercent = 60, availableCredits = 40),
                categories = categories,
            ),
        )

        composeRule.onNodeWithText("Olá, Diego").assertIsDisplayed()
        composeRule.onNodeWithText("40 créditos").assertIsDisplayed()
        composeRule.onNodeWithText("60% utilizado").assertIsDisplayed()
        composeRule.onNodeWithText("40% disponível").assertIsDisplayed()
    }

    @Test
    fun showsEveryCategoryCard() {
        setScreen(HomeUiState(categories = categories))

        composeRule.onNodeWithTag(CATEGORY_GRID_TAG).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Imagem").assertIsDisplayed()
        composeRule.onNodeWithText("Texto").assertIsDisplayed()
    }

    @Test
    fun typingInTheSearchSendsTheQuery() {
        val actions = mutableListOf<HomeUiAction>()
        setScreen(HomeUiState(categories = categories)) { actions += it }

        composeRule.onNodeWithTag(HOME_SEARCH_TAG).performTextInput("vid")

        assertTrue(actions.contains(HomeUiAction.SearchQueryChanged("vid")))
    }

    @Test
    fun searchWithoutResultsShowsTheMessage() {
        setScreen(HomeUiState(searchQuery = "música", categories = CategoriesSection.NoSearchResults("música")))

        composeRule.onNodeWithText("Nenhum recurso encontrado para \"música\"").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun categoriesErrorRetrySendsTheRetryAction() {
        val actions = mutableListOf<HomeUiAction>()
        setScreen(HomeUiState(categories = CategoriesSection.Error(ContentLoadError.UNKNOWN))) { actions += it }

        composeRule.onNodeWithText("Tentar de novo").performScrollTo().performClick()

        assertEquals(listOf<HomeUiAction>(HomeUiAction.RetryCategories), actions)
    }

    @Test
    fun chatSendButtonSendsTheAction() {
        val actions = mutableListOf<HomeUiAction>()
        setScreen(HomeUiState(categories = categories, chatMessage = "Oi")) { actions += it }

        composeRule.onNodeWithTag(CHAT_SEND_TAG).performScrollTo().performClick()

        assertEquals(listOf<HomeUiAction>(HomeUiAction.ChatSendClicked), actions)
    }

    @Test
    fun tappingACategorySendsTheClickAction() {
        val actions = mutableListOf<HomeUiAction>()
        setScreen(HomeUiState(categories = categories)) { actions += it }

        composeRule.onNodeWithText("Texto").performScrollTo().performClick()

        assertEquals(listOf<HomeUiAction>(HomeUiAction.CategoryClicked(categories.categories[1])), actions)
    }
}
