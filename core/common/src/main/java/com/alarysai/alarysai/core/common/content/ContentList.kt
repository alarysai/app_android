package com.alarysai.alarysai.core.common.content

/**
 * A list of published content as last seen by the device.
 *
 * [isFromCache] is true when the list came from the offline cache instead of the server,
 * so the screen can show a discreet "offline" notice.
 */
data class ContentList<T>(
    val items: List<T>,
    val isFromCache: Boolean,
)
