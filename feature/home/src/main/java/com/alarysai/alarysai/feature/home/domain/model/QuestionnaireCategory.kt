package com.alarysai.alarysai.feature.home.domain.model

import com.alarysai.alarysai.core.common.language.LocalizedText

/** An active category shown on the home grid. Only active categories reach the domain. */
data class QuestionnaireCategory(
    val id: String,
    val name: LocalizedText,
    val iconUrl: String?,
    val order: Int,
)
