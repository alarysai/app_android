package com.alarysai.alarysai.feature.questionnaires.data.model

import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.google.firebase.firestore.IgnoreExtraProperties

/** `tips/{tipId}`, only what a step needs. `categoryId`, `languages` and `order` belong to the tips screens. */
@IgnoreExtraProperties
data class TipDto(
    val text: LocalizedTextDto? = null,
    val image: ImageRefDto? = null,
    val status: String = "",
)
