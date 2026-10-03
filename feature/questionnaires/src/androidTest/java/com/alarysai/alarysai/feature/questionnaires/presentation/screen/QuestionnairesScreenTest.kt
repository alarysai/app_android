package com.alarysai.alarysai.feature.questionnaires.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.ui.state.LOADING_CONTENT_TAG
import com.alarysai.alarysai.feature.questionnaires.presentation.action.QuestionnairesUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnaireItemUi
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnairesContent
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnairesUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class QuestionnairesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(
        content: QuestionnairesContent,
        onAction: (QuestionnairesUiAction) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            AlarysTheme {
                QuestionnairesScreen(
                    uiState = QuestionnairesUiState(categoryName = "Texto", content = content),
                    onAction = onAction,
                    onBack = onBack,
                )
            }
        }
    }

    @Test
    fun loadingShowsTitleAndProgress() {
        setScreen(QuestionnairesContent.Loading)

        composeRule.onNodeWithText("Texto").assertIsDisplayed()
        composeRule.onNodeWithTag(LOADING_CONTENT_TAG).assertIsDisplayed()
    }

    @Test
    fun successShowsQuestionnairesAndThePortugueseBadge() {
        setScreen(
            QuestionnairesContent.Success(
                questionnaires = listOf(
                    QuestionnaireItemUi("1", "Ética na IA", "Use IA com responsabilidade.", null, true, 0),
                    QuestionnaireItemUi("2", "Post para redes", null, null, false, 1),
                ),
                isOffline = false,
            ),
        )

        composeRule.onNodeWithTag(QUESTIONNAIRES_LIST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Ética na IA").assertIsDisplayed()
        composeRule.onNodeWithText("Use IA com responsabilidade.").assertIsDisplayed()
        composeRule.onNodeWithText("Em português").assertIsDisplayed()
    }

    @Test
    fun emptyShowsTheEmptyMessage() {
        setScreen(QuestionnairesContent.Empty(isOffline = false))

        composeRule.onNodeWithText("Nenhum questionário nesta categoria ainda").assertIsDisplayed()
    }

    @Test
    fun errorRetrySendsTheRetryAction() {
        val actions = mutableListOf<QuestionnairesUiAction>()
        setScreen(QuestionnairesContent.Error(ContentLoadError.UNKNOWN), onAction = { actions += it })

        composeRule.onNodeWithText("Tentar de novo").performClick()

        assertEquals(listOf<QuestionnairesUiAction>(QuestionnairesUiAction.Retry), actions)
    }

    @Test
    fun backButtonCallsOnBack() {
        var backClicks = 0
        setScreen(QuestionnairesContent.Loading, onBack = { backClicks++ })

        composeRule.onNodeWithContentDescription("Voltar").performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun tappingAQuestionnaireSendsTheClickAction() {
        val item = QuestionnaireItemUi("1", "Ética na IA", null, null, true, 0)
        val actions = mutableListOf<QuestionnairesUiAction>()
        setScreen(QuestionnairesContent.Success(listOf(item), isOffline = false), onAction = { actions += it })

        composeRule.onNodeWithText("Ética na IA").performClick()

        assertEquals(listOf<QuestionnairesUiAction>(QuestionnairesUiAction.QuestionnaireClicked(item)), actions)
    }
}
