package com.alarysai.alarysai.feature.advertisers.data.mapper

import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.mapper.toUrlOrNull
import com.alarysai.alarysai.feature.advertisers.data.model.AdvertiserDto
import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser

private val SPACES = Regex("\\s+")

/**
 * Null for an advertiser that should not be shown: not active, without name and image, or
 * whose link is not `https://` (the app only opens safe links).
 */
fun AdvertiserDto.toDomainOrNull(id: String): Advertiser? {
    if (status != FirestoreContract.STATUS_ACTIVE) return null
    val safeLink = link.trim().takeIf { it.startsWith("https://") } ?: return null
    val tidyName = name?.trim()?.takeIf { it.isNotEmpty() }
    val imageUrl = image.toUrlOrNull()
    if (tidyName == null && imageUrl == null) return null
    return Advertiser(
        id = id,
        name = tidyName,
        imageUrl = imageUrl,
        // Same tidying as the panel's tidyType: trimmed, single spaces.
        type = type.trim().replace(SPACES, " "),
        link = safeLink,
        order = order.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt(),
    )
}
