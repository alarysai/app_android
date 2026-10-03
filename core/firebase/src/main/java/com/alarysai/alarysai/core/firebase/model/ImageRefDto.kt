package com.alarysai.alarysai.core.firebase.model

import com.google.firebase.firestore.IgnoreExtraProperties

/** Always null until Storage uploads are enabled in the admin panel. */
@IgnoreExtraProperties
data class ImageRefDto(
    val path: String = "",
    val url: String = "",
)
