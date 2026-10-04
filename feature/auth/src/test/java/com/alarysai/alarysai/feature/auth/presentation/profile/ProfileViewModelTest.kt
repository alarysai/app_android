package com.alarysai.alarysai.feature.auth.presentation.profile

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.common.language.PreferredLanguageHolder
import com.alarysai.alarysai.core.common.session.SessionUser
import com.alarysai.alarysai.core.common.session.UserProfile
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.auth.FakeAuthRepository
import com.alarysai.alarysai.feature.auth.FakeProfiles
import com.alarysai.alarysai.feature.auth.FakeSession
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val marina = SessionUser("u1", "marina.alves@email.com", null)
    private val session = FakeSession(marina)
    private val profiles = FakeProfiles()
    private val auth = FakeAuthRepository()
    private val preferred = PreferredLanguageHolder()

    private fun viewModel(mode: String? = null) = ProfileViewModel(
        SavedStateHandle(buildMap { mode?.let { put(ProfileViewModel.ARG_MODE, it) } }),
        session,
        profiles,
        auth,
        preferred,
        object : LanguageProvider {
            override fun currentLanguage() = Language.PT
        },
    )

    @Test
    fun `setup shows the e-mail and fills the name once the provider sends it`() {
        val viewModel = viewModel()

        assertEquals(ProfileMode.SETUP, viewModel.uiState.value.mode)
        assertEquals("marina.alves@email.com", viewModel.uiState.value.email)
        assertEquals("", viewModel.uiState.value.name)

        session.user.value = marina.copy(displayName = "Marina Alves")
        assertEquals("Marina Alves", viewModel.uiState.value.name)
        assertEquals("MA", viewModel.uiState.value.initials)
    }

    @Test
    fun `starting creates the profile with the chosen name and language`() {
        val viewModel = viewModel()
        viewModel.onAction(ProfileUiAction.NameChanged("Marina"))
        viewModel.onAction(ProfileUiAction.LanguageSelected(Language.ES))

        viewModel.onAction(ProfileUiAction.SaveClicked)

        assertEquals(listOf("u1:Marina:es:new"), profiles.saved)
        assertEquals(Language.ES, preferred.language)
    }

    @Test
    fun `a blank name is not saved`() {
        val viewModel = viewModel()

        viewModel.onAction(ProfileUiAction.SaveClicked)

        assertTrue(viewModel.uiState.value.showNameError)
        assertEquals(emptyList<String>(), profiles.saved)
    }

    @Test
    fun `edit mode loads the profile, keeps typed values and announces the save`() = runTest {
        profiles.profiles.value = mapOf("u1" to UserProfile("Marina Alves", null, Language.EN))
        val viewModel = viewModel(mode = ProfileViewModel.MODE_EDIT)
        assertEquals("Marina Alves", viewModel.uiState.value.name)
        assertEquals(Language.EN, viewModel.uiState.value.language)

        viewModel.onAction(ProfileUiAction.NameChanged("Marina A."))
        profiles.profiles.value = mapOf("u1" to UserProfile("Outro Nome", null, Language.EN))
        assertEquals("Marina A.", viewModel.uiState.value.name)

        viewModel.events.test {
            viewModel.onAction(ProfileUiAction.SaveClicked)
            assertEquals(ProfileUiEvent.Saved, awaitItem())
        }
        assertEquals(listOf("u1:Marina A.:en:update"), profiles.saved)
    }

    @Test
    fun `a failed save is reported and signing out ends the session`() {
        profiles.failSave = ContentLoadException(ContentLoadError.OFFLINE)
        val viewModel = viewModel()
        viewModel.onAction(ProfileUiAction.NameChanged("Marina"))

        viewModel.onAction(ProfileUiAction.SaveClicked)
        assertTrue(viewModel.uiState.value.saveFailed)
        assertFalse(viewModel.uiState.value.isSaving)

        viewModel.onAction(ProfileUiAction.SignOutClicked)
        assertEquals(listOf("signOut"), auth.calls)
    }
}
