package com.alarysai.alarysai.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.feature.auth.domain.model.AuthGate
import com.alarysai.alarysai.feature.auth.presentation.AuthFlow
import com.alarysai.alarysai.feature.auth.presentation.profile.ProfileScreenRoute
import com.alarysai.alarysai.navigation.AlarysNavHost

/** Sign-in is required: nothing of the app is shown until there is a session and a profile. */
@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val gate by viewModel.gate.collectAsStateWithLifecycle()
    AlarysBackground {
        when (gate) {
            null -> LoadingContent()
            AuthGate.SignedOut -> AuthFlow()
            is AuthGate.NeedsProfile -> ProfileScreenRoute()
            is AuthGate.Ready -> AlarysNavHost()
        }
    }
}
