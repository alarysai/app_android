package com.alarysai.alarysai.feature.auth

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.SessionUser
import com.alarysai.alarysai.core.common.session.UserProfile
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import com.alarysai.alarysai.feature.auth.domain.model.AuthException
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.alarysai.alarysai.feature.auth.domain.repository.KeepSignedInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Records every call; [failWith] makes the next calls fail. */
class FakeAuthRepository(var failWith: AuthError? = null) : AuthRepository {
    val calls = mutableListOf<String>()

    override suspend fun signIn(email: String, password: String) = record("signIn:$email:$password")
    override suspend fun signUp(name: String, email: String, password: String) = record("signUp:$name:$email:$password")
    override suspend fun sendPasswordReset(email: String) = record("reset:$email")
    override suspend fun signInWithGoogle(idToken: String) = record("google:$idToken")
    override fun signOut() {
        calls += "signOut"
    }

    private fun record(call: String) {
        calls += call
        failWith?.let { throw AuthException(it) }
    }
}

class FakeKeepSignedIn(var keep: Boolean = true) : KeepSignedInRepository {
    override fun isKeepSignedIn() = keep
    override fun setKeepSignedIn(keep: Boolean) {
        this.keep = keep
    }
}

class FakeSession(user: SessionUser? = null) : SessionRepository {
    val user = MutableStateFlow(user)
    override fun observeUser(): Flow<SessionUser?> = user
    override fun observeUserId(): Flow<String?> = user.map { it?.uid }
}

/** Profiles by UID; a missing entry is a missing document. */
class FakeProfiles(profiles: Map<String, UserProfile> = emptyMap()) : UserProfileRepository {
    val profiles = MutableStateFlow(profiles)
    var failSave: Exception? = null
    val saved = mutableListOf<String>()

    override fun observeProfile(uid: String): Flow<UserProfile?> = profiles.map { it[uid] }

    override suspend fun saveProfile(uid: String, displayName: String, language: Language, isNew: Boolean) {
        failSave?.let { throw it }
        saved += "$uid:$displayName:${language.code}:${if (isNew) "new" else "update"}"
        profiles.value = profiles.value + (uid to UserProfile(displayName, null, language))
    }
}
