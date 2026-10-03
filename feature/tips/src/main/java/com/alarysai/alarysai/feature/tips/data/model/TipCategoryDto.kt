package com.alarysai.alarysai.feature.tips.data.model

import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.google.firebase.firestore.IgnoreExtraProperties

/** `tipCategories/{categoryId}`. Audit fields are ignored. */
@IgnoreExtraProperties
data class TipCategoryDto(
    val name: LocalizedTextDto? = null,
    val order: Long = 0,
    val status: String = "",
)
