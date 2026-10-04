package com.alarysai.alarysai.feature.auth.domain.usecase

import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.alarysai.alarysai.feature.auth.domain.repository.KeepSignedInRepository
import javax.inject.Inject

/**
 * Run once when the app starts: if the user unchecked "Keep me signed in" last time, the
 * session that Firebase restored is ended and the login screen shows again.
 */
class ApplyKeepSignedInUseCase @Inject constructor(
    private val keepSignedInRepository: KeepSignedInRepository,
    private val authRepository: AuthRepository,
) {
    operator fun invoke() {
        if (!keepSignedInRepository.isKeepSignedIn()) authRepository.signOut()
    }
}
