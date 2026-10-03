package com.alarysai.alarysai.feature.tips.domain.model

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LocalizedText

/** An active tip. */
data class Tip(
    val id: String,
    val categoryId: String,
    val text: LocalizedText,
    val imageUrl: String?,
    /** Languages with a complete translation, computed by the panel. Always contains PT. */
    val languages: Set<Language>,
    val order: Int,
)
