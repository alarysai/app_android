package com.alarysai.alarysai.core.common.content

/** Why content could not be loaded. The UI layer maps each value to a localized message. */
enum class ContentLoadError {
    /** No network and nothing cached yet. */
    OFFLINE,

    /** The content was unpublished, deactivated or removed. */
    UNAVAILABLE,

    /** Anything else; shown as a generic "try again" message. */
    UNKNOWN,
}
