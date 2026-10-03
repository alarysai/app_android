package com.alarysai.alarysai.feature.questionnaires.presentation.run.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.navigation.QuestionnaireRunRoute
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.domain.model.InfoFlag
import com.alarysai.alarysai.feature.questionnaires.domain.model.PromptPart
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire
import com.alarysai.alarysai.feature.questionnaires.domain.model.QuestionnaireWithSteps
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.Tip
import com.alarysai.alarysai.feature.questionnaires.domain.option
import com.alarysai.alarysai.feature.questionnaires.domain.question
import com.alarysai.alarysai.feature.questionnaires.domain.repository.QuestionnaireRepository
import com.alarysai.alarysai.feature.questionnaires.domain.repository.StepTipRepository
import com.alarysai.alarysai.feature.questionnaires.domain.usecase.AdvanceQuestionnaireUseCase
import com.alarysai.alarysai.feature.questionnaires.domain.usecase.BuildPromptPreviewUseCase
import com.alarysai.alarysai.feature.questionnaires.domain.usecase.CollectPromptPartsUseCase
import com.alarysai.alarysai.feature.questionnaires.domain.usecase.ResolveNextStepUseCase
import com.alarysai.alarysai.feature.questionnaires.domain.video
import com.alarysai.alarysai.feature.questionnaires.presentation.run.action.QuestionnaireRunUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.run.event.QuestionnaireRunUiEvent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.DraftUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunContent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewAnswerUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewItemUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.RunProgressUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.TipCardUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuestionnaireRunViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Shaped like the mockup: format (single, in prompt), things (multiple), scene (optional open text),
    // real people (yes/no with an ethics tip), then the review.
    private val format = question("format", 0, option("post"), option("logo", nextStepId = "real"))
        .copy(partOfPrompt = true, promptInstruction = "Crie uma imagem.", helpText = LocalizedText(pt = "Isso ajuda a escolher o formato."))
    private val things = question("things", 1, option("people"), option("product"))
        .copy(answerType = AnswerType.MULTIPLE_CHOICE, partOfPrompt = true)
    private val scene = question("scene", 2)
        .copy(answerType = AnswerType.OPEN_TEXT, required = false, maxLength = 20, partOfPrompt = true, promptInstruction = "Cena:")
    private val real = question("real", 3, option("yes").copy(tipId = "consent"), option("no"))
        .copy(answerType = AnswerType.YES_NO, infoFlag = InfoFlag(LocalizedText(pt = "isso é ético?"), value = true, tipId = "ethics"))

    private val questionnaire = Questionnaire(
        id = "q1",
        categoryId = "cat1",
        title = LocalizedText(pt = "Criar imagem", en = "Create image"),
        description = null,
        imageUrl = null,
        languages = emptySet(),
        order = 0,
        creditCost = 4,
    )

    private fun loaded(vararg steps: Step) = QuestionnaireWithSteps(questionnaire, steps.toList())

    private val full = loaded(format, things, scene, real)

    /** Each load gets the next result, so retry can be tested. */
    private class FakeRepository(vararg results: suspend () -> QuestionnaireWithSteps) : QuestionnaireRepository {
        private val pending = ArrayDeque(results.toList())
        val requestedIds = mutableListOf<String>()

        override fun observePublishedQuestionnaires(categoryId: String): Flow<ContentList<Questionnaire>> = emptyFlow()

        override suspend fun getQuestionnaireWithSteps(questionnaireId: String): QuestionnaireWithSteps {
            requestedIds += questionnaireId
            return pending.removeFirst().invoke()
        }
    }

    private class FakeTipRepository(private val tips: Map<String, Tip> = emptyMap()) : StepTipRepository {
        val requestedIds = mutableListOf<String>()

        override suspend fun getActiveTip(tipId: String): Tip? {
            requestedIds += tipId
            return tips[tipId]
        }
    }

    private class FixedLanguage(private val language: Language) : LanguageProvider {
        override fun currentLanguage() = language
    }

    private fun args(questionnaireId: String? = "q1") = SavedStateHandle(
        buildMap {
            questionnaireId?.let { put(QuestionnaireRunRoute.ARG_QUESTIONNAIRE_ID, it) }
            put(QuestionnaireRunRoute.ARG_TITLE, "Criar imagem")
        },
    )

    private fun viewModel(
        repository: QuestionnaireRepository = FakeRepository({ full }),
        language: Language = Language.PT,
        tipRepository: StepTipRepository = FakeTipRepository(),
        savedStateHandle: SavedStateHandle = args(),
    ) = QuestionnaireRunViewModel(
        savedStateHandle,
        repository,
        tipRepository,
        AdvanceQuestionnaireUseCase(ResolveNextStepUseCase()),
        CollectPromptPartsUseCase(),
        BuildPromptPreviewUseCase(),
        FixedLanguage(language),
    )

    private fun QuestionnaireRunViewModel.inProgress() = uiState.value.content as QuestionnaireRunContent.InProgress
    private fun QuestionnaireRunViewModel.act(vararg actions: QuestionnaireRunUiAction) = actions.forEach(::onAction)

    @Test
    fun `opens the first question with title, help text and estimated progress in the user language`() {
        val viewModel = viewModel(language = Language.EN)

        assertEquals("Create image", viewModel.uiState.value.title)
        val content = viewModel.inProgress()
        assertEquals("format", content.step.id)
        assertEquals("Isso ajuda a escolher o formato.", content.step.helpText)
        assertEquals(RunProgressUi(number = 1, total = 4), content.progress)
        assertFalse(content.canContinue)
        assertFalse(content.canSkip)
    }

    @Test
    fun `selecting an option enables continue and updates the estimated path`() {
        val viewModel = viewModel()

        viewModel.act(QuestionnaireRunUiAction.OptionToggled("logo"))

        assertEquals(DraftUi(selectedOptionIds = listOf("logo")), viewModel.inProgress().draft)
        assertTrue(viewModel.inProgress().canContinue)
        assertEquals(2, viewModel.inProgress().progress.total)
    }

    @Test
    fun `single choice replaces the selection, multiple choice toggles`() {
        val viewModel = viewModel()
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("post"), QuestionnaireRunUiAction.OptionToggled("logo"))
        assertEquals(listOf("logo"), viewModel.inProgress().draft.selectedOptionIds)

        viewModel.act(QuestionnaireRunUiAction.OptionToggled("post"), QuestionnaireRunUiAction.ContinueClicked)
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("people"), QuestionnaireRunUiAction.OptionToggled("product"))
        assertEquals(listOf("people", "product"), viewModel.inProgress().draft.selectedOptionIds)
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("people"))
        assertEquals(listOf("product"), viewModel.inProgress().draft.selectedOptionIds)
    }

    @Test
    fun `optional open text can be skipped and limits the text length`() {
        val viewModel = viewModel()
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("post"), QuestionnaireRunUiAction.ContinueClicked)
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("people"), QuestionnaireRunUiAction.ContinueClicked)

        assertEquals("scene", viewModel.inProgress().step.id)
        assertTrue(viewModel.inProgress().canSkip)
        viewModel.act(QuestionnaireRunUiAction.TextChanged("Uma barista sorrindo no balcão"))
        assertEquals("Uma barista sorrindo", viewModel.inProgress().draft.text)

        viewModel.act(QuestionnaireRunUiAction.SkipClicked)
        assertEquals("real", viewModel.inProgress().step.id)
        assertTrue(viewModel.inProgress().isLastStep)
    }

    @Test
    fun `the yes or no step shows the info flag tip, and the selected option tip replaces it`() {
        val tips = FakeTipRepository(
            mapOf(
                "ethics" to Tip("ethics", LocalizedText(pt = "Prefira pessoas fictícias."), null),
                "consent" to Tip("consent", LocalizedText(pt = "Tenha autorização por escrito."), null),
            ),
        )
        val viewModel = viewModel(repository = FakeRepository({ loaded(real) }), tipRepository = tips)

        assertEquals(TipCardUi("isso é ético?", "Prefira pessoas fictícias.", null), viewModel.inProgress().step.tip)
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("yes"))
        assertEquals(TipCardUi(null, "Tenha autorização por escrito.", null), viewModel.inProgress().step.tip)
        assertEquals(listOf("ethics", "consent"), tips.requestedIds)
    }

    @Test
    fun `finishing shows the review with answers, chips, prompt preview and cost`() {
        val viewModel = viewModel()
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("post"), QuestionnaireRunUiAction.ContinueClicked)
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("people"), QuestionnaireRunUiAction.ContinueClicked)
        viewModel.act(QuestionnaireRunUiAction.TextChanged("Uma barista"), QuestionnaireRunUiAction.ContinueClicked)
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("no"), QuestionnaireRunUiAction.ContinueClicked)

        assertEquals(
            QuestionnaireRunContent.Review(
                items = listOf(
                    ReviewItemUi("format", "Pergunta format", ReviewAnswerUi.Text("Opção post"), isPartOfPrompt = true),
                    ReviewItemUi("things", "Pergunta things", ReviewAnswerUi.Text("Opção people"), isPartOfPrompt = true),
                    ReviewItemUi("scene", "Pergunta scene", ReviewAnswerUi.Text("Uma barista"), isPartOfPrompt = true),
                    ReviewItemUi("real", "Pergunta real", ReviewAnswerUi.Text("Opção no"), isPartOfPrompt = false),
                ),
                promptPreview = "Crie uma imagem: Opção post\nOpção people\nCena: Uma barista",
                creditCost = 4,
            ),
            viewModel.uiState.value.content,
        )
        assertEquals(
            listOf(
                PromptPart("format", "Opção post", listOf("Crie uma imagem.")),
                PromptPart("things", "Opção people", emptyList()),
                PromptPart("scene", "Uma barista", listOf("Cena:")),
            ),
            viewModel.promptParts,
        )
    }

    @Test
    fun `videos and skipped questions show as such in the review`() {
        val viewModel = viewModel(repository = FakeRepository({ loaded(video("intro", 0), scene) }))
        viewModel.act(QuestionnaireRunUiAction.ContinueClicked, QuestionnaireRunUiAction.SkipClicked)

        val review = viewModel.uiState.value.content as QuestionnaireRunContent.Review
        assertEquals(listOf(ReviewAnswerUi.VideoWatched, ReviewAnswerUi.Skipped), review.items.map { it.answer })
    }

    @Test
    fun `editing an answer from the review reopens that step with the answer filled in`() {
        val viewModel = viewModel(repository = FakeRepository({ loaded(format, things) }))
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("post"), QuestionnaireRunUiAction.ContinueClicked)
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("product"), QuestionnaireRunUiAction.ContinueClicked)

        viewModel.act(QuestionnaireRunUiAction.EditAnswerClicked("format"))

        assertEquals("format", viewModel.inProgress().step.id)
        assertEquals(listOf("post"), viewModel.inProgress().draft.selectedOptionIds)
        assertNull(viewModel.promptParts)
    }

    @Test
    fun `back restores the previous answer and leaves from the first step`() = runTest {
        val viewModel = viewModel()
        viewModel.act(QuestionnaireRunUiAction.OptionToggled("post"), QuestionnaireRunUiAction.ContinueClicked)

        viewModel.events.test {
            viewModel.act(QuestionnaireRunUiAction.BackClicked)
            assertEquals("format", viewModel.inProgress().step.id)
            assertEquals(listOf("post"), viewModel.inProgress().draft.selectedOptionIds)
            expectNoEvents()

            viewModel.act(QuestionnaireRunUiAction.BackClicked)
            assertEquals(QuestionnaireRunUiEvent.Exit, awaitItem())
        }
    }

    @Test
    fun `closing asks for confirmation only when there is something to lose`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.act(QuestionnaireRunUiAction.CloseClicked)
            assertEquals(QuestionnaireRunUiEvent.Exit, awaitItem())

            viewModel.act(QuestionnaireRunUiAction.OptionToggled("post"), QuestionnaireRunUiAction.CloseClicked)
            assertTrue(viewModel.uiState.value.isExitConfirmationVisible)
            expectNoEvents()

            viewModel.act(QuestionnaireRunUiAction.ExitDismissed)
            assertFalse(viewModel.uiState.value.isExitConfirmationVisible)

            viewModel.act(QuestionnaireRunUiAction.CloseClicked, QuestionnaireRunUiAction.ExitConfirmed)
            assertEquals(QuestionnaireRunUiEvent.Exit, awaitItem())
        }
    }

    @Test
    fun `video steps open their link and generating announces it is coming soon`() = runTest {
        val viewModel = viewModel(repository = FakeRepository({ loaded(video("intro", 0)) }))
        assertTrue(viewModel.inProgress().step is StepUi.Video)

        viewModel.events.test {
            viewModel.act(QuestionnaireRunUiAction.WatchVideoClicked)
            assertEquals(QuestionnaireRunUiEvent.OpenVideo("https://www.youtube.com/watch?v=intro"), awaitItem())
            viewModel.act(QuestionnaireRunUiAction.GenerateClicked)
            assertEquals(QuestionnaireRunUiEvent.GenerationComingSoon, awaitItem())
        }
    }

    @Test
    fun `unavailable, missing id and retry`() {
        val unavailable = viewModel(repository = FakeRepository({ throw ContentLoadException(ContentLoadError.UNAVAILABLE) }))
        assertEquals(QuestionnaireRunContent.Error(ContentLoadError.UNAVAILABLE), unavailable.uiState.value.content)
        assertEquals("Criar imagem", unavailable.uiState.value.title)

        val noId = FakeRepository()
        assertEquals(
            QuestionnaireRunContent.Error(ContentLoadError.UNAVAILABLE),
            viewModel(repository = noId, savedStateHandle = args(questionnaireId = null)).uiState.value.content,
        )
        assertEquals(emptyList<String>(), noId.requestedIds)

        val flaky = FakeRepository({ throw ContentLoadException(ContentLoadError.OFFLINE) }, { full })
        val viewModel = viewModel(repository = flaky)
        viewModel.act(QuestionnaireRunUiAction.Retry)
        assertEquals("format", viewModel.inProgress().step.id)
    }
}
