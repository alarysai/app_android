package com.alarysai.alarysai.feature.auth.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun `maps the Firebase Auth failures`() {
        assertEquals(AuthError.WEAK_PASSWORD, FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "weak", "short").toAuthError())
        assertEquals(AuthError.EMAIL_ALREADY_IN_USE, FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "used").toAuthError())
        assertEquals(AuthError.INVALID_EMAIL, FirebaseAuthInvalidCredentialsException("ERROR_INVALID_EMAIL", "bad").toAuthError())
        assertEquals(AuthError.INVALID_CREDENTIALS, FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "bad").toAuthError())
        assertEquals(AuthError.INVALID_CREDENTIALS, FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "none").toAuthError())
        assertEquals(AuthError.NETWORK, FirebaseNetworkException("offline").toAuthError())
        assertEquals(AuthError.TOO_MANY_REQUESTS, FirebaseTooManyRequestsException("slow down").toAuthError())
        assertEquals(AuthError.UNKNOWN, IllegalStateException().toAuthError())
    }
}
