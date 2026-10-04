package com.alarysai.alarysai.feature.auth.domain.model

import com.alarysai.alarysai.core.common.session.SessionUser
import com.alarysai.alarysai.core.common.session.UserProfile

/** Which part of the app to show, from the session and the user's profile. */
sealed interface AuthGate {
    /** Login, sign-up and password recovery. */
    data object SignedOut : AuthGate

    /** Signed in for the first time: confirm name and language ("Your profile was created"). */
    data class NeedsProfile(val user: SessionUser) : AuthGate

    data class Ready(val user: SessionUser, val profile: UserProfile) : AuthGate
}
