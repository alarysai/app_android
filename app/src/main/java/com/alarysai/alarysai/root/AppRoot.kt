package com.alarysai.alarysai.root

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.feature.auth.domain.model.AuthGate
import com.alarysai.alarysai.feature.auth.presentation.AuthFlow
import com.alarysai.alarysai.feature.auth.presentation.profile.ProfileScreenRoute
import com.alarysai.alarysai.feature.splash.presentation.screen.SplashRoute
import com.alarysai.alarysai.navigation.AlarysNavHost

/**
 * The opening (splash) plays first; meanwhile the session is checked. After it, sign-in is
 * required: nothing of the app is shown until there is a session and a profile.
 */
@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val gate by viewModel.gate.collectAsStateWithLifecycle()
    // Saveable: rotating the phone or coming back to the app does not replay the opening.
    var isSplashFinished by rememberSaveable { mutableStateOf(false) }
    AlarysBackground {
        Crossfade(targetState = isSplashFinished, label = "splash") { splashFinished ->
            if (!splashFinished) {
                SplashRoute(onFinished = { isSplashFinished = true })
            } else {
                when (gate) {
                    null -> LoadingContent()
                    AuthGate.SignedOut -> AuthFlow()
                    is AuthGate.NeedsProfile -> ProfileScreenRoute()
                    is AuthGate.Ready -> AlarysNavHost()
                }
            }
        }
    }
}
