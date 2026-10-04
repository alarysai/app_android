package com.alarysai.alarysai.feature.auth.domain.usecase

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.session.SessionUser
import com.alarysai.alarysai.core.common.session.UserProfile
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.feature.auth.FakeAuthRepository
import com.alarysai.alarysai.feature.auth.FakeKeepSignedIn
import com.alarysai.alarysai.feature.auth.FakeProfiles
import com.alarysai.alarysai.feature.auth.FakeSession
import com.alarysai.alarysai.feature.auth.domain.model.AuthGate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionUseCasesTest {

    private val marina = SessionUser("u1", "marina@email.com", "Marina")
    private val profile = UserProfile("Marina Alves", null, Language.EN)

    @Test
    fun `gate follows sign-in, first access and sign-out`() = runTest {
        val session = FakeSession()
        val profiles = FakeProfiles()

        ObserveAuthGateUseCase(session, profiles)().test {
            assertEquals(AuthGate.SignedOut, awaitItem())

            session.user.value = marina
            assertEquals(AuthGate.NeedsProfile(marina), awaitItem())

            profiles.profiles.value = mapOf("u1" to profile)
            assertEquals(AuthGate.Ready(marina, profile), awaitItem())

            session.user.value = null
            assertEquals(AuthGate.SignedOut, awaitItem())
        }
    }

    @Test
    fun `an unreadable profile still opens the app with the provider data`() = runTest {
        val failing = object : UserProfileRepository {
            override fun observeProfile(uid: String): Flow<UserProfile?> = flow { throw IllegalStateException("offline") }
            override suspend fun saveProfile(uid: String, displayName: String, language: Language, isNew: Boolean) = Unit
        }

        ObserveAuthGateUseCase(FakeSession(marina), failing)().test {
            assertEquals(AuthGate.Ready(marina, UserProfile("Marina", null, null)), awaitItem())
            // The session keeps being observed after the profile failure.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a session the user chose not to keep is ended at start`() {
        val auth = FakeAuthRepository()

        ApplyKeepSignedInUseCase(FakeKeepSignedIn(keep = true), auth)()
        assertEquals(emptyList<String>(), auth.calls)

        ApplyKeepSignedInUseCase(FakeKeepSignedIn(keep = false), auth)()
        assertEquals(listOf("signOut"), auth.calls)
    }
}
