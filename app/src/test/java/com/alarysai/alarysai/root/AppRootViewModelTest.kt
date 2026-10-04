package com.alarysai.alarysai.root

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.PreferredLanguageHolder
import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.SessionUser
import com.alarysai.alarysai.core.common.session.UserProfile
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.auth.domain.model.AuthGate
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.alarysai.alarysai.feature.auth.domain.repository.KeepSignedInRepository
import com.alarysai.alarysai.feature.auth.domain.usecase.ApplyKeepSignedInUseCase
import com.alarysai.alarysai.feature.auth.domain.usecase.ObserveAuthGateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class AppRootViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val marina = SessionUser("u1", "marina@email.com", "Marina")
    private val user = MutableStateFlow<SessionUser?>(marina)
    private val profiles = MutableStateFlow(mapOf("u1" to UserProfile("Marina", null, Language.ES)))
    private val events = mutableListOf<String>()

    private val session = object : SessionRepository {
        override fun observeUser(): Flow<SessionUser?> = user.also { events += "observe" }
        override fun observeUserId(): Flow<String?> = user.map { it?.uid }
    }
    private val profileRepository = object : UserProfileRepository {
        override fun observeProfile(uid: String): Flow<UserProfile?> = profiles.map { it[uid] }
        override suspend fun saveProfile(uid: String, displayName: String, language: Language, isNew: Boolean) = Unit
    }
    private val auth = object : AuthRepository {
        override suspend fun signIn(email: String, password: String) = Unit
        override suspend fun signUp(name: String, email: String, password: String) = Unit
        override suspend fun sendPasswordReset(email: String) = Unit
        override suspend fun signInWithGoogle(idToken: String) = Unit
        override fun signOut() {
            events += "signOut"
            user.value = null
        }
    }

    private fun viewModel(keep: Boolean, holder: PreferredLanguageHolder = PreferredLanguageHolder()) = AppRootViewModel(
        ApplyKeepSignedInUseCase(
            object : KeepSignedInRepository {
                override fun isKeepSignedIn() = keep
                override fun setKeepSignedIn(keep: Boolean) = Unit
            },
            auth,
        ),
        ObserveAuthGateUseCase(session, profileRepository),
        holder,
    )

    @Test
    fun `a kept session opens the app in the profile language`() {
        val holder = PreferredLanguageHolder()
        val viewModel = viewModel(keep = true, holder = holder)

        assertEquals(AuthGate.Ready(marina, UserProfile("Marina", null, Language.ES)), viewModel.gate.value)
        assertEquals(Language.ES, holder.language)
    }

    @Test
    fun `a session not kept is ended before anything is shown`() {
        val viewModel = viewModel(keep = false)

        assertEquals(listOf("signOut", "observe"), events)
        assertEquals(AuthGate.SignedOut, viewModel.gate.value)
    }

    @Test
    fun `signing out clears the profile language`() {
        val holder = PreferredLanguageHolder()
        val viewModel = viewModel(keep = true, holder = holder)

        user.value = null

        assertEquals(AuthGate.SignedOut, viewModel.gate.value)
        assertNull(holder.language)
    }

    @Test
    fun `first access asks for the profile`() {
        profiles.value = emptyMap()

        assertEquals(AuthGate.NeedsProfile(marina), viewModel(keep = true).gate.value)
    }
}
