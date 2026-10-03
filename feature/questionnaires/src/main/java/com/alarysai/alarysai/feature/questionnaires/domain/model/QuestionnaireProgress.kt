package com.alarysai.alarysai.feature.questionnaires.domain.model

/**
 * What the user did on a step. A video has neither options nor text; a skipped question
 * (optional only) also has neither.
 */
data class StepAnswer(
    val stepId: String,
    val optionIds: List<String> = emptyList(),
    val text: String? = null,
) {
    val isEmpty: Boolean get() = optionIds.isEmpty() && text == null
}

/** The user's response to the current step, before it is validated by the flow. */
data class StepResponse(
    val optionIds: List<String> = emptyList(),
    val text: String? = null,
    /** True when the user tapped "Skip"; only accepted on optional questions. */
    val skipped: Boolean = false,
)

/**
 * Where a run of a questionnaire is. [answers] are in the order the steps were answered,
 * so they also describe the path taken. [currentStepId] null means the questionnaire ended.
 */
data class QuestionnaireProgress(
    val currentStepId: String?,
    val answers: List<StepAnswer> = emptyList(),
) {
    val isFinished: Boolean get() = currentStepId == null

    /** Back to the previous step, undoing its answer; null on the first step. */
    fun back(): QuestionnaireProgress? {
        val last = answers.lastOrNull() ?: return null
        return QuestionnaireProgress(currentStepId = last.stepId, answers = answers.dropLast(1))
    }

    /**
     * Reopens an answered step to edit it: that answer and every later one are undone,
     * because a different answer may lead down a different path. Null if [stepId] was not answered.
     */
    fun reopen(stepId: String): QuestionnaireProgress? {
        val index = answers.indexOfFirst { it.stepId == stepId }
        if (index < 0) return null
        return QuestionnaireProgress(currentStepId = stepId, answers = answers.take(index))
    }
}
