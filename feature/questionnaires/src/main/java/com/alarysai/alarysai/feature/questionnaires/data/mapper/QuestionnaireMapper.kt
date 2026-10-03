package com.alarysai.alarysai.feature.questionnaires.data.mapper

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.mapper.toDomainOrNull
import com.alarysai.alarysai.core.firebase.mapper.toUrlOrNull
import com.alarysai.alarysai.feature.questionnaires.data.model.QuestionnaireDto
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire

/** Null for anything that should not be shown: not published, no category, or no Portuguese title. */
fun QuestionnaireDto.toDomainOrNull(id: String): Questionnaire? {
    if (status != FirestoreContract.STATUS_PUBLISHED) return null
    if (categoryId.isBlank()) return null
    val localizedTitle = title?.toDomainOrNull() ?: return null
    return Questionnaire(
        id = id,
        categoryId = categoryId,
        title = localizedTitle,
        description = description?.toDomainOrNull(),
        imageUrl = image.toUrlOrNull(),
        // Portuguese is always complete, even if the panel ever omits it from the list.
        languages = languages.mapNotNull(Language::fromCodeOrNull).toSet() + Language.PT,
        order = order.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt(),
        creditCost = creditCost?.takeIf { it >= 0 }?.coerceAtMost(Int.MAX_VALUE.toLong())?.toInt(),
    )
}
