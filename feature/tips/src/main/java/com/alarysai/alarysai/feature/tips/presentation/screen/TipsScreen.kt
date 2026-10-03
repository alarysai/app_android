package com.alarysai.alarysai.feature.tips.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.ui.state.ContentLoadErrorContent
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.core.ui.state.MessageContent
import com.alarysai.alarysai.core.ui.state.OfflineNotice
import com.alarysai.alarysai.feature.tips.R
import com.alarysai.alarysai.feature.tips.presentation.action.TipsUiAction
import com.alarysai.alarysai.feature.tips.presentation.components.TipCard
import com.alarysai.alarysai.feature.tips.presentation.components.TipCategoryChips
import com.alarysai.alarysai.feature.tips.presentation.state.TipCategoryUi
import com.alarysai.alarysai.feature.tips.presentation.state.TipItemUi
import com.alarysai.alarysai.feature.tips.presentation.state.TipsContent
import com.alarysai.alarysai.feature.tips.presentation.state.TipsUiState
import com.alarysai.alarysai.feature.tips.presentation.viewmodel.TipsViewModel

const val TIPS_LIST_TAG = "tips_list"

@Composable
fun TipsScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: TipsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TipsScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}

/** Content only: the "Club AI" tab provides the title and the Tips/Advertisers tabs. */
@Composable
fun TipsScreen(
    uiState: TipsUiState,
    onAction: (TipsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.categories.isNotEmpty()) {
            TipCategoryChips(
                categories = uiState.categories,
                selectedId = uiState.selectedCategoryId,
                onSelect = { onAction(TipsUiAction.CategorySelected(it)) },
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        TipsContentSection(uiState.content, onAction)
    }
}

@Composable
private fun TipsContentSection(content: TipsContent, onAction: (TipsUiAction) -> Unit) {
    val fill = Modifier.fillMaxSize()
    when (content) {
        TipsContent.Loading -> LoadingContent(fill)
        is TipsContent.Success -> Column(fill) {
            if (content.isOffline) OfflineNotice()
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag(TIPS_LIST_TAG),
            ) {
                items(content.tips, key = { it.id }) { tip -> TipCard(tip) }
            }
        }
        is TipsContent.Empty -> Column(fill) {
            if (content.isOffline) OfflineNotice()
            MessageContent(
                title = stringResource(R.string.tips_empty_title),
                body = stringResource(R.string.tips_empty_body),
            )
        }
        is TipsContent.Error -> ContentLoadErrorContent(
            error = content.error,
            onRetry = { onAction(TipsUiAction.Retry) },
            modifier = fill,
        )
    }
}

@Preview(heightDp = 700)
@Composable
private fun TipsScreenPreview() {
    AlarysTheme {
        AlarysBackground {
            TipsScreen(
                uiState = TipsUiState(
                    categories = listOf(TipCategoryUi("etica", "Ética"), TipCategoryUi("conhecimento", "Conhecimento")),
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
