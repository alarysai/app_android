package com.alarysai.alarysai.feature.auth.data.repository

import android.content.Context
import com.alarysai.alarysai.feature.auth.domain.repository.KeepSignedInRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** A single local flag; nothing about the account is stored here. */
class SharedPrefsKeepSignedInRepository @Inject constructor(
    @ApplicationContext context: Context,
) : KeepSignedInRepository {

    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    override fun isKeepSignedIn(): Boolean = preferences.getBoolean(KEY_KEEP_SIGNED_IN, true)

    override fun setKeepSignedIn(keep: Boolean) {
        preferences.edit().putBoolean(KEY_KEEP_SIGNED_IN, keep).apply()
    }

    private companion object {
        const val PREFERENCES = "auth"
        const val KEY_KEEP_SIGNED_IN = "keep_signed_in"
    }
}
