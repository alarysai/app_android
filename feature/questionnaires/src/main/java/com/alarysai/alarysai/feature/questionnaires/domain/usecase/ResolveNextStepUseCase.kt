package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.feature.questionnaires.domain.model.END_OF_QUESTIONNAIRE
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepOption
import javax.inject.Inject

/**
 * Port of `resolveNext` from the admin panel (`src/features/questionnaires/domain/flow.ts`,
 * `android-integration.md` section 6). The next step is the first that exists of:
 * 1. the chosen option's `nextStepId`;
 * 2. the step's `nextStepId`;
 * 3. the next step by `order`.
 *
 * [END_OF_QUESTIONNAIRE] anywhere ends the questionnaire. Returns the next step ID, or null for the end.
 * The returned ID may not exist in [ordered]; [AdvanceQuestionnaireUseCase] treats that as the end.
 */
class ResolveNextStepUseCase @Inject constructor() {

    operator fun invoke(step: Step, option: StepOption?, ordered: List<Step>): String? {
        val jump = option?.nextStepId ?: step.nextStepId
        if (jump == END_OF_QUESTIONNAIRE) return null
        if (jump != null) return jump
        val index = ordered.indexOfFirst { it.id == step.id }
        return ordered.getOrNull(index + 1)?.id
    }
}
