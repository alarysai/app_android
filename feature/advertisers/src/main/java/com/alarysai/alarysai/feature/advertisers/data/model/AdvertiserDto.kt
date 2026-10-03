package com.alarysai.alarysai.feature.advertisers.data.model

import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.google.firebase.firestore.IgnoreExtraProperties

/** `advertisers/{advertiserId}`. Audit fields are ignored. */
@IgnoreExtraProperties
data class AdvertiserDto(
    val name: String? = null,
    val image: ImageRefDto? = null,
    val type: String = "",
    val link: String = "",
    val order: Long = 0,
    val status: String = "",
)
