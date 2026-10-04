package com.alarysai.alarysai.feature.auth.presentation.login

import app.cash.turbine.test
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.auth.FakeAuthRepository
import com.alarysai.alarysai.feature.auth.FakeKeepSignedIn
import com.alarysai.alarysai.feature.auth.di.GoogleSignInConfig
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val keep = FakeKeepSignedIn(keep = true)

    private fun viewModel() = LoginViewModel(auth, keep, GoogleSignInConfig("web-client"))

    private fun LoginViewModel.fill(email: String, password: String) {
        onAction(LoginUiAction.EmailChanged(email))
        onAction(LoginUiAction.PasswordChanged(password))
    }

    @Test
    fun `starts with the saved keep-signed-in choice and the Google client`() {
        keep.keep = false
        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.keepSignedIn)
        assertEquals("web-client", viewModel.googleWebClientId)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `signs in with the typed credentials and saves keep signed in`() {
        val viewModel = viewModel()
        viewModel.fill("marina@email.com", "segredo123")
        viewModel.onAction(LoginUiAction.KeepSignedInChanged(false))

        viewModel.onAction(LoginUiAction.SignInClicked)

        assertEquals(listOf("signIn:marina@email.com:segredo123"), auth.calls)
        assertFalse(keep.keep)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `a malformed e-mail is flagged without calling the provider`() {
        val viewModel = viewModel()
        viewModel.fill("marina", "segredo123")

        viewModel.onAction(LoginUiAction.SignInClicked)

        assertTrue(viewModel.uiState.value.showEmailError)
        assertEquals(emptyList<String>(), auth.calls)
        viewModel.onAction(LoginUiAction.EmailChanged("marina@email.com"))
        assertFalse(viewModel.uiState.value.showEmailError)
    }

    @Test
    fun `wrong credentials show the error until the user types again`() {
        auth.failWith = AuthError.INVALID_CREDENTIALS
        val viewModel = viewModel()
        viewModel.fill("marina@email.com", "errada")

        viewModel.onAction(LoginUiAction.SignInClicked)
        assertEquals(AuthError.INVALID_CREDENTIALS, viewModel.uiState.value.error)

        viewModel.onAction(LoginUiAction.PasswordChanged("outra"))
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `Google token signs in, a cancelled picker shows nothing and failures show the error`() {
        val viewModel = viewModel()

        viewModel.onAction(LoginUiAction.GoogleTokenReceived("token-123"))
        assertEquals(listOf("google:token-123"), auth.calls)

        viewModel.onAction(LoginUiAction.GoogleFailed(null))
        assertNull(viewModel.uiState.value.error)
        viewModel.onAction(LoginUiAction.GoogleFailed(AuthError.GOOGLE_UNAVAILABLE))
        assertEquals(AuthError.GOOGLE_UNAVAILABLE, viewModel.uiState.value.error)
    }

    @Test
    fun `password visibility toggles and links open the other screens`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(LoginUiAction.TogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        viewModel.events.test {
            viewModel.onAction(LoginUiAction.SignUpClicked)
            assertEquals(LoginUiEvent.OpenSignUp, awaitItem())
            viewModel.onAction(LoginUiAction.ForgotPasswordClicked)
            assertEquals(LoginUiEvent.OpenForgotPassword, awaitItem())
        }
    }
}
