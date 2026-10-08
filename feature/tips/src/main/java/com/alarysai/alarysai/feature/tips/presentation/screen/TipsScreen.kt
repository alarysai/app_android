package com.alarysai.alarysai.feature.tips.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.ui.state.ContentLoadErrorBanner
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.core.ui.state.OfflineNotice
import com.alarysai.alarysai.feature.tips.presentation.action.TipsUiAction
import com.alarysai.alarysai.feature.tips.presentation.components.TipCarouselHeight
import com.alarysai.alarysai.feature.tips.presentation.components.TipsCarousel
import com.alarysai.alarysai.feature.tips.presentation.state.TipItemUi
import com.alarysai.alarysai.feature.tips.presentation.state.TipsContent
import com.alarysai.alarysai.feature.tips.presentation.state.TipsUiState
import com.alarysai.alarysai.feature.tips.presentation.viewmodel.TipsViewModel

@Composable
fun TipsScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: TipsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TipsScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}

/**
 * Tips section at the top of the "Club AI" tab: a carousel, sized to its content so the
 * advertisers fill the rest of the screen. Without tips the section takes no space.
 */
@Composable
fun TipsScreen(
    uiState: TipsUiState,
    onAction: (TipsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (val content = uiState.content) {
            TipsContent.Loading -> LoadingContent(Modifier.height(TipCarouselHeight))
            is TipsContent.Success -> {
                if (content.isOffline) OfflineNotice()
                TipsCarousel(tips = content.tips)
            }
            is TipsContent.Empty -> if (content.isOffline) OfflineNotice()
            is TipsContent.Error -> ContentLoadErrorBanner(error = content.error, onRetry = { onAction(TipsUiAction.Retry) })
        }
    }
}

@Preview(heightDp = 260)
@Composable
private fun TipsScreenPreview() {
    AlarysTheme {
        AlarysBackground {
            TipsScreen(
                uiState = TipsUiState(
                    content = TipsContent.Success(
                        tips = listOf(
                            TipItemUi("1", "Sempre cite as fontes que a IA usou.", null, "Ética", true),
                            TipItemUi("2", "Peça exemplos para entender melhor.", null, "Conhecimento", false),
                        ),
                        isOffline = false,
                    ),
                ),
                onAction = {},
            )
        }
    }
}
