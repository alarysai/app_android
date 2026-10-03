package com.alarysai.alarysai.feature.questionnaires.domain.model

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LocalizedText

/** A published questionnaire. Drafts never reach the domain. */
data class Questionnaire(
    val id: String,
    val categoryId: String,
    val title: LocalizedText,
    val description: LocalizedText?,
    val imageUrl: String?,
    /** Languages with a complete translation (title, steps and options), computed by the panel. */
    val languages: Set<Language>,
    val order: Int,
    /** Proposed `creditCost`: credits one generation costs; null while the panel does not set it. */
    val creditCost: Int? = null,
)
