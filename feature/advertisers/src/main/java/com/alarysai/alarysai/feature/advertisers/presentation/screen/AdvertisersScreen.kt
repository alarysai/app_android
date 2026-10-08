package com.alarysai.alarysai.feature.advertisers.presentation.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.alarysai.alarysai.feature.advertisers.R
import com.alarysai.alarysai.feature.advertisers.presentation.action.AdvertisersUiAction
import com.alarysai.alarysai.feature.advertisers.presentation.components.AdvertiserCard
import com.alarysai.alarysai.feature.advertisers.presentation.event.AdvertisersUiEvent
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserGroupUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserItemUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertisersUiState
import com.alarysai.alarysai.feature.advertisers.presentation.viewmodel.AdvertisersViewModel

const val ADVERTISERS_LIST_TAG = "advertisers_list"

@Composable
fun AdvertisersScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: AdvertisersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AdvertisersUiEvent.OpenLink -> openLink(context, event.url)
            }
        }
    }
    AdvertisersScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}

/** Content only: the "Club AI" tab provides the title and puts the tips carousel above. */
@Composable
fun AdvertisersScreen(
    uiState: AdvertisersUiState,
    onAction: (AdvertisersUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill = modifier.fillMaxSize()
    when (uiState) {
        AdvertisersUiState.Loading -> LoadingContent(fill)
        is AdvertisersUiState.Success -> Column(fill) {
            if (uiState.isOffline) OfflineNotice()
            AdvertiserGroups(uiState.groups, onAction)
        }
        is AdvertisersUiState.Empty -> Column(fill) {
            if (uiState.isOffline) OfflineNotice()
            MessageContent(
                title = stringResource(R.string.advertisers_empty_title),
                body = stringResource(R.string.advertisers_empty_body),
            )
        }
        is AdvertisersUiState.Error -> ContentLoadErrorContent(
            error = uiState.error,
            onRetry = { onAction(AdvertisersUiAction.Retry) },
            modifier = fill,
        )
    }
}

@Composable
private fun AdvertiserGroups(groups: List<AdvertiserGroupUi>, onAction: (AdvertisersUiAction) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.testTag(ADVERTISERS_LIST_TAG),
    ) {
        groups.forEachIndexed { groupIndex, group ->
            item(key = "type:${group.type}") {
                Text(
                    text = group.type ?: stringResource(R.string.advertisers_other_type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(group.advertisers, key = { it.id }) { advertiser ->
                AdvertiserCard(
                    advertiser = advertiser,
                    accentIndex = groupIndex,
                    onClick = { onAction(AdvertisersUiAction.AdvertiserClicked(advertiser)) },
                )
            }
        }
    }
}

/** Custom Tab as recommended by the integration guide; falls back to any app that opens links. */
private fun openLink(context: Context, url: String) {
    val uri = Uri.parse(url)
    try {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    } catch (_: ActivityNotFoundException) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }
}

@Preview(heightDp = 600)
@Composable
private fun AdvertisersScreenPreview() {
    AlarysTheme {
        AlarysBackground {
            AdvertisersScreen(
                uiState = AdvertisersUiState.Success(
                    groups = listOf(
                        AdvertiserGroupUi("Parceiro", listOf(AdvertiserItemUi("1", "Loja X", null, "https://lojax.com.br/promo"))),
                        AdvertiserGroupUi("Patrocínio", listOf(AdvertiserItemUi("2", "Escola Y", null, "https://escolay.com.br"))),
                    ),
                    isOffline = false,
                ),
                onAction = {},
            )
        }
    }
}
