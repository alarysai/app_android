package com.alarysai.alarysai.feature.tips.domain.model

import com.alarysai.alarysai.core.common.language.LocalizedText

/** An active tip category (e.g. Ethics, Knowledge). */
data class TipCategory(
    val id: String,
    val name: LocalizedText,
    val order: Int,
)
