package com.alarysai.alarysai.feature.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.ui.state.ContentLoadErrorContent
import com.alarysai.alarysai.core.ui.state.MessageContent
import com.alarysai.alarysai.core.ui.state.OfflineNotice
import com.alarysai.alarysai.feature.home.R
import com.alarysai.alarysai.feature.home.presentation.action.HomeUiAction
import com.alarysai.alarysai.feature.home.presentation.components.CategoryGrid
import com.alarysai.alarysai.feature.home.presentation.components.ChatPromptCard
import com.alarysai.alarysai.feature.home.presentation.components.HomeGreeting
import com.alarysai.alarysai.feature.home.presentation.components.HomeHeader
import com.alarysai.alarysai.feature.home.presentation.components.HomeSearchBar
import com.alarysai.alarysai.feature.home.presentation.components.PlanUsageCard
import com.alarysai.alarysai.feature.home.presentation.event.ComingSoonFeature
import com.alarysai.alarysai.feature.home.presentation.event.HomeUiEvent
import com.alarysai.alarysai.feature.home.presentation.state.CategoriesSection
import com.alarysai.alarysai.feature.home.presentation.state.CategoryItemUi
import com.alarysai.alarysai.feature.home.presentation.state.HomeUiState
import com.alarysai.alarysai.feature.home.presentation.state.PlanUsageUi
import com.alarysai.alarysai.feature.home.presentation.viewmodel.HomeViewModel

const val CATEGORIES_LOADING_TAG = "categories_loading"

@Composable
fun HomeScreenRoute(
    onOpenCategory: (categoryId: String, categoryName: String) -> Unit,
    onOpenPlans: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.ShowComingSoon ->
                    snackbarHostState.showSnackbar(context.getString(event.feature.messageRes()))
                is HomeUiEvent.OpenCategory -> onOpenCategory(event.categoryId, event.categoryName)
                HomeUiEvent.OpenPlans -> onOpenPlans()
            }
        }
    }
    HomeScreen(uiState = uiState, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HomeHeader(onNotificationsClick = { onAction(HomeUiAction.NotificationsClicked) })
            HomeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onAction(HomeUiAction.SearchQueryChanged(it)) },
            )
            HomeGreeting(userName = uiState.userName)
            PlanUsageCard(
                usage = uiState.planUsage,
                onBuyCreditsClick = { onAction(HomeUiAction.BuyCreditsClicked) },
            )
            CategoriesSectionContent(
                section = uiState.categories,
                onRetry = { onAction(HomeUiAction.RetryCategories) },
                onCategoryClick = { onAction(HomeUiAction.CategoryClicked(it)) },
            )
            ChatPromptCard(
                message = uiState.chatMessage,
                onMessageChange = { onAction(HomeUiAction.ChatMessageChanged(it)) },
                onSendClick = { onAction(HomeUiAction.ChatSendClicked) },
            )
        }
    }
}

@Composable
private fun CategoriesSectionContent(
    section: CategoriesSection,
    onRetry: () -> Unit,
    onCategoryClick: (CategoryItemUi) -> Unit,
) {
    when (section) {
        CategoriesSection.Loading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag(CATEGORIES_LOADING_TAG),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        is CategoriesSection.Success -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (section.isOffline) OfflineNotice()
            CategoryGrid(categories = section.categories, onCategoryClick = onCategoryClick)
        }
        is CategoriesSection.NoSearchResults -> MessageContent(
            title = stringResource(R.string.home_search_no_results, section.query),
        )
        is CategoriesSection.Empty -> Column {
            if (section.isOffline) OfflineNotice()
            MessageContent(
                title = stringResource(R.string.home_categories_empty_title),
                body = stringResource(R.string.home_categories_empty_body),
            )
        }
        is CategoriesSection.Error -> ContentLoadErrorContent(error = section.error, onRetry = onRetry)
    }
}

private fun ComingSoonFeature.messageRes(): Int = when (this) {
    ComingSoonFeature.CHAT -> R.string.home_coming_soon_chat
    ComingSoonFeature.NOTIFICATIONS -> R.string.home_coming_soon_notifications
}

@Preview(heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    AlarysTheme {
        AlarysBackground {
            HomeScreen(
                uiState = HomeUiState(
                    userName = "Diego",
                    planUsage = PlanUsageUi(usedPercent = 60, availableCredits = 40),
                    categories = CategoriesSection.Success(
                        categories = listOf("Texto", "Imagem", "Vídeo", "Apresentação", "Recursos avançados")
                            .mapIndexed { index, name -> CategoryItemUi("$index", name, null, index) },
                        isOffline = false,
                    ),
                ),
                onAction = {},
            )
        }
    }
}
