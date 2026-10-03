package com.alarysai.alarysai.feature.questionnaires.data.mapper

import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.mapper.toDomainOrNull
import com.alarysai.alarysai.core.firebase.mapper.toUrlOrNull
import com.alarysai.alarysai.feature.questionnaires.data.model.TipDto
import com.alarysai.alarysai.feature.questionnaires.domain.model.Tip

/** Null for a tip that should not be shown: not active, or without a Portuguese text. */
fun TipDto.toDomainOrNull(id: String): Tip? {
    if (status != FirestoreContract.STATUS_ACTIVE) return null
    val localizedText = text?.toDomainOrNull() ?: return null
    return Tip(id = id, text = localizedText, imageUrl = image.toUrlOrNull())
}
