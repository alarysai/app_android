package com.alarysai.alarysai.feature.questionnaires.presentation.run.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.toContentLoadErrorOrUnknown
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.navigation.QuestionnaireRunRoute
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.domain.model.PromptPart
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire
import com.alarysai.alarysai.feature.questionnaires.domain.model.QuestionnaireProgress
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepAnswer
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepResponse
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepType
import com.alarysai.alarysai.feature.questionnaires.domain.model.Tip
import com.alarysai.alarysai.feature.questionnaires.domain.repository.QuestionnaireRepository
import com.alarysai.alarysai.feature.questionnaires.domain.repository.StepTipRepository
import com.alarysai.alarysai.feature.questionnaires.domain.usecase.AdvanceQuestionnaireUseCase
import com.alarysai.alarysai.feature.questionnaires.domain.usecase.BuildPromptPreviewUseCase
import com.alarysai.alarysai.feature.questionnaires.domain.usecase.CollectPromptPartsUseCase
import com.alarysai.alarysai.feature.questionnaires.presentation.run.action.QuestionnaireRunUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.run.event.QuestionnaireRunUiEvent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.DraftUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunContent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunUiState
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewAnswerUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.ReviewItemUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.RunProgressUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepOptionUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.TipCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Runs a questionnaire, one screen per step, then a review. All steps are loaded once at the
 * start and the linked tips right after; from then on the flow is decided locally by
 * [AdvanceQuestionnaireUseCase], so it keeps working offline.
 */
@HiltViewModel
class QuestionnaireRunViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuestionnaireRepository,
    private val tipRepository: StepTipRepository,
    private val advanceQuestionnaire: AdvanceQuestionnaireUseCase,
    private val collectPromptParts: CollectPromptPartsUseCase,
    private val buildPromptPreview: BuildPromptPreviewUseCase,
    private val languageProvider: LanguageProvider,
) : ViewModel() {

    private val questionnaireId: String? =
        savedStateHandle.get<String>(QuestionnaireRunRoute.ARG_QUESTIONNAIRE_ID)?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(
        QuestionnaireRunUiState(title = savedStateHandle.get<String>(QuestionnaireRunRoute.ARG_TITLE).orEmpty()),
    )
    val uiState: StateFlow<QuestionnaireRunUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuestionnaireRunUiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<QuestionnaireRunUiEvent> = _events.asSharedFlow()

    private var questionnaire: Questionnaire? = null
    private var steps: List<Step> = emptyList()
    private var progress: QuestionnaireProgress? = null
    private var draft = DraftUi()

    /** Active tips by ID. A missing entry means still loading or nothing to show. */
    private val tips = mutableMapOf<String, Tip>()

    /**
     * Parts collected when the run reaches the review; null while it is in progress. They are what
     * the generation service will receive once it exists (sent as they are, never to the AI directly).
     */
    var promptParts: List<PromptPart>? = null
        private set

    init {
        load()
    }

    fun onAction(action: QuestionnaireRunUiAction) {
        when (action) {
            is QuestionnaireRunUiAction.OptionToggled -> toggleOption(action.optionId)
            is QuestionnaireRunUiAction.TextChanged -> changeText(action.text)
            QuestionnaireRunUiAction.ContinueClicked -> submit(draft.toResponse())
            QuestionnaireRunUiAction.SkipClicked -> submit(StepResponse(skipped = true))
            QuestionnaireRunUiAction.WatchVideoClicked -> openCurrentVideo()
            QuestionnaireRunUiAction.BackClicked -> goBack()
            QuestionnaireRunUiAction.CloseClicked -> close()
            QuestionnaireRunUiAction.ExitConfirmed -> exit()
            QuestionnaireRunUiAction.ExitDismissed -> _uiState.update { it.copy(isExitConfirmationVisible = false) }
            is QuestionnaireRunUiAction.EditAnswerClicked -> edit(action.stepId)
            QuestionnaireRunUiAction.GenerateClicked -> _events.tryEmit(QuestionnaireRunUiEvent.GenerationComingSoon)
            QuestionnaireRunUiAction.Retry -> load()
        }
    }

    private fun load() {
        val id = questionnaireId ?: return showContent(QuestionnaireRunContent.Error(ContentLoadError.UNAVAILABLE))
        showContent(QuestionnaireRunContent.Loading)
        viewModelScope.launch {
            try {
                val loaded = repository.getQuestionnaireWithSteps(id)
                questionnaire = loaded.questionnaire
                steps = loaded.steps
                _uiState.update { it.copy(title = loaded.questionnaire.title.resolve(languageProvider.currentLanguage())) }
                moveTo(advanceQuestionnaire.start(steps), DraftUi())
                loadTips()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showContent(QuestionnaireRunContent.Error(error.toContentLoadErrorOrUnknown()))
            }
        }
    }

    /** One read per distinct tip (step flags and options), in parallel. A tip that fails never shows. */
    private fun loadTips() {
        steps.flatMap { step -> listOfNotNull(step.infoFlag?.tipId) + step.options.mapNotNull { it.tipId } }
            .distinct()
            .filterNot { it in tips }
            .forEach { tipId ->
                viewModelScope.launch {
                    tips[tipId] = tipRepository.getActiveTip(tipId) ?: return@launch
                    if (progress?.isFinished == false) render()
                }
            }
    }

    private fun toggleOption(optionId: String) {
        val step = currentStep() ?: return
        if (step.options.none { it.id == optionId }) return
        val selected = draft.selectedOptionIds
        draft = draft.copy(
            selectedOptionIds = when {
                step.answerType != AnswerType.MULTIPLE_CHOICE -> listOf(optionId)
                optionId in selected -> selected - optionId
                else -> selected + optionId
            },
        )
        render()
    }

    private fun changeText(text: String) {
        val step = currentStep() ?: return
        draft = draft.copy(text = text.take(step.maxLength))
        render()
    }

    private fun submit(response: StepResponse) {
        val current = progress ?: return
        val next = advanceQuestionnaire(steps, current, response)
        if (next != current) moveTo(next, DraftUi())
    }

    /** Back to the previous step with its answer filled in; on the first step (or while loading) it leaves. */
    private fun goBack() {
        val current = progress
        val previous = current?.back()
        if (current == null || previous == null) {
            _events.tryEmit(QuestionnaireRunUiEvent.Exit)
            return
        }
        moveTo(previous, current.answers.last().toDraft())
    }

    private fun edit(stepId: String) {
        val current = progress ?: return
        val reopened = current.reopen(stepId) ?: return
        moveTo(reopened, current.answers.first { it.stepId == stepId }.toDraft())
    }

    private fun close() {
        val hasAnswers = progress?.answers?.isNotEmpty() == true || draft != DraftUi()
        if (hasAnswers) {
            _uiState.update { it.copy(isExitConfirmationVisible = true) }
        } else {
            exit()
        }
    }

    private fun exit() {
        _uiState.update { it.copy(isExitConfirmationVisible = false) }
        _events.tryEmit(QuestionnaireRunUiEvent.Exit)
    }

    private fun openCurrentVideo() {
        val url = currentStep()?.videoUrl ?: return
        _events.tryEmit(QuestionnaireRunUiEvent.OpenVideo(url))
    }

    private fun moveTo(newProgress: QuestionnaireProgress, newDraft: DraftUi) {
        progress = newProgress
        draft = newDraft
        render()
    }

    private fun render() {
        val current = progress ?: return
        val language = languageProvider.currentLanguage()
        val step = currentStep()
        if (step == null) {
            showReview(current, language)
            return
        }
        promptParts = null
        val remaining = advanceQuestionnaire.countRemaining(steps, current, draft.selectedOptionIds.singleOrNull())
        showContent(
            QuestionnaireRunContent.InProgress(
                step = step.toUi(language),
                draft = draft,
                progress = RunProgressUi(number = current.answers.size + 1, total = current.answers.size + remaining),
                canContinue = advanceQuestionnaire(steps, current, draft.toResponse()) != current,
                canSkip = step.type == StepType.QUESTION && !step.required,
                isLastStep = remaining <= 1,
            ),
        )
    }

    private fun showReview(current: QuestionnaireProgress, language: Language) {
        val parts = collectPromptParts(current.answers, steps, language)
        promptParts = parts
        showContent(
            QuestionnaireRunContent.Review(
                items = current.answers.mapNotNull { it.toReviewItem(language) },
                promptPreview = buildPromptPreview(parts),
                creditCost = questionnaire?.creditCost,
            ),
        )
    }

    private fun showContent(content: QuestionnaireRunContent) {
        _uiState.update { it.copy(content = content) }
    }

    private fun currentStep(): Step? = progress?.currentStepId?.let { id -> steps.firstOrNull { it.id == id } }

    private fun DraftUi.toResponse() = StepResponse(optionIds = selectedOptionIds, text = text)

    private fun StepAnswer.toDraft() = DraftUi(selectedOptionIds = optionIds, text = text.orEmpty())

    private fun Step.toUi(language: Language): StepUi {
        val title = text?.resolve(language)
        val help = helpText?.resolve(language)
        val tip = tipFor(language)
        return when (type) {
            StepType.VIDEO -> StepUi.Video(id, title, help, imageUrl, videoUrl = videoUrl.orEmpty(), tip = tip)
            StepType.QUESTION -> StepUi.Question(
                id = id,
                title = title,
                helpText = help,
                imageUrl = imageUrl,
                answerType = answerType,
                options = options.map { StepOptionUi(it.id, it.text?.resolve(language), it.imageUrl) },
                showsOptionsAsGrid = showsOptionsAsGrid,
                maxLength = maxLength,
                placeholder = placeholder?.resolve(language),
                required = required,
                tip = tip,
            )
        }
    }

    /** The selected option's tip wins; otherwise the step's info flag tip ("Tip: Is this ethical?"). */
    private fun Step.tipFor(language: Language): TipCardUi? {
        val optionTip = options.filter { it.id in draft.selectedOptionIds }
            .firstNotNullOfOrNull { option -> option.tipId?.let(tips::get) }
        if (optionTip != null) {
            return TipCardUi(label = null, text = optionTip.text.resolve(language), imageUrl = optionTip.imageUrl)
        }
        val flag = infoFlag ?: return null
        val flagTip = flag.tipId?.let(tips::get) ?: return null
        return TipCardUi(label = flag.label.resolve(language), text = flagTip.text.resolve(language), imageUrl = flagTip.imageUrl)
    }

    private fun StepAnswer.toReviewItem(language: Language): ReviewItemUi? {
        val step = steps.firstOrNull { it.id == stepId } ?: return null
        val chosenTexts = step.options.filter { it.id in optionIds }.mapNotNull { it.text?.resolve(language) }
        val answer = when {
            step.type == StepType.VIDEO -> ReviewAnswerUi.VideoWatched
            text != null -> ReviewAnswerUi.Text(text)
            chosenTexts.isNotEmpty() -> ReviewAnswerUi.Text(chosenTexts.joinToString(", "))
            else -> ReviewAnswerUi.Skipped
        }
        return ReviewItemUi(stepId, step.text?.resolve(language), answer, isPartOfPrompt = step.partOfPrompt)
    }
}
