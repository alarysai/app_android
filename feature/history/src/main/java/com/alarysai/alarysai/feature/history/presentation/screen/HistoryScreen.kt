package com.alarysai.alarysai.feature.history.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.Blue500
import com.alarysai.alarysai.core.ui.state.ContentLoadErrorContent
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.core.ui.state.MessageContent
import com.alarysai.alarysai.core.ui.state.OfflineNotice
import com.alarysai.alarysai.feature.history.R
import com.alarysai.alarysai.feature.history.presentation.action.HistoryUiAction
import com.alarysai.alarysai.feature.history.presentation.components.CreditItemRow
import com.alarysai.alarysai.feature.history.presentation.components.HistoryItemCard
import com.alarysai.alarysai.feature.history.presentation.event.HistoryUiEvent
import com.alarysai.alarysai.feature.history.presentation.state.HistoryTab
import com.alarysai.alarysai.feature.history.presentation.state.HistoryUiState
import com.alarysai.alarysai.feature.history.presentation.state.ListContent
import com.alarysai.alarysai.feature.history.presentation.viewmodel.HistoryViewModel

@Composable
fun HistoryScreenRoute(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryUiEvent.OpenResult -> runCatching { uriHandler.openUri(event.url) }
                HistoryUiEvent.DeleteFailed -> snackbarHostState.showSnackbar(context.getString(R.string.history_delete_failed))
            }
        }
    }
    HistoryScreen(uiState = uiState, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onAction: (HistoryUiAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            HistoryUiState.Loading -> LoadingContent(contentModifier)
            HistoryUiState.SignedOut -> MessageContent(
                title = stringResource(R.string.history_signed_out_title),
                body = stringResource(R.string.history_signed_out_body),
                modifier = contentModifier,
            )
            is HistoryUiState.SignedIn -> SignedInContent(uiState, onAction, contentModifier)
        }
    }
}

@Composable
private fun SignedInContent(state: HistoryUiState.SignedIn, onAction: (HistoryUiAction) -> Unit, modifier: Modifier) {
    Column(modifier) {
        BalanceCard(state.balance)
        TabRow(selectedTabIndex = state.selectedTab.ordinal, containerColor = Color.Transparent) {
            HistoryTab.entries.forEach { tab ->
                Tab(
                    selected = tab == state.selectedTab,
                    onClick = { onAction(HistoryUiAction.TabSelected(tab)) },
                    text = {
                        Text(
                            stringResource(
                                if (tab == HistoryTab.GENERATIONS) R.string.history_tab_generations else R.string.history_tab_statement,
                            ),
                        )
                    },
                )
            }
        }
        when (state.selectedTab) {
            HistoryTab.GENERATIONS -> TabList(
                content = state.generations,
                emptyTitle = R.string.history_empty_generations,
                onRetry = { onAction(HistoryUiAction.Retry) },
            ) { item ->
                HistoryItemCard(
                    item = item,
                    onOpenResult = { onAction(HistoryUiAction.OpenResultClicked(item)) },
                    onDelete = { onAction(HistoryUiAction.DeleteClicked(item)) },
                )
            }
            HistoryTab.STATEMENT -> TabList(
                content = state.statement,
                emptyTitle = R.string.history_empty_statement,
                onRetry = { onAction(HistoryUiAction.Retry) },
            ) { item -> CreditItemRow(item) }
        }
    }
    state.pendingDeletion?.let { entry ->
        AlertDialog(
            onDismissRequest = { onAction(HistoryUiAction.DeleteDismissed) },
            title = { Text(stringResource(R.string.history_delete_title)) },
            text = { Text(stringResource(R.string.history_delete_body, entry.title ?: stringResource(R.string.history_untitled))) },
            confirmButton = {
                TextButton(onClick = { onAction(HistoryUiAction.DeleteConfirmed) }) { Text(stringResource(R.string.history_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { onAction(HistoryUiAction.DeleteDismissed) }) { Text(stringResource(R.string.history_cancel)) }
            },
        )
    }
}

@Composable
private fun BalanceCard(balance: Int?) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        accent = Blue500,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.history_balance_title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = balance?.let { pluralStringResource(R.plurals.history_balance, it, it) } ?: "—",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun <T> TabList(
    content: ListContent<T>,
    emptyTitle: Int,
    onRetry: () -> Unit,
    itemContent: @Composable (T) -> Unit,
) {
    val fill = Modifier.fillMaxSize()
    when (content) {
        ListContent.Loading -> LoadingContent(fill)
        is ListContent.Success -> Column(fill) {
            if (content.isOffline) OfflineNotice()
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(content.items) { itemContent(it) }
            }
        }
        is ListContent.Empty -> Column(fill) {
            if (content.isOffline) OfflineNotice()
            MessageContent(title = stringResource(emptyTitle), body = stringResource(R.string.history_empty_body))
        }
        is ListContent.Error -> ContentLoadErrorContent(error = content.error, onRetry = onRetry, modifier = fill)
    }
}
