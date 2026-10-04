package com.alarysai.alarysai.feature.auth.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    private val validate = ValidateSignUpUseCase()

    @Test
    fun `accepts common e-mails and rejects malformed ones`() {
        assertTrue(EmailValidator.isValid("marina.alves@email.com"))
        assertTrue(EmailValidator.isValid("  diego+ia@alarys.com.br "))
        assertFalse(EmailValidator.isValid("marina"))
        assertFalse(EmailValidator.isValid("marina@email"))
        assertFalse(EmailValidator.isValid("ma rina@email.com"))
        assertFalse(EmailValidator.isValid(""))
    }

    @Test
    fun `a complete form has no errors`() {
        assertTrue(validate("Marina", "marina@email.com", "segredo123", "segredo123", acceptedTerms = true).isEmpty)
    }

    @Test
    fun `reports every problem of the form`() {
        assertEquals(
            SignUpErrors(nameMissing = true, emailInvalid = true, passwordTooShort = true, passwordsDiffer = true, termsNotAccepted = true),
            validate(" ", "marina", "curta", "outra", acceptedTerms = false),
        )
    }

    @Test
    fun `passwords need the minimum length`() {
        val shortBy1 = "a".repeat(ValidateSignUpUseCase.MIN_PASSWORD_LENGTH - 1)
        val exact = "a".repeat(ValidateSignUpUseCase.MIN_PASSWORD_LENGTH)

        assertTrue(validate("M", "m@e.com", shortBy1, shortBy1, true).passwordTooShort)
        assertFalse(validate("M", "m@e.com", exact, exact, true).passwordTooShort)
    }
}
