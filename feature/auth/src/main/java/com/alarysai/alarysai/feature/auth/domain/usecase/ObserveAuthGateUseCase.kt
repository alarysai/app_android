package com.alarysai.alarysai.feature.auth.domain.usecase

import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.UserProfile
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.feature.auth.domain.model.AuthGate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Signed out -> login; signed in without `users/{uid}` -> profile setup; otherwise the app.
 * If the profile cannot be read (e.g. offline on a new device), the app opens anyway with the
 * provider data instead of locking the user out.
 */
class ObserveAuthGateUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: UserProfileRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<AuthGate> =
        sessionRepository.observeUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(AuthGate.SignedOut)
            } else {
                profileRepository.observeProfile(user.uid)
                    .map { profile -> if (profile == null) AuthGate.NeedsProfile(user) else AuthGate.Ready(user, profile) }
                    .catch { emit(AuthGate.Ready(user, UserProfile(user.displayName, photoUrl = null, language = null))) }
            }
        }
}
