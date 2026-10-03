package com.alarysai.alarysai.feature.advertisers.domain.model

/** An active advertiser. Always has [name] or [imageUrl]; [link] is always `https://`. */
data class Advertiser(
    val id: String,
    /** Not translated. */
    val name: String?,
    val imageUrl: String?,
    /** Free text set in the panel, tidied (trimmed, single spaces). */
    val type: String,
    val link: String,
    val order: Int,
)

/** Advertisers sharing a type. [type] is null for advertisers without one. */
data class AdvertiserGroup(
    val type: String?,
    val advertisers: List<Advertiser>,
)
