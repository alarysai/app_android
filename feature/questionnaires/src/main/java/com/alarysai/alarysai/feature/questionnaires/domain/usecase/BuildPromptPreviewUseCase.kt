package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.feature.questionnaires.domain.model.PromptPart
import javax.inject.Inject

/**
 * Text shown as "prompt preview" on the review screen: one line per part, the instructions
 * followed by the answer ("Write a short text. Focus on ethics: Ethics").
 *
 * It is only a preview. The final prompt is assembled by the generation service, which may use a
 * questionnaire template (proposed `promptTemplate`, see docs/proposta-questionario-v2.md).
 * Null when nothing goes into the prompt.
 */
class BuildPromptPreviewUseCase @Inject constructor() {

    operator fun invoke(parts: List<PromptPart>): String? =
        parts.mapNotNull { it.toLine() }
            .joinToString("\n")
            .takeIf { it.isNotEmpty() }

    private fun PromptPart.toLine(): String? {
        val instructionText = instructions.joinToString(" ").trim()
        return when {
            instructionText.isEmpty() -> answer
            answer == null -> instructionText
            else -> "${instructionText.trimEnd('.', ':')}: $answer"
        }
    }
}
