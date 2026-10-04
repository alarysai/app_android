package com.alarysai.alarysai.core.common.session

import kotlinx.coroutines.flow.Flow

/** The signed-in account, as the authentication provider knows it. */
data class SessionUser(
    val uid: String,
    val email: String?,
    /** Name from the provider (Google, or the one typed at sign-up); the profile may differ. */
    val displayName: String?,
)

/**
 * Who is signed in. User data (`users/{uid}`, history, credits) can only be read by its owner,
 * so screens that show it start from here.
 */
interface SessionRepository {

    /** The signed-in user, or null when nobody is signed in. Emits again on sign in/out. */
    fun observeUser(): Flow<SessionUser?>

    /** Shortcut for screens that only need the ID. */
    fun observeUserId(): Flow<String?>
}
