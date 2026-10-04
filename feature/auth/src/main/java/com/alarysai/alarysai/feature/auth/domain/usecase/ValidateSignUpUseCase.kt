package com.alarysai.alarysai.feature.auth.domain.usecase

import javax.inject.Inject

/** Field problems of the sign-up form; empty means the form can be sent. */
data class SignUpErrors(
    val nameMissing: Boolean = false,
    val emailInvalid: Boolean = false,
    val passwordTooShort: Boolean = false,
    val passwordsDiffer: Boolean = false,
    /** LGPD: the privacy policy and terms must be accepted to create an account. */
    val termsNotAccepted: Boolean = false,
) {
    val isEmpty: Boolean
        get() = !nameMissing && !emailInvalid && !passwordTooShort && !passwordsDiffer && !termsNotAccepted
}

class ValidateSignUpUseCase @Inject constructor() {

    operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmation: String,
        acceptedTerms: Boolean,
    ) = SignUpErrors(
        nameMissing = name.isBlank(),
        emailInvalid = !EmailValidator.isValid(email),
        passwordTooShort = password.length < MIN_PASSWORD_LENGTH,
        passwordsDiffer = password != confirmation,
        termsNotAccepted = !acceptedTerms,
    )

    companion object {
        /** Firebase accepts 6; the app asks for 8. */
        const val MIN_PASSWORD_LENGTH = 8
    }
}
