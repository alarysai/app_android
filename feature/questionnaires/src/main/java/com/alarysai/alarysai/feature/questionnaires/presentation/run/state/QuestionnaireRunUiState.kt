package com.alarysai.alarysai.feature.questionnaires.presentation.run.state

import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType

data class QuestionnaireRunUiState(
    /** From the navigation argument first, then from the loaded questionnaire. */
    val title: String,
    val content: QuestionnaireRunContent = QuestionnaireRunContent.Loading,
    /** "Leave the questionnaire?" dialog, shown when closing with answers given. */
    val isExitConfirmationVisible: Boolean = false,
)

sealed interface QuestionnaireRunContent {
    data object Loading : QuestionnaireRunContent

    data class InProgress(
        val step: StepUi,
        val draft: DraftUi,
        val progress: RunProgressUi,
        /** The draft is a valid answer for this step. */
        val canContinue: Boolean,
        /** Optional question: "Skip" is shown. */
        val canSkip: Boolean,
        /** Continuing ends the questionnaire, so the button reads "Review request". */
        val isLastStep: Boolean,
    ) : QuestionnaireRunContent

    /** Every answer, the prompt preview and the cost, before generating. */
    data class Review(
        val items: List<ReviewItemUi>,
        val promptPreview: String?,
        /** Null while the panel does not set the proposed `creditCost`. */
        val creditCost: Int?,
    ) : QuestionnaireRunContent

    data class Error(val error: ContentLoadError) : QuestionnaireRunContent
}

/** "Question [number] of [total]". [total] is an estimate: it follows the current answers' path. */
data class RunProgressUi(
    val number: Int,
    val total: Int,
) {
    /** Share of the path already answered: 0% on the first question, 100% on the last. */
    val percent: Int get() = if (total <= 1) 100 else ((number - 1) * 100) / (total - 1)
}

/** What the user selected or typed on the current step, not yet submitted. */
data class DraftUi(
    val selectedOptionIds: List<String> = emptyList(),
    val text: String = "",
)

/** Texts already resolved to the user's language. */
sealed interface StepUi {
    val id: String
    val title: String?
    val helpText: String?
    val imageUrl: String?
    val tip: TipCardUi?

    data class Question(
        override val id: String,
        override val title: String?,
        override val helpText: String?,
        override val imageUrl: String?,
        val answerType: AnswerType,
        /** Empty for open text, or for a malformed choice question (then only "Continue"). */
        val options: List<StepOptionUi>,
        val showsOptionsAsGrid: Boolean,
        val maxLength: Int,
        val placeholder: String?,
        val required: Boolean,
        override val tip: TipCardUi? = null,
    ) : StepUi

    data class Video(
        override val id: String,
        override val title: String?,
        override val helpText: String?,
        override val imageUrl: String?,
        val videoUrl: String,
        override val tip: TipCardUi? = null,
    ) : StepUi
}

/** A tip shown on the step: "Tip: [label]" (from the step's info flag) or just "Tip". */
data class TipCardUi(
    val label: String?,
    val text: String,
    val imageUrl: String?,
)

data class StepOptionUi(
    val id: String,
    val text: String?,
    val imageUrl: String?,
)

/** One answered step on the review screen; tapping it reopens the step to edit. */
data class ReviewItemUi(
    val stepId: String,
    val question: String?,
    val answer: ReviewAnswerUi,
    /** "In the prompt" or "Context only" chip. */
    val isPartOfPrompt: Boolean,
)

sealed interface ReviewAnswerUi {
    data class Text(val value: String) : ReviewAnswerUi
    data object VideoWatched : ReviewAnswerUi
    data object Skipped : ReviewAnswerUi
}
