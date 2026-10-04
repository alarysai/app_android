package com.alarysai.alarysai.feature.auth.domain.repository

/** Every call fails with `AuthException`. Success is observed through the session, not returned. */
interface AuthRepository {
    suspend fun signIn(email: String, password: String)

    /** Creates the account and stores [name] as the provider display name. */
    suspend fun signUp(name: String, email: String, password: String)

    /** Sends the reset link. Succeeds even when no account uses [email], so nobody can probe e-mails. */
    suspend fun sendPasswordReset(email: String)

    suspend fun signInWithGoogle(idToken: String)

    fun signOut()
}
