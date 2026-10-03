package com.alarysai.alarysai.feature.history.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.history.domain.model.CreditKind
import com.alarysai.alarysai.feature.history.domain.model.OutputType
import com.alarysai.alarysai.feature.history.presentation.action.HistoryUiAction
import com.alarysai.alarysai.feature.history.presentation.state.CreditItemUi
import com.alarysai.alarysai.feature.history.presentation.state.HistoryItemUi
import com.alarysai.alarysai.feature.history.presentation.state.HistoryTab
import com.alarysai.alarysai.feature.history.presentation.state.HistoryUiState
import com.alarysai.alarysai.feature.history.presentation.state.ListContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<HistoryUiAction>()
    private val entry = HistoryItemUi("h1", "Ética na IA", OutputType.TEXT, "Um texto curto.", "https://x/r.pdf", 3, null)

    private fun setScreen(state: HistoryUiState) {
        composeRule.setContent {
            AlarysTheme { HistoryScreen(uiState = state, onAction = { actions += it }) }
        }
    }

    @Test
    fun signedOutAsksToSignIn() {
        setScreen(HistoryUiState.SignedOut)

        composeRule.onNodeWithText("Entre na sua conta").assertIsDisplayed()
    }

    @Test
    fun generationsShowBalanceEntryAndActions() {
        setScreen(HistoryUiState.SignedIn(balance = 37, generations = ListContent.Success(listOf(entry), isOffline = false)))

        composeRule.onNodeWithText("37 créditos").assertIsDisplayed()
        composeRule.onNodeWithText("Ética na IA").assertIsDisplayed()
        composeRule.onNodeWithText("Um texto curto.").assertIsDisplayed()
        composeRule.onNodeWithText("Abrir resultado").performClick()
        composeRule.onNodeWithContentDescription("Apagar").performClick()

        assertEquals(listOf(HistoryUiAction.OpenResultClicked(entry), HistoryUiAction.DeleteClicked(entry)), actions)
    }

    @Test
    fun deletionDialogConfirmsOrCancels() {
        setScreen(HistoryUiState.SignedIn(generations = ListContent.Success(listOf(entry), false), pendingDeletion = entry))

        composeRule.onNodeWithText("Apagar do histórico?").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelar").performClick()

        assertEquals(listOf<HistoryUiAction>(HistoryUiAction.DeleteDismissed), actions)
    }

    @Test
    fun statementShowsSignedAmountsAndSwitchesTabs() {
        setScreen(
            HistoryUiState.SignedIn(
                selectedTab = HistoryTab.STATEMENT,
                statement = ListContent.Success(listOf(CreditItemUi("c1", CreditKind.USAGE, -3, 37, null)), false),
            ),
        )

        composeRule.onNodeWithText("Uso").assertIsDisplayed()
        composeRule.onNodeWithText("-3").assertIsDisplayed()
        composeRule.onNodeWithText("Saldo: 37").assertIsDisplayed()
        composeRule.onNodeWithText("Gerações").performClick()

        assertEquals(listOf<HistoryUiAction>(HistoryUiAction.TabSelected(HistoryTab.GENERATIONS)), actions)
    }
}
