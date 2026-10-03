package com.alarysai.alarysai.feature.questionnaires.domain

import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.feature.questionnaires.domain.model.InfoFlag
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepOption
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepType

/** Builders for flow tests: only the fields the flow looks at need to be set. */
fun question(
    id: String,
    order: Int,
    vararg options: StepOption,
    nextStepId: String? = null,
    infoFlag: InfoFlag? = null,
) = Step(
    id = id,
    order = order,
    type = StepType.QUESTION,
    text = LocalizedText(pt = "Pergunta $id"),
    imageUrl = null,
    videoUrl = null,
    options = options.toList(),
    nextStepId = nextStepId,
    partOfPrompt = false,
    promptInstruction = null,
    infoFlag = infoFlag,
)

fun video(id: String, order: Int, nextStepId: String? = null, infoFlag: InfoFlag? = null) = Step(
    id = id,
    order = order,
    type = StepType.VIDEO,
    text = LocalizedText(pt = "Vídeo $id"),
    imageUrl = null,
    videoUrl = "https://www.youtube.com/watch?v=$id",
    options = emptyList(),
    nextStepId = nextStepId,
    partOfPrompt = false,
    promptInstruction = null,
    infoFlag = infoFlag,
)

fun option(id: String, nextStepId: String? = null) = StepOption(
    id = id,
    text = LocalizedText(pt = "Opção $id"),
    imageUrl = null,
    promptInstruction = null,
    nextStepId = nextStepId,
)
