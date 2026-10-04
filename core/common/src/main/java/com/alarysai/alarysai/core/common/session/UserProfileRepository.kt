package com.alarysai.alarysai.core.common.session

import com.alarysai.alarysai.core.common.language.Language
import kotlinx.coroutines.flow.Flow

/** `users/{uid}`: the profile fields the app may read and write (see `firestore.rules`). */
data class UserProfile(
    val displayName: String?,
    val photoUrl: String?,
    val language: Language?,
)

interface UserProfileRepository {

    /** The user's profile; null while the document does not exist (first sign-in). Fails with `ContentLoadException`. */
    fun observeProfile(uid: String): Flow<UserProfile?>

    /**
     * Creates ([isNew]) or updates the profile. Only `displayName`, `photoUrl`, `language`,
     * `createdAt` and `updatedAt` are written: the rules reject anything else.
     * Fails with `ContentLoadException`.
     */
    suspend fun saveProfile(uid: String, displayName: String, language: Language, isNew: Boolean)
}
