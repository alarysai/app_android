package com.alarysai.alarysai.core.common.session

import kotlinx.coroutines.flow.Flow

/**
 * Who is signed in. User data (`users/{uid}`, history, credits) can only be read by its owner,
 * so screens that show it start from here.
 */
interface SessionRepository {

    /** The signed-in user's ID, or null when nobody is signed in. Emits again on sign in/out. */
    fun observeUserId(): Flow<String?>
}
