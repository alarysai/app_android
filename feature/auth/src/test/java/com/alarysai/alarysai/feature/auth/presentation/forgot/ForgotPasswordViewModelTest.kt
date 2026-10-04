package com.alarysai.alarysai.feature.auth.presentation.forgot

import app.cash.turbine.test
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.auth.FakeAuthRepository
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val auth = FakeAuthRepository()

    @Test
    fun `sends the link, shows the confirmation and counts down before resending`() = runTest(dispatcher) {
        val viewModel = ForgotPasswordViewModel(auth)
        viewModel.onAction(ForgotPasswordUiAction.EmailChanged(" marina@email.com "))

        viewModel.onAction(ForgotPasswordUiAction.SendClicked)
        runCurrent()
        assertEquals("marina@email.com", viewModel.uiState.value.sentTo)
        assertEquals(ForgotPasswordViewModel.RESEND_COOLDOWN_SECONDS, viewModel.uiState.value.resendInSeconds)

        viewModel.onAction(ForgotPasswordUiAction.ResendClicked)
        runCurrent()
        assertEquals(listOf("reset: marina@email.com "), auth.calls)

        advanceTimeBy(ForgotPasswordViewModel.RESEND_COOLDOWN_SECONDS * 1_000L + 1)
        assertEquals(0, viewModel.uiState.value.resendInSeconds)
        viewModel.onAction(ForgotPasswordUiAction.ResendClicked)
        runCurrent()
        assertEquals(2, auth.calls.size)
    }

    @Test
    fun `a malformed e-mail is flagged and provider errors are shown`() = runTest(dispatcher) {
        val viewModel = ForgotPasswordViewModel(auth)
        viewModel.onAction(ForgotPasswordUiAction.EmailChanged("marina"))
        viewModel.onAction(ForgotPasswordUiAction.SendClicked)
        assertTrue(viewModel.uiState.value.showEmailError)

        auth.failWith = AuthError.TOO_MANY_REQUESTS
        viewModel.onAction(ForgotPasswordUiAction.EmailChanged("marina@email.com"))
        viewModel.onAction(ForgotPasswordUiAction.SendClicked)
        runCurrent()
        assertEquals(AuthError.TOO_MANY_REQUESTS, viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.sentTo)
    }

    @Test
    fun `opens the e-mail app and goes back to the login`() = runTest(dispatcher) {
        val viewModel = ForgotPasswordViewModel(auth)

        viewModel.events.test {
            viewModel.onAction(ForgotPasswordUiAction.OpenEmailAppClicked)
            assertEquals(ForgotPasswordUiEvent.OpenEmailApp, awaitItem())
            viewModel.onAction(ForgotPasswordUiAction.BackClicked)
            assertEquals(ForgotPasswordUiEvent.BackToLogin, awaitItem())
        }
    }
}
