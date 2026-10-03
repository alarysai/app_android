package com.alarysai.alarysai.core.common.text

import java.text.Normalizer

private val DIACRITICS = Regex("\\p{Mn}+")
private val SPACES = Regex("\\s+")

/** Lower case, no accents, single spaces: "  Vídeo  IA " -> "video ia". */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .lowercase()
        .trim()
        .replace(SPACES, " ")

/** True when [query] is blank or appears in this text, ignoring case and accents. */
fun String.matchesSearch(query: String): Boolean {
    val normalizedQuery = query.normalizedForSearch()
    return normalizedQuery.isEmpty() || normalizedForSearch().contains(normalizedQuery)
}
