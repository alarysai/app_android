package com.alarysai.alarysai.feature.auth.domain.usecase

/** Plain format check (something@domain.tld); the provider has the final word. */
object EmailValidator {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

    fun isValid(email: String): Boolean = EMAIL.matches(email.trim())
}
