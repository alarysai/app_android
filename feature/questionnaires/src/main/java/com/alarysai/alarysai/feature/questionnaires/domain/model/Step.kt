package com.alarysai.alarysai.feature.questionnaires.domain.model

import com.alarysai.alarysai.core.common.language.LocalizedText

enum class StepType { QUESTION, VIDEO }

/**
 * How a question is answered. Proposed field `answerType` (see docs/proposta-questionario-v2.md);
 * while the panel does not write it, every question is [SINGLE_CHOICE], the original behavior.
 */
enum class AnswerType {
    /** One option, in a list (or a grid when every option has an image). */
    SINGLE_CHOICE,

    /** One or more options. Option jumps are ignored: the step jump (or `order`) decides. */
    MULTIPLE_CHOICE,

    /** Free text typed or dictated by the user. */
    OPEN_TEXT,

    /** Single choice between exactly two options shown side by side ("Sim" / "Não"). */
    YES_NO,
}

/**
 * One step of a questionnaire. Always has [text] or [imageUrl]; a video always has [videoUrl].
 * [nextStepId] is null (follow `order`), [END_OF_QUESTIONNAIRE] or the ID of another step.
 */
data class Step(
    val id: String,
    val order: Int,
    val type: StepType,
    /** The question or statement, shown as the step title. */
    val text: LocalizedText?,
    val imageUrl: String?,
    val videoUrl: String?,
    val options: List<StepOption>,
    val nextStepId: String?,
    val partOfPrompt: Boolean,
    /** Not translated: it goes to the AI as is. */
    val promptInstruction: String?,
    val infoFlag: InfoFlag? = null,
    val answerType: AnswerType = AnswerType.SINGLE_CHOICE,
    /** Proposed `helpText`: smaller line under the title. */
    val helpText: LocalizedText? = null,
    /** Proposed `required` (default true). Optional questions can be skipped. */
    val required: Boolean = true,
    /** Proposed `maxLength`, for [AnswerType.OPEN_TEXT]. */
    val maxLength: Int = DEFAULT_MAX_LENGTH,
    /** Proposed `placeholder`, for [AnswerType.OPEN_TEXT]. */
    val placeholder: LocalizedText? = null,
) {
    /** Options are shown as image cards in a grid only when every option has an image. */
    val showsOptionsAsGrid: Boolean
        get() = options.isNotEmpty() && options.all { it.imageUrl != null }

    companion object {
        const val DEFAULT_MAX_LENGTH = 500
    }
}

/** Answer option of a question. Always has [text] or [imageUrl]; [id] is unique within the step. */
data class StepOption(
    val id: String,
    val text: LocalizedText?,
    val imageUrl: String?,
    val promptInstruction: String?,
    /** Overrides the step's [Step.nextStepId] when this option is chosen (single choice only). */
    val nextStepId: String?,
    /** Proposed `tipId`: tip shown while this option is selected (e.g. "Sim" -> ethics tip). */
    val tipId: String? = null,
)

/**
 * Informative yes/no attached to the step (e.g. "Is this ethical?"). In the new design it is
 * shown as the step's tip card: "Tip: <label>" with the tip text. [value] is not shown.
 */
data class InfoFlag(
    val label: LocalizedText,
    val value: Boolean,
    val tipId: String?,
)

/** Special `nextStepId` that ends the questionnaire right away. */
const val END_OF_QUESTIONNAIRE = "__end__"
