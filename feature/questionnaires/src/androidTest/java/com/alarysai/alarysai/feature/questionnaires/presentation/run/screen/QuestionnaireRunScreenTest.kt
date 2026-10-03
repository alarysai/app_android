package com.alarysai.alarysai.feature.questionnaires.presentation.run.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.presentation.run.action.QuestionnaireRunUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.OPEN_TEXT_FIELD_TAG
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.DraftUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunContent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunUiState
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewAnswerUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewItemUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.RunProgressUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepOptionUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.TipCardUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class QuestionnaireRunScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<QuestionnaireRunUiAction>()

    private fun setScreen(content: QuestionnaireRunContent, exitDialog: Boolean = false) {
        composeRule.setContent {
            AlarysTheme {
                QuestionnaireRunScreen(
                    uiState = QuestionnaireRunUiState(title = "Criar imagem", content = content, isExitConfirmationVisible = exitDialog),
                    onAction = { actions += it },
                )
            }
        }
    }

    private fun question(
        answerType: AnswerType,
        options: List<StepOptionUi> = listOf(StepOptionUi("post", "Post para redes sociais", null), StepOptionUi("logo", "Logo ou ícone", null)),
        required: Boolean = true,
        tip: TipCardUi? = null,
    ) = StepUi.Question(
        id = "s1",
        title = "Para que é a imagem?",
        helpText = "Isso ajuda a Alarys a escolher formato e composição.",
        imageUrl = null,
        answerType = answerType,
        options = options,
        showsOptionsAsGrid = false,
        maxLength = 500,
        placeholder = null,
        required = required,
        tip = tip,
    )

    private fun inProgress(
        step: StepUi,
        draft: DraftUi = DraftUi(),
        canContinue: Boolean = false,
        canSkip: Boolean = false,
        isLastStep: Boolean = false,
    ) = QuestionnaireRunContent.InProgress(step, draft, RunProgressUi(number = 1, total = 6), canContinue, canSkip, isLastStep)

    @Test
    fun singleChoiceShowsHeaderProgressAndSelects() {
        setScreen(inProgress(question(AnswerType.SINGLE_CHOICE)))

        composeRule.onNodeWithText("Criar imagem").assertIsDisplayed()
        composeRule.onNodeWithText("Pergunta 1 de 6").assertIsDisplayed()
        composeRule.onNodeWithText("0%").assertIsDisplayed()
        composeRule.onNodeWithText("Escolha uma opção").assertIsDisplayed()
        composeRule.onNodeWithText("Isso ajuda a Alarys a escolher formato e composição.").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar").assertIsNotEnabled()
        composeRule.onNodeWithText("Logo ou ícone").performClick()

        assertEquals(listOf<QuestionnaireRunUiAction>(QuestionnaireRunUiAction.OptionToggled("logo")), actions)
    }

    @Test
    fun multipleChoiceShowsItsBadgeAndContinues() {
        setScreen(inProgress(question(AnswerType.MULTIPLE_CHOICE), DraftUi(listOf("post")), canContinue = true))

        composeRule.onNodeWithText("Escolha uma ou mais").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar").performClick()

        assertEquals(listOf<QuestionnaireRunUiAction>(QuestionnaireRunUiAction.ContinueClicked), actions)
    }

    @Test
    fun optionalOpenTextShowsCounterHintAndSkip() {
        setScreen(inProgress(question(AnswerType.OPEN_TEXT, options = emptyList(), required = false), DraftUi(text = "Café"), canSkip = true))

        composeRule.onNodeWithText("Resposta aberta").assertIsDisplayed()
        composeRule.onNodeWithText("4/500").assertIsDisplayed()
        composeRule.onNodeWithText("Opcional — você pode pular.").assertIsDisplayed()
        composeRule.onNodeWithTag(OPEN_TEXT_FIELD_TAG).performTextInput("!")
        composeRule.onNodeWithText("Pular").performClick()

        assertEquals(QuestionnaireRunUiAction.SkipClicked, actions.last())
    }

    @Test
    fun yesNoShowsTheTipAndReviewButtonOnTheLastStep() {
        setScreen(
            inProgress(
                question(AnswerType.YES_NO, tip = TipCardUi("isso é ético?", "Prefira pessoas fictícias.", null)),
                isLastStep = true,
            ),
        )

        composeRule.onNodeWithText("Sim ou não").assertIsDisplayed()
        composeRule.onNodeWithText("Dica: isso é ético?").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Prefira pessoas fictícias.").assertIsDisplayed()
        composeRule.onNodeWithText("Revisar pedido").assertIsDisplayed()
    }

    @Test
    fun closeAndBackSendTheirActions() {
        setScreen(inProgress(question(AnswerType.SINGLE_CHOICE)))

        composeRule.onNodeWithContentDescription("Fechar").performClick()
        composeRule.onNodeWithContentDescription("Voltar").performClick()

        assertEquals(listOf(QuestionnaireRunUiAction.CloseClicked, QuestionnaireRunUiAction.BackClicked), actions)
    }

    @Test
    fun exitDialogConfirmsOrStays() {
        setScreen(inProgress(question(AnswerType.SINGLE_CHOICE)), exitDialog = true)

        composeRule.onNodeWithText("Sair do questionário?").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar respondendo").performClick()

        assertEquals(listOf<QuestionnaireRunUiAction>(QuestionnaireRunUiAction.ExitDismissed), actions)
    }

    @Test
    fun reviewShowsAnswersChipsPreviewCostAndEdits() {
        setScreen(
            QuestionnaireRunContent.Review(
                items = listOf(
                    ReviewItemUi("s1", "Para que é a imagem?", ReviewAnswerUi.Text("Post para redes sociais"), isPartOfPrompt = true),
                    ReviewItemUi("v1", "Como descrever bem uma cena", ReviewAnswerUi.VideoWatched, isPartOfPrompt = false),
                ),
                promptPreview = "Crie uma imagem: Post para redes sociais",
                creditCost = 4,
            ),
        )

        composeRule.onNodeWithText("Revise seu pedido").assertIsDisplayed()
        composeRule.onNodeWithText("No prompt").assertIsDisplayed()
        composeRule.onNodeWithText("Só contexto").assertIsDisplayed()
        composeRule.onNodeWithText("Vídeo assistido").assertIsDisplayed()
        composeRule.onNodeWithText("Crie uma imagem: Post para redes sociais").assertIsDisplayed()
        composeRule.onNodeWithText("4 créditos").assertIsDisplayed()
        composeRule.onNodeWithText("Post para redes sociais").performClick()
        composeRule.onNodeWithText("Gerar com Alarys").performClick()

        assertEquals(
            listOf(QuestionnaireRunUiAction.EditAnswerClicked("s1"), QuestionnaireRunUiAction.GenerateClicked),
            actions,
        )
    }

    @Test
    fun unavailableOffersTheWayBack() {
        setScreen(QuestionnaireRunContent.Error(ContentLoadError.UNAVAILABLE))

        composeRule.onNodeWithText("Questionário indisponível").assertIsDisplayed()
        composeRule.onNodeWithText("Voltar aos questionários").performClick()

        assertEquals(listOf<QuestionnaireRunUiAction>(QuestionnaireRunUiAction.ExitConfirmed), actions)
    }
}
