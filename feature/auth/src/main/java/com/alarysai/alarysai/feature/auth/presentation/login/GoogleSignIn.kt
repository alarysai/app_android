package com.alarysai.alarysai.feature.auth.presentation.login

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.alarysai.alarysai.feature.auth.domain.model.AuthError
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Opens the Google account picker (Credential Manager) and hands back the ID token, which the
 * ViewModel exchanges for a Firebase session. Lives in the UI because it needs an Activity context.
 */
@Composable
internal fun rememberGoogleSignIn(
    webClientId: String,
    onToken: (String) -> Unit,
    onFailure: (AuthError?) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(webClientId, context) {
        {
            scope.launch {
                when (val result = requestGoogleIdToken(context, webClientId)) {
                    is GoogleResult.Token -> onToken(result.idToken)
                    is GoogleResult.Failure -> onFailure(result.error)
                }
            }
        }
    }
}

private sealed interface GoogleResult {
    data class Token(val idToken: String) : GoogleResult

    /** [error] null: the user closed the picker. */
    data class Failure(val error: AuthError?) : GoogleResult
}

private suspend fun requestGoogleIdToken(context: Context, webClientId: String): GoogleResult {
    if (webClientId.isBlank()) return GoogleResult.Failure(AuthError.GOOGLE_UNAVAILABLE)
    val option = GetGoogleIdOption.Builder()
        .setServerClientId(webClientId)
        .setFilterByAuthorizedAccounts(false)
        .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    return try {
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            GoogleResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            GoogleResult.Failure(AuthError.UNKNOWN)
        }
    } catch (_: GetCredentialCancellationException) {
        GoogleResult.Failure(null)
    } catch (_: NoCredentialException) {
        GoogleResult.Failure(AuthError.GOOGLE_UNAVAILABLE)
    } catch (_: GetCredentialException) {
        GoogleResult.Failure(AuthError.UNKNOWN)
    }
}
