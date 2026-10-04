package com.alarysai.alarysai.feature.auth.domain.repository

/** "Keep me signed in" on the login screen. Defaults to true, like Firebase itself. */
interface KeepSignedInRepository {
    fun isKeepSignedIn(): Boolean
    fun setKeepSignedIn(keep: Boolean)
}
