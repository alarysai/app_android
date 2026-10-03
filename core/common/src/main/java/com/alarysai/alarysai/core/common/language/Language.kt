package com.alarysai.alarysai.core.common.language

/** Content languages filled in by the admin panel. Portuguese is always present. */
enum class Language(val code: String) {
    PT("pt"),
    EN("en"),
    ES("es");

    companion object {
        /** Unknown, blank or regional codes fall back to [PT]; "en-US" resolves to [EN]. */
        fun fromCode(code: String?): Language = fromCodeOrNull(code) ?: PT

        /** Strict variant for stored data (e.g. a questionnaire's `languages`): unknown codes are null. */
        fun fromCodeOrNull(code: String?): Language? {
            val primary = code?.trim()?.substringBefore('-')?.substringBefore('_')?.lowercase()
            return entries.firstOrNull { it.code == primary }
        }
    }
}
