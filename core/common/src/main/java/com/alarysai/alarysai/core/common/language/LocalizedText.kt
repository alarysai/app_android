package com.alarysai.alarysai.core.common.language

/** Text translated by the admin panel. [pt] is always filled; [en] and [es] may be missing. */
data class LocalizedText(
    val pt: String,
    val en: String? = null,
    val es: String? = null,
) {
    /** The user's language, falling back to Portuguese. */
    fun resolve(language: Language): String = when (language) {
        Language.PT -> pt
        Language.EN -> en ?: pt
        Language.ES -> es ?: pt
    }
}
