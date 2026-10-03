package com.alarysai.alarysai.core.common.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Temporary session while the app has no sign-in (Task 9): nobody is ever signed in.
 *
 * Task 9 replaces this binding with a Firebase Auth implementation. Note for that task:
 * `firebase-auth` 24.x (Firebase BoM 34) is compiled with Kotlin 2.3 metadata, which the
 * project's Kotlin 2.0.20 cannot read; it needs a toolchain upgrade or an older Auth version.
 */
class SignedOutSessionRepository @Inject constructor() : SessionRepository {
    override fun observeUserId(): Flow<String?> = flowOf(null)
}
