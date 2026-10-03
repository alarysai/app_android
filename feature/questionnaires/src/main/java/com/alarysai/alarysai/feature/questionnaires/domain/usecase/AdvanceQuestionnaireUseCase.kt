package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.domain.model.QuestionnaireProgress
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepAnswer
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepOption
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepResponse
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepType
import javax.inject.Inject

/**
 * Moves a run forward. The panel guarantees a flow without cycles whose jumps all exist,
 * but the app protects itself anyway: a jump to a missing step, a step already seen in this
 * run, or more than [MAX_STEPS] answers all end the questionnaire instead of looping.
 *
 * Only single choice (and yes/no) follows option jumps; multiple choice, open text, videos and
 * skipped questions follow the step jump, then `order`.
 */
class AdvanceQuestionnaireUseCase @Inject constructor(
    private val resolveNextStep: ResolveNextStepUseCase,
) {

    /** The first step is the one with the lowest `order`: the head of the sorted list. */
    fun start(steps: List<Step>): QuestionnaireProgress =
        QuestionnaireProgress(currentStepId = steps.firstOrNull()?.id)

    /** Answers the current step. Returns [progress] unchanged when [response] is not valid for it. */
    operator fun invoke(steps: List<Step>, progress: QuestionnaireProgress, response: StepResponse): QuestionnaireProgress {
        val current = steps.firstOrNull { it.id == progress.currentStepId } ?: return progress
        val answer = current.toAnswerOrNull(response) ?: return progress
        val answers = progress.answers + answer
        val visited = answers.map { it.stepId }.toSet()
        val nextStepId = resolveNextStep(current, current.jumpOption(answer), steps)
            ?.takeIf { next -> steps.any { it.id == next } }
            ?.takeIf { it !in visited }
            ?.takeIf { answers.size < MAX_STEPS }
        return QuestionnaireProgress(currentStepId = nextStepId, answers = answers)
    }

    /**
     * How many steps are still ahead, counting the current one, if the user keeps the current
     * [draftOptionId] (single choice) and takes default paths afterwards. Used for "Question N of M";
     * it changes when an answer leads down a shorter or longer branch.
     */
    fun countRemaining(steps: List<Step>, progress: QuestionnaireProgress, draftOptionId: String?): Int {
        var step = steps.firstOrNull { it.id == progress.currentStepId } ?: return 0
        val visited = progress.answers.map { it.stepId }.toMutableSet()
        var count = 0
        var option = step.options.firstOrNull { it.id == draftOptionId }?.takeIf { step.followsOptionJumps }
        while (visited.size < MAX_STEPS) {
            count++
            visited += step.id
            val nextId = resolveNextStep(step, option, steps) ?: break
            step = steps.firstOrNull { it.id == nextId }?.takeIf { it.id !in visited } ?: break
            option = null
        }
        return count
    }

    private fun Step.toAnswerOrNull(response: StepResponse): StepAnswer? {
        if (response.skipped) return if (type == StepType.QUESTION && !required) StepAnswer(id) else null
        if (type == StepType.VIDEO) return StepAnswer(id)
        return when (answerType) {
            AnswerType.OPEN_TEXT -> openTextAnswer(response.text)
            // A choice question left without valid options continues like a video.
            else -> if (options.isEmpty()) StepAnswer(id) else choiceAnswer(response.optionIds)
        }
    }

    private fun Step.openTextAnswer(text: String?): StepAnswer? {
        val trimmed = text?.trim().orEmpty()
        if (trimmed.length > maxLength) return null
        if (trimmed.isEmpty()) return if (required) null else StepAnswer(id)
        return StepAnswer(id, text = trimmed)
    }

    private fun Step.choiceAnswer(optionIds: List<String>): StepAnswer? {
        val chosen = options.filter { it.id in optionIds }
        if (chosen.size != optionIds.distinct().size) return null
        val valid = when (answerType) {
            AnswerType.MULTIPLE_CHOICE -> chosen.isNotEmpty()
            else -> chosen.size == 1
        }
        // Options are stored in the step's order, whatever order they were tapped in.
        return if (valid) StepAnswer(id, optionIds = chosen.map { it.id }) else null
    }

    private val Step.followsOptionJumps: Boolean
        get() = answerType == AnswerType.SINGLE_CHOICE || answerType == AnswerType.YES_NO

    private fun Step.jumpOption(answer: StepAnswer): StepOption? =
        if (followsOptionJumps) options.firstOrNull { it.id == answer.optionIds.singleOrNull() } else null

    companion object {
        const val MAX_STEPS = 200
    }
}
