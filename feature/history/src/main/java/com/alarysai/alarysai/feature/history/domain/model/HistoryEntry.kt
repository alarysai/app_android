package com.alarysai.alarysai.feature.history.domain.model

/** One questionnaire run and what was generated. Written by the generation service only. */
data class HistoryEntry(
    val id: String,
    /** Copy of the title in the language used; null when missing. */
    val questionnaireTitle: String?,
    val outputType: OutputType,
    val resultText: String?,
    /** Link to the generated file; only `https://` links are kept. */
    val resultUrl: String?,
    val creditsSpent: Int,
    val createdAtMillis: Long?,
)

/** Unknown values become [OTHER] instead of hiding the entry. */
enum class OutputType { TEXT, IMAGE, VIDEO, SLIDES, OTHER }
