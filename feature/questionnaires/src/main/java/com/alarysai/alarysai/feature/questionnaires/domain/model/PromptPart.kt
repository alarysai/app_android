package com.alarysai.alarysai.feature.questionnaires.domain.model

/**
 * What one answered step contributes to the AI prompt. How the parts become the final prompt
 * is decided by the generation service (server), which receives them as they are.
 */
data class PromptPart(
    val stepId: String,
    /** Text of the chosen option in the language used; null for videos or image-only options. */
    val answer: String?,
    /** Step instruction, then option instruction, when present. Never translated. */
    val instructions: List<String>,
)
