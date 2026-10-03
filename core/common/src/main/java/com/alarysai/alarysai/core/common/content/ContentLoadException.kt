package com.alarysai.alarysai.core.common.content

/**
 * Thrown by repositories so the presentation layer can react to [error]
 * without knowing which backend failed.
 */
class ContentLoadException(
    val error: ContentLoadError,
    cause: Throwable? = null,
) : Exception("Content load failed: $error", cause)

fun Throwable.toContentLoadErrorOrUnknown(): ContentLoadError =
    (this as? ContentLoadException)?.error ?: ContentLoadError.UNKNOWN
