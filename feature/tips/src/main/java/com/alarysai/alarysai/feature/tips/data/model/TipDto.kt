package com.alarysai.alarysai.feature.tips.data.model

import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.google.firebase.firestore.IgnoreExtraProperties

/** `tips/{tipId}`. Audit fields are ignored. */
@IgnoreExtraProperties
data class TipDto(
    val categoryId: String = "",
    val text: LocalizedTextDto? = null,
    val image: ImageRefDto? = null,
    val languages: List<String> = emptyList(),
    val order: Long = 0,
    val status: String = "",
)
