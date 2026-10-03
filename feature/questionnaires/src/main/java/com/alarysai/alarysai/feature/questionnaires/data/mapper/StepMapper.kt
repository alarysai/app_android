package com.alarysai.alarysai.feature.questionnaires.data.mapper

import com.alarysai.alarysai.core.firebase.mapper.toDomainOrNull
import com.alarysai.alarysai.core.firebase.mapper.toUrlOrNull
import com.alarysai.alarysai.feature.questionnaires.data.model.InfoFlagDto
import com.alarysai.alarysai.feature.questionnaires.data.model.StepDto
import com.alarysai.alarysai.feature.questionnaires.data.model.StepOptionDto
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.domain.model.InfoFlag
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepOption
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepType

/**
 * Defensive mapping: a step the app cannot show (unknown type, no text and no image, video
 * without an `https://` link) is dropped. Jumps to a dropped step then end the questionnaire.
 */
fun StepDto.toDomainOrNull(id: String): Step? {
    val stepType = when (type) {
        "question" -> StepType.QUESTION
        "video" -> StepType.VIDEO
        else -> return null
    }
    val localizedText = text?.toDomainOrNull()
    val imageUrl = image.toUrlOrNull()
    if (localizedText == null && imageUrl == null) return null
    val safeVideoUrl = videoUrl?.trim()?.takeIf { it.startsWith("https://") }
    if (stepType == StepType.VIDEO && safeVideoUrl == null) return null
    val domainOptions = if (stepType == StepType.QUESTION) options.toDomainOptions() else emptyList()
    val resolvedAnswerType = resolveAnswerType(answerType, domainOptions)
    return Step(
        id = id,
        order = order.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt(),
        type = stepType,
        text = localizedText,
        imageUrl = imageUrl,
        videoUrl = if (stepType == StepType.VIDEO) safeVideoUrl else null,
        // Open text has no options to choose from.
        options = if (resolvedAnswerType == AnswerType.OPEN_TEXT) emptyList() else domainOptions,
        nextStepId = nextStepId.blankToNull(),
        partOfPrompt = partOfPrompt,
        promptInstruction = promptInstruction.blankToNull(),
        infoFlag = infoFlag?.toDomainOrNull(),
        answerType = resolvedAnswerType,
        helpText = helpText?.toDomainOrNull(),
        required = required ?: true,
        maxLength = (maxLength ?: Step.DEFAULT_MAX_LENGTH.toLong()).coerceIn(1, MAX_TEXT_LENGTH).toInt(),
        placeholder = placeholder?.toDomainOrNull(),
    )
}

private const val MAX_TEXT_LENGTH = 5_000L

/**
 * Proposed `answerType`. Missing or unknown values keep the original behavior (single choice);
 * "yes_no" needs exactly two options, otherwise it is shown as a regular single choice.
 */
private fun resolveAnswerType(value: String?, options: List<StepOption>): AnswerType = when (value) {
    "multiple_choice" -> AnswerType.MULTIPLE_CHOICE
    "open_text" -> AnswerType.OPEN_TEXT
    "yes_no" -> if (options.size == 2) AnswerType.YES_NO else AnswerType.SINGLE_CHOICE
    else -> AnswerType.SINGLE_CHOICE
}

/** Without a Portuguese label there is nothing to show, so the flag is dropped (the step stays). */
private fun InfoFlagDto.toDomainOrNull(): InfoFlag? {
    val localizedLabel = label?.toDomainOrNull() ?: return null
    return InfoFlag(label = localizedLabel, value = value, tipId = tipId.blankToNull())
}

/** Drops options without ID or without text and image; keeps the first of a repeated ID. */
private fun List<StepOptionDto>.toDomainOptions(): List<StepOption> =
    mapNotNull { it.toDomainOrNull() }.distinctBy { it.id }

private fun StepOptionDto.toDomainOrNull(): StepOption? {
    val optionId = id.trim().ifEmpty { return null }
    val localizedText = text?.toDomainOrNull()
    val imageUrl = image.toUrlOrNull()
    if (localizedText == null && imageUrl == null) return null
    return StepOption(
        id = optionId,
        text = localizedText,
        imageUrl = imageUrl,
        promptInstruction = promptInstruction.blankToNull(),
        nextStepId = nextStepId.blankToNull(),
        tipId = tipId.blankToNull(),
    )
}

private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
