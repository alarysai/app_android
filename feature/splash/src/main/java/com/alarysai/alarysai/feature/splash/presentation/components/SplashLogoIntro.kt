package com.alarysai.alarysai.feature.splash.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.component.AlarysLogo
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import kotlinx.coroutines.launch

/** Opening without a video: the logo fades in while growing slightly. */
@Composable
fun SplashLogoIntro(modifier: Modifier = Modifier) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(INITIAL_SCALE) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(DURATION_MILLIS, easing = FastOutSlowInEasing)) }
        launch { scale.animateTo(1f, tween(DURATION_MILLIS, easing = FastOutSlowInEasing)) }
    }
    AlarysLogo(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            scaleX = scale.value
            scaleY = scale.value
        },
    )
}

private const val INITIAL_SCALE = 0.85f
private const val DURATION_MILLIS = 900

@Preview
@Composable
private fun SplashLogoIntroPreview() {
    AlarysTheme { AlarysBackground { SplashLogoIntro() } }
}
