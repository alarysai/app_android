package com.alarysai.alarysai.feature.questionnaires.data.model

import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.google.firebase.firestore.IgnoreExtraProperties

/** `questionnaires/{questionnaireId}`. Audit fields and `publishedAt` are ignored. */
@IgnoreExtraProperties
data class QuestionnaireDto(
    val title: LocalizedTextDto? = null,
    val description: LocalizedTextDto? = null,
    val categoryId: String = "",
    val image: ImageRefDto? = null,
    val languages: List<String> = emptyList(),
    val order: Long = 0,
    val status: String = "",
    /** Proposed: credits one generation costs. */
    val creditCost: Long? = null,
)
