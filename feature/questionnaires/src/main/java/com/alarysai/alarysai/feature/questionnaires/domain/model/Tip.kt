package com.alarysai.alarysai.feature.questionnaires.domain.model

import com.alarysai.alarysai.core.common.language.LocalizedText

/** An active tip shown next to the informative yes/no of a step. */
data class Tip(
    val id: String,
    val text: LocalizedText,
    val imageUrl: String?,
)
