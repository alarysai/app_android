package com.alarysai.alarysai.feature.home.data.model

import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.google.firebase.firestore.IgnoreExtraProperties

/** `questionnaireCategories/{categoryId}`. Audit fields (createdAt, updatedBy...) are ignored. */
@IgnoreExtraProperties
data class QuestionnaireCategoryDto(
    val name: LocalizedTextDto? = null,
    val icon: ImageRefDto? = null,
    val order: Long = 0,
    val status: String = "",
)
