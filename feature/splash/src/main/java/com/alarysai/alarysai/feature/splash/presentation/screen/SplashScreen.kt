package com.alarysai.alarysai.feature.splash.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.splash.R
import com.alarysai.alarysai.feature.splash.presentation.action.SplashUiAction
import com.alarysai.alarysai.feature.splash.presentation.components.SplashLogoIntro
import com.alarysai.alarysai.feature.splash.presentation.components.SplashVideoPlayer
import com.alarysai.alarysai.feature.splash.presentation.event.SplashUiEvent
import com.alarysai.alarysai.feature.splash.presentation.state.SplashUiState
import com.alarysai.alarysai.feature.splash.presentation.viewmodel.SplashViewModel

/** Opening shown when the app starts; calls [onFinished] once when it is over. */
@Composable
fun SplashRoute(
    onFinished: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SplashUiEvent.Finished -> currentOnFinished()
            }
        }
    }
    SplashScreen(uiState = uiState, onAction = viewModel::onAction)
}

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onAction: (SplashUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (uiState) {
            SplashUiState.Preparing -> Unit
            SplashUiState.ShowingLogo -> SplashLogoIntro()
            is SplashUiState.PlayingVideo -> {
                SplashVideoPlayer(
                    uri = uiState.uri,
                    onEnded = { onAction(SplashUiAction.VideoEnded) },
                    onFailed = { onAction(SplashUiAction.VideoFailed) },
                    modifier = Modifier.fillMaxSize(),
                )
                SkipButton(
                    onClick = { onAction(SplashUiAction.SkipClicked) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .safeDrawingPadding()
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SkipButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(stringResource(R.string.splash_skip))
    }
}

@Preview
@Composable
private fun SplashScreenLogoPreview() {
    AlarysTheme { AlarysBackground { SplashScreen(SplashUiState.ShowingLogo, onAction = {}) } }
}
