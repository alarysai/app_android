package com.alarysai.alarysai.feature.auth.presentation.signup

import app.cash.turbine.test
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.auth.FakeAuthRepository
import com.alarysai.alarysai.feature.auth.FakeKeepSignedIn
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import com.alarysai.alarysai.feature.auth.domain.usecase.SignUpErrors
import com.alarysai.alarysai.feature.auth.domain.usecase.ValidateSignUpUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val keep = FakeKeepSignedIn(keep = false)

    private fun viewModel() = SignUpViewModel(auth, keep, ValidateSignUpUseCase())

    private fun SignUpViewModel.fill(
        name: String = "Marina Alves",
        email: String = "marina@email.com",
        password: String = "segredo123",
        confirmation: String = password,
        terms: Boolean = true,
    ) {
        onAction(SignUpUiAction.NameChanged(name))
        onAction(SignUpUiAction.EmailChanged(email))
        onAction(SignUpUiAction.PasswordChanged(password))
        onAction(SignUpUiAction.ConfirmationChanged(confirmation))
        onAction(SignUpUiAction.TermsChanged(terms))
    }

    @Test
    fun `the button needs every field and the accepted terms`() {
        val viewModel = viewModel()
        viewModel.fill(terms = false)
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onAction(SignUpUiAction.TermsChanged(true))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `creates the account and keeps the new user signed in`() {
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.onAction(SignUpUiAction.CreateAccountClicked)

        assertEquals(listOf("signUp:Marina Alves:marina@email.com:segredo123"), auth.calls)
        assertTrue(keep.keep)
    }

    @Test
    fun `field errors appear after the first attempt and follow the typing`() {
        val viewModel = viewModel()
        viewModel.fill(email = "marina", password = "curta", confirmation = "outra")
        assertEquals(SignUpErrors(), viewModel.uiState.value.errors)

        viewModel.onAction(SignUpUiAction.CreateAccountClicked)
        assertEquals(SignUpErrors(emailInvalid = true, passwordTooShort = true, passwordsDiffer = true), viewModel.uiState.value.errors)
        assertEquals(emptyList<String>(), auth.calls)

        viewModel.onAction(SignUpUiAction.EmailChanged("marina@email.com"))
        assertFalse(viewModel.uiState.value.errors.emailInvalid)
    }

    @Test
    fun `an e-mail already in use is reported`() {
        auth.failWith = AuthError.EMAIL_ALREADY_IN_USE
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.onAction(SignUpUiAction.CreateAccountClicked)

        assertEquals(AuthError.EMAIL_ALREADY_IN_USE, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `back returns to the login`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(SignUpUiAction.BackClicked)
            assertEquals(SignUpUiEvent.BackToLogin, awaitItem())
        }
    }
}
