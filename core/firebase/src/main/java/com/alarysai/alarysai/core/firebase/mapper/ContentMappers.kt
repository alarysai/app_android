package com.alarysai.alarysai.core.firebase.mapper

import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto

/** Null when Portuguese is blank: the panel always fills it, so the text is unusable. */
fun LocalizedTextDto.toDomainOrNull(): LocalizedText? {
    val portuguese = pt.trim().ifEmpty { return null }
    return LocalizedText(
        pt = portuguese,
        en = en?.trim()?.takeIf { it.isNotEmpty() },
        es = es?.trim()?.takeIf { it.isNotEmpty() },
    )
}

/** "No image" for a missing reference or a blank URL. */
fun ImageRefDto?.toUrlOrNull(): String? = this?.url?.trim()?.takeIf { it.isNotEmpty() }
