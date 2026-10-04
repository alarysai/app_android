package com.alarysai.alarysai.core.firebase.session

import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.SessionUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Firebase Auth session; emits on sign in, sign out and when the display name is updated. */
class FirebaseSessionRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : SessionRepository {

    override fun observeUser(): Flow<SessionUser?> = callbackFlow {
        val emitCurrent = { trySend(auth.currentUser?.let { SessionUser(it.uid, it.email, it.displayName) }) }
        val authListener = FirebaseAuth.AuthStateListener { emitCurrent() }
        // Token refreshes also fire after updateProfile, which brings the new display name.
        val idTokenListener = FirebaseAuth.IdTokenListener { emitCurrent() }
        auth.addAuthStateListener(authListener)
        auth.addIdTokenListener(idTokenListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            auth.removeIdTokenListener(idTokenListener)
        }
    }.distinctUntilChanged()

    override fun observeUserId(): Flow<String?> = observeUser().map { it?.uid }.distinctUntilChanged()
}
