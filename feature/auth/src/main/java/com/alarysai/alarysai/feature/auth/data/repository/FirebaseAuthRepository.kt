package com.alarysai.alarysai.feature.auth.data.repository

import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import com.alarysai.alarysai.feature.auth.domain.model.AuthException
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val languageProvider: LanguageProvider,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String) = authCall {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signUp(name: String, email: String, password: String) = authCall {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user ?: return@authCall
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).await()
        // Forces a token refresh so the session emits again, now with the display name.
        user.getIdToken(true).await()
    }

    override suspend fun sendPasswordReset(email: String) = authCall {
        // The e-mail goes out in the user's language.
        auth.setLanguageCode(languageProvider.currentLanguage().code)
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun signInWithGoogle(idToken: String) = authCall {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    override fun signOut() = auth.signOut()

    private suspend fun authCall(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AuthException(error.toAuthError(), error)
        }
    }
}

/** Order matters: the weak-password exception is also an invalid-credentials one. */
internal fun Throwable.toAuthError(): AuthError = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
    is FirebaseAuthInvalidCredentialsException ->
        if (errorCode == "ERROR_INVALID_EMAIL") AuthError.INVALID_EMAIL else AuthError.INVALID_CREDENTIALS
    is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
    is FirebaseNetworkException -> AuthError.NETWORK
    is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
    else -> AuthError.UNKNOWN
}
