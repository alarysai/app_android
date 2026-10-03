package com.alarysai.alarysai.feature.history.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/** `users/{uid}/history/{entryId}`. `answers`, `prompt`, `language` and `questionnaireId` are not shown. */
@IgnoreExtraProperties
data class HistoryEntryDto(
    val questionnaireTitle: String? = null,
    val outputType: String = "",
    val result: HistoryResultDto? = null,
    val creditsSpent: Long = 0,
    val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class HistoryResultDto(
    val text: String? = null,
    val url: String? = null,
)
