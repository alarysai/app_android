package com.alarysai.alarysai.feature.auth.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import com.alarysai.alarysai.feature.auth.domain.usecase.SignUpErrors
import com.alarysai.alarysai.feature.auth.presentation.forgot.ForgotPasswordScreen
import com.alarysai.alarysai.feature.auth.presentation.forgot.ForgotPasswordUiAction
import com.alarysai.alarysai.feature.auth.presentation.forgot.ForgotPasswordUiState
import com.alarysai.alarysai.feature.auth.presentation.login.LOGIN_EMAIL_TAG
import com.alarysai.alarysai.feature.auth.presentation.login.LoginScreen
import com.alarysai.alarysai.feature.auth.presentation.login.LoginUiAction
import com.alarysai.alarysai.feature.auth.presentation.login.LoginUiState
import com.alarysai.alarysai.feature.auth.presentation.profile.ProfileMode
import com.alarysai.alarysai.feature.auth.presentation.profile.ProfileScreen
import com.alarysai.alarysai.feature.auth.presentation.profile.ProfileUiAction
import com.alarysai.alarysai.feature.auth.presentation.profile.ProfileUiState
import com.alarysai.alarysai.feature.auth.presentation.signup.SignUpScreen
import com.alarysai.alarysai.feature.auth.presentation.signup.SignUpUiAction
import com.alarysai.alarysai.feature.auth.presentation.signup.SignUpUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class AuthScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginTypesSignsInAndOpensTheOtherScreens() {
        val actions = mutableListOf<LoginUiAction>()
        var googleClicks = 0
        composeRule.setContent {
            AlarysTheme {
                LoginScreen(
                    LoginUiState(email = "marina@email.com", password = "segredo123", error = AuthError.INVALID_CREDENTIALS),
                    onAction = { actions += it },
                    onGoogleClick = { googleClicks++ },
                )
            }
        }

        composeRule.onNodeWithText("Bem-vindo de volta").assertIsDisplayed()
        composeRule.onNodeWithText("E-mail ou senha incorretos.").assertIsDisplayed()
        composeRule.onNodeWithTag(LOGIN_EMAIL_TAG).performTextInput("x")
        composeRule.onNodeWithText("Entrar").performClick()
        composeRule.onNodeWithText("Esqueci a senha").performClick()
        composeRule.onNodeWithText("Criar conta").performClick()
        composeRule.onNodeWithText("Continuar com Google").performClick()
        composeRule.onNodeWithContentDescription("Mostrar senha").performClick()

        assertTrue(actions.first() is LoginUiAction.EmailChanged)
        assertTrue(LoginUiAction.SignInClicked in actions)
        assertTrue(LoginUiAction.ForgotPasswordClicked in actions)
        assertTrue(LoginUiAction.SignUpClicked in actions)
        assertTrue(LoginUiAction.TogglePasswordVisibility in actions)
        assertEquals(1, googleClicks)
    }

    @Test
    fun loginButtonWaitsForCredentials() {
        composeRule.setContent { AlarysTheme { LoginScreen(LoginUiState(), onAction = {}, onGoogleClick = {}) } }

        composeRule.onNodeWithText("Entrar").assertIsNotEnabled()
    }

    @Test
    fun signUpShowsFieldErrorsAndNeedsTheTerms() {
        val actions = mutableListOf<SignUpUiAction>()
        composeRule.setContent {
            AlarysTheme {
                SignUpScreen(
                    SignUpUiState(
                        name = "Marina", email = "marina", password = "curta", confirmation = "outra",
                        errors = SignUpErrors(emailInvalid = true, passwordsDiffer = true),
                    ),
                    onAction = { actions += it },
                )
            }
        }

        composeRule.onNodeWithText("Informe um e-mail válido.").assertIsDisplayed()
        composeRule.onNodeWithText("As senhas não são iguais.").assertIsDisplayed()
        composeRule.onNodeWithText("Li e aceito a Política de Privacidade e os Termos de Uso, conforme a LGPD.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Voltar").performClick()
        assertEquals(listOf<SignUpUiAction>(SignUpUiAction.BackClicked), actions)
    }

    @Test
    fun forgotPasswordFormAndConfirmation() {
        val actions = mutableListOf<ForgotPasswordUiAction>()
        composeRule.setContent {
            AlarysTheme {
                ForgotPasswordScreen(ForgotPasswordUiState(sentTo = "marina.alves@email.com", resendInSeconds = 24), onAction = { actions += it })
            }
        }

        composeRule.onNodeWithText("Verifique seu e-mail").assertIsDisplayed()
        composeRule.onNodeWithText("Reenviar em 0:24").assertIsNotEnabled()
        composeRule.onNodeWithText("Abrir app de e-mail").performClick()
        composeRule.onNodeWithText("Voltar ao login").performClick()

        assertEquals(listOf(ForgotPasswordUiAction.OpenEmailAppClicked, ForgotPasswordUiAction.BackClicked), actions)
    }

    @Test
    fun profileSetupShowsEmailInitialsAndLanguages() {
        val actions = mutableListOf<ProfileUiAction>()
        composeRule.setContent {
            AlarysTheme {
                ProfileScreen(
                    ProfileUiState(ProfileMode.SETUP, isLoading = false, email = "marina.alves@email.com", name = "Marina Alves"),
                    onAction = { actions += it },
                )
            }
        }

        composeRule.onNodeWithText("Seu perfil foi criado").assertIsDisplayed()
        composeRule.onNodeWithText("marina.alves@email.com").assertIsDisplayed()
        composeRule.onNodeWithText("MA").assertIsDisplayed()
        composeRule.onNodeWithText("Começar").assertIsEnabled()
        composeRule.onNodeWithText("English").performClick()
        composeRule.onNodeWithText("Começar").performClick()

        assertEquals(listOf(ProfileUiAction.LanguageSelected(Language.EN), ProfileUiAction.SaveClicked), actions)
    }
}
