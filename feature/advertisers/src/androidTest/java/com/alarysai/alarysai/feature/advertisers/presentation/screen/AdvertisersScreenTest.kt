package com.alarysai.alarysai.feature.advertisers.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.advertisers.presentation.action.AdvertisersUiAction
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserGroupUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserItemUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertisersUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class AdvertisersScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<AdvertisersUiAction>()

    private fun setScreen(state: AdvertisersUiState) {
        composeRule.setContent {
            AlarysTheme { AdvertisersScreen(uiState = state, onAction = { actions += it }) }
        }
    }

    private val shop = AdvertiserItemUi("a1", "Loja X", null, "https://lojax.com.br/promo")
    private val imageOnly = AdvertiserItemUi("a2", null, null, "https://escolay.com.br/curso")

    @Test
    fun showsGroupsWithTitlesAndTheHostForImageOnlyAdvertisers() {
        setScreen(
            AdvertisersUiState.Success(
                groups = listOf(AdvertiserGroupUi("Parceiro", listOf(shop)), AdvertiserGroupUi(null, listOf(imageOnly))),
                isOffline = false,
            ),
        )

        composeRule.onNodeWithText("Parceiro").assertIsDisplayed()
        composeRule.onNodeWithText("Loja X").assertIsDisplayed()
        composeRule.onNodeWithText("Outros").assertIsDisplayed()
        composeRule.onNodeWithText("escolay.com.br").assertIsDisplayed()
    }

    @Test
    fun tappingAnAdvertiserSendsTheClick() {
        setScreen(AdvertisersUiState.Success(listOf(AdvertiserGroupUi("Parceiro", listOf(shop))), isOffline = false))

        composeRule.onNodeWithText("Loja X").performClick()

        assertEquals(listOf<AdvertisersUiAction>(AdvertisersUiAction.AdvertiserClicked(shop)), actions)
    }

    @Test
    fun errorRetrySendsRetry() {
        setScreen(AdvertisersUiState.Error(ContentLoadError.UNKNOWN))

        composeRule.onNodeWithText("Tentar de novo").performClick()

        assertEquals(listOf<AdvertisersUiAction>(AdvertisersUiAction.Retry), actions)
    }

    @Test
    fun emptyShowsTheMessage() {
        setScreen(AdvertisersUiState.Empty(isOffline = false))

        composeRule.onNodeWithText("Nenhum anunciante por enquanto").assertIsDisplayed()
    }
}
