package com.alarysai.alarysai.feature.auth.domain.model

/** Why an authentication request failed. The UI maps each value to a localized message. */
enum class AuthError {
    INVALID_EMAIL,

    /** Wrong e-mail or password. Firebase does not tell which, so neither does the app. */
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    NETWORK,
    TOO_MANY_REQUESTS,

    /** No Google account on the device, or Google sign-in is not set up for this app. */
    GOOGLE_UNAVAILABLE,
    UNKNOWN,
}

class AuthException(
    val error: AuthError,
    cause: Throwable? = null,
) : Exception("Authentication failed: $error", cause)
