package com.alarysai.alarysai.feature.home.data.mapper

import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.mapper.toDomainOrNull
import com.alarysai.alarysai.core.firebase.mapper.toUrlOrNull
import com.alarysai.alarysai.feature.home.data.model.QuestionnaireCategoryDto
import com.alarysai.alarysai.feature.home.domain.model.QuestionnaireCategory

/** Null for anything that should not be shown: inactive or unknown status, or no Portuguese name. */
fun QuestionnaireCategoryDto.toDomainOrNull(id: String): QuestionnaireCategory? {
    if (status != FirestoreContract.STATUS_ACTIVE) return null
    val localizedName = name?.toDomainOrNull() ?: return null
    return QuestionnaireCategory(
        id = id,
        name = localizedName,
        iconUrl = icon.toUrlOrNull(),
        order = order.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt(),
    )
}
