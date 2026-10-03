package com.alarysai.alarysai.feature.questionnaires.data.model

import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * `questionnaires/{questionnaireId}/steps/{stepId}`. Audit fields are ignored.
 * `answerType`, `helpText`, `required`, `maxLength` and `placeholder` are proposed fields
 * (docs/proposta-questionario-v2.md); absent, the step behaves as before.
 */
@IgnoreExtraProperties
data class StepDto(
    val order: Long = 0,
    val type: String = "",
    val text: LocalizedTextDto? = null,
    val image: ImageRefDto? = null,
    val videoUrl: String? = null,
    val options: List<StepOptionDto> = emptyList(),
    val nextStepId: String? = null,
    val partOfPrompt: Boolean = false,
    val promptInstruction: String? = null,
    val infoFlag: InfoFlagDto? = null,
    val answerType: String? = null,
    val helpText: LocalizedTextDto? = null,
    val required: Boolean? = null,
    val maxLength: Long? = null,
    val placeholder: LocalizedTextDto? = null,
)

@IgnoreExtraProperties
data class InfoFlagDto(
    val label: LocalizedTextDto? = null,
    val value: Boolean = false,
    val tipId: String? = null,
)

@IgnoreExtraProperties
data class StepOptionDto(
    val id: String = "",
    val text: LocalizedTextDto? = null,
    val image: ImageRefDto? = null,
    val promptInstruction: String? = null,
    val nextStepId: String? = null,
    /** Proposed: tip shown while this option is selected. */
    val tipId: String? = null,
)
