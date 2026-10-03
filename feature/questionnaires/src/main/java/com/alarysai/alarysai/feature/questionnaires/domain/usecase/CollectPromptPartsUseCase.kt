package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.feature.questionnaires.domain.model.PromptPart
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepAnswer
import javax.inject.Inject

/**
 * Port of `promptParts` from the admin panel (`src/features/questionnaires/domain/prompt-preview.ts`,
 * `android-integration.md` section 6), extended to the proposed answer types: for each answered
 * step marked "part of the prompt", in the order the steps were answered, the answer and the
 * step and option instructions. Steps missing from [steps] are ignored.
 *
 * - single choice / yes-no: the option text;
 * - multiple choice: the option texts joined with ", " and every chosen option's instruction;
 * - open text: what the user wrote (never translated);
 * - video or skipped question: no answer, only the step instruction.
 */
class CollectPromptPartsUseCase @Inject constructor() {

    operator fun invoke(answers: List<StepAnswer>, steps: List<Step>, language: Language): List<PromptPart> {
        val stepsById = steps.associateBy { it.id }
        return answers.mapNotNull { answer ->
            val step = stepsById[answer.stepId]?.takeIf { it.partOfPrompt } ?: return@mapNotNull null
            val chosen = step.options.filter { it.id in answer.optionIds }
            val optionTexts = chosen.mapNotNull { it.text?.resolve(language) }
            PromptPart(
                stepId = step.id,
                answer = answer.text ?: optionTexts.joinToString(", ").takeIf { it.isNotEmpty() },
                instructions = listOfNotNull(step.promptInstruction) + chosen.mapNotNull { it.promptInstruction },
            )
        }
    }
}
