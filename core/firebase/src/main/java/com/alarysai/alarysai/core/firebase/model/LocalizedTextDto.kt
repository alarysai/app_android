package com.alarysai.alarysai.core.firebase.model

import com.google.firebase.firestore.IgnoreExtraProperties

/** Default values let `toObject()` tolerate missing fields. */
@IgnoreExtraProperties
data class LocalizedTextDto(
    val pt: String = "",
    val en: String? = null,
    val es: String? = null,
)
