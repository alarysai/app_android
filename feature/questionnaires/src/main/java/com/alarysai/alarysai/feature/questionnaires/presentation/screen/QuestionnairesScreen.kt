package com.alarysai.alarysai.feature.questionnaires.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import com.alarysai.alarysai.feature.questionnaires.R
import com.alarysai.alarysai.feature.questionnaires.presentation.action.QuestionnairesUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.components.QuestionnaireCard
import com.alarysai.alarysai.feature.questionnaires.presentation.event.QuestionnairesUiEvent
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnaireItemUi
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnairesContent
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnairesUiState
import com.alarysai.alarysai.feature.questionnaires.presentation.viewmodel.QuestionnairesViewModel

const val QUESTIONNAIRES_LIST_TAG = "questionnaires_list"

@Composable
fun QuestionnairesScreenRoute(
    onBack: () -> Unit,
    onOpenQuestionnaire: (questionnaireId: String, title: String) -> Unit,
    viewModel: QuestionnairesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is QuestionnairesUiEvent.OpenQuestionnaire -> onOpenQuestionnaire(event.questionnaireId, event.title)
            }
        }
    }
    QuestionnairesScreen(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnairesScreen(
    uiState: QuestionnairesUiState,
    onAction: (QuestionnairesUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(uiState.categoryName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.questionnaires_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (val content = uiState.content) {
            QuestionnairesContent.Loading -> LoadingContent(contentModifier)
            is QuestionnairesContent.Success -> QuestionnaireList(
                content = content,
                onQuestionnaireClick = { onAction(QuestionnairesUiAction.QuestionnaireClicked(it)) },
                modifier = contentModifier,
            )
            is QuestionnairesContent.Empty -> Column(contentModifier) {
                if (content.isOffline) OfflineNotice()
                MessageContent(
                    title = stringResource(R.string.questionnaires_empty_title),
                    body = stringResource(R.string.questionnaires_empty_body),
                )
            }
            is QuestionnairesContent.Error -> ContentLoadErrorContent(
                error = content.error,
                onRetry = { onAction(QuestionnairesUiAction.Retry) },
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun QuestionnaireList(
    content: QuestionnairesContent.Success,
    onQuestionnaireClick: (QuestionnaireItemUi) -> Unit,
    modifier: Modifier,
) {
    Column(modifier) {
        if (content.isOffline) OfflineNotice()
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag(QUESTIONNAIRES_LIST_TAG),
        ) {
            items(content.questionnaires, key = { it.id }) { questionnaire ->
                QuestionnaireCard(questionnaire = questionnaire, onClick = { onQuestionnaireClick(questionnaire) })
            }
        }
    }
}

@Preview(heightDp = 700)
@Composable
private fun QuestionnairesScreenPreview() {
    AlarysTheme {
        AlarysBackground {
            QuestionnairesScreen(
                uiState = QuestionnairesUiState(
                    categoryName = "Texto",
                    content = QuestionnairesContent.Success(
                        questionnaires = listOf(
                            QuestionnaireItemUi("1", "Ética na IA", "Descubra como usar IA com responsabilidade.", null, true, 0),
                            QuestionnaireItemUi("2", "Post para redes", null, null, false, 1),
                        ),
                        isOffline = false,
                    ),
                ),
                onAction = {},
                onBack = {},
            )
        }
    }
}
