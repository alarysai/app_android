package com.alarysai.alarysai.core.firebase.session

import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.session.UserProfile
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.core.firebase.document.observeRemoteDocument
import com.alarysai.alarysai.core.firebase.error.toContentLoadError
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** `users/{uid}` fields read by the app. `creditBalance` is read by the history feature. */
@IgnoreExtraProperties
data class UserProfileDto(
    val displayName: String? = null,
    val photoUrl: String? = null,
    val language: String? = null,
)

fun UserProfileDto.toDomain() = UserProfile(
    displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
    photoUrl = photoUrl?.trim()?.takeIf { it.startsWith("https://") },
    language = Language.fromCodeOrNull(language),
)

/** `android-integration.md` section 8: the app creates the document on first sign-in and updates it. */
class FirestoreUserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserProfileRepository {

    override fun observeProfile(uid: String): Flow<UserProfile?> =
        user(uid).observeRemoteDocument(UserProfileDto::class.java)
            .map { it?.data?.toDomain() }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }

    override suspend fun saveProfile(uid: String, displayName: String, language: Language, isNew: Boolean) {
        val fields = buildMap<String, Any?> {
            put(FIELD_DISPLAY_NAME, displayName.trim())
            put(FIELD_LANGUAGE, language.code)
            put(FIELD_UPDATED_AT, FieldValue.serverTimestamp())
            if (isNew) {
                put(FIELD_PHOTO_URL, null)
                put(FIELD_CREATED_AT, FieldValue.serverTimestamp())
            }
        }
        try {
            user(uid).set(fields, SetOptions.merge()).await()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw ContentLoadException(error.toContentLoadError(), error)
        }
    }

    private fun user(uid: String) = firestore.collection(USERS).document(uid)

    private companion object {
        const val USERS = "users"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_PHOTO_URL = "photoUrl"
        const val FIELD_LANGUAGE = "language"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
