package com.alarysai.alarysai.feature.tips.data.mapper

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.mapper.toDomainOrNull
import com.alarysai.alarysai.core.firebase.mapper.toUrlOrNull
import com.alarysai.alarysai.feature.tips.data.model.TipCategoryDto
import com.alarysai.alarysai.feature.tips.data.model.TipDto
import com.alarysai.alarysai.feature.tips.domain.model.Tip
import com.alarysai.alarysai.feature.tips.domain.model.TipCategory

/** Null for a category that should not be shown: not active, or without a Portuguese name. */
fun TipCategoryDto.toDomainOrNull(id: String): TipCategory? {
    if (status != FirestoreContract.STATUS_ACTIVE) return null
    val localizedName = name?.toDomainOrNull() ?: return null
    return TipCategory(id = id, name = localizedName, order = order.toSafeInt())
}

/** Null for a tip that should not be shown: not active, without category, or without a Portuguese text. */
fun TipDto.toDomainOrNull(id: String): Tip? {
    if (status != FirestoreContract.STATUS_ACTIVE) return null
    if (categoryId.isBlank()) return null
    val localizedText = text?.toDomainOrNull() ?: return null
    return Tip(
        id = id,
        categoryId = categoryId,
        text = localizedText,
        imageUrl = image.toUrlOrNull(),
        // Portuguese is always complete, even if the panel ever omits it from the list.
        languages = languages.mapNotNull(Language::fromCodeOrNull).toSet() + Language.PT,
        order = order.toSafeInt(),
    )
}

private fun Long.toSafeInt(): Int = coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
