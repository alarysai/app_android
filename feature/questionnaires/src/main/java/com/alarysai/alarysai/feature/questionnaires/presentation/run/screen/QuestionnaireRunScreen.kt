package com.alarysai.alarysai.feature.questionnaires.presentation.run.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.ui.state.ContentLoadErrorContent
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.core.ui.state.MessageContent
import com.alarysai.alarysai.feature.questionnaires.R
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.presentation.run.action.QuestionnaireRunUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.ChoiceOptions
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.OpenTextAnswer
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.ReviewBottomBar
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.ReviewContent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.RunBottomBar
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.RunProgressBar
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.RunTopBar
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.StepHeader
import com.alarysai.alarysai.feature.questionnaires.presentation.run.components.TipCard
import com.alarysai.alarysai.feature.questionnaires.presentation.run.event.QuestionnaireRunUiEvent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunContent
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.QuestionnaireRunUiState
import com.alarysai.alarysai.feature.questionnaires.presentation.run.state.StepUi
import com.alarysai.alarysai.feature.questionnaires.presentation.run.viewmodel.QuestionnaireRunViewModel

@Composable
fun QuestionnaireRunScreenRoute(
    onExit: () -> Unit,
    viewModel: QuestionnaireRunViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is QuestionnaireRunUiEvent.OpenVideo -> runCatching { uriHandler.openUri(event.url) }
                QuestionnaireRunUiEvent.Exit -> onExit()
                QuestionnaireRunUiEvent.GenerationComingSoon ->
                    snackbarHostState.showSnackbar(context.getString(R.string.run_generate_coming_soon))
            }
        }
    }
    QuestionnaireRunScreen(uiState = uiState, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

/**
 * Full-screen questionnaire (the app hides the bottom navigation here): one screen per step,
 * then the review. Every screen is built from the step data in Firestore.
 */
@Composable
fun QuestionnaireRunScreen(
    uiState: QuestionnaireRunUiState,
    onAction: (QuestionnaireRunUiAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // System back goes to the previous step instead of leaving the questionnaire.
    BackHandler { onAction(QuestionnaireRunUiAction.BackClicked) }
    val content = uiState.content
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (content is QuestionnaireRunContent.Review) {
                ReviewTopBar(uiState.title, onBack = { onAction(QuestionnaireRunUiAction.BackClicked) })
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RunTopBar(
                        title = uiState.title,
                        canSkip = (content as? QuestionnaireRunContent.InProgress)?.canSkip == true,
                        onClose = { onAction(QuestionnaireRunUiAction.CloseClicked) },
                        onSkip = { onAction(QuestionnaireRunUiAction.SkipClicked) },
                    )
                    if (content is QuestionnaireRunContent.InProgress) RunProgressBar(content.progress)
                }
            }
        },
        bottomBar = {
            when (content) {
                is QuestionnaireRunContent.InProgress -> RunBottomBar(
                    canContinue = content.canContinue,
                    isLastStep = content.isLastStep,
                    onBack = { onAction(QuestionnaireRunUiAction.BackClicked) },
                    onContinue = { onAction(QuestionnaireRunUiAction.ContinueClicked) },
                )
                is QuestionnaireRunContent.Review -> ReviewBottomBar(
                    creditCost = content.creditCost,
                    onGenerate = { onAction(QuestionnaireRunUiAction.GenerateClicked) },
                )
                else -> Unit
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (content) {
            QuestionnaireRunContent.Loading -> LoadingContent(contentModifier)
            is QuestionnaireRunContent.InProgress -> StepScreen(content, onAction, contentModifier)
            is QuestionnaireRunContent.Review -> ReviewContent(
                review = content,
                onEditAnswer = { onAction(QuestionnaireRunUiAction.EditAnswerClicked(it)) },
                modifier = contentModifier,
            )
            is QuestionnaireRunContent.Error -> RunErrorContent(content.error, onAction, contentModifier)
        }
    }
    if (uiState.isExitConfirmationVisible) ExitDialog(onAction)
}

@Composable
private fun StepScreen(
    content: QuestionnaireRunContent.InProgress,
    onAction: (QuestionnaireRunUiAction) -> Unit,
    modifier: Modifier,
) {
    val step = content.step
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        StepHeader(step)
        when (step) {
            is StepUi.Video -> OutlinedButton(
                onClick = { onAction(QuestionnaireRunUiAction.WatchVideoClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.PlayCircle, contentDescription = null)
                Text(stringResource(R.string.run_watch_video), modifier = Modifier.padding(start = 8.dp))
            }
            is StepUi.Question -> if (step.answerType == AnswerType.OPEN_TEXT) {
                OpenTextAnswer(
                    text = content.draft.text,
                    maxLength = step.maxLength,
                    placeholder = step.placeholder,
                    required = step.required,
                    onTextChange = { onAction(QuestionnaireRunUiAction.TextChanged(it)) },
                )
            } else {
                ChoiceOptions(
                    options = step.options,
                    answerType = step.answerType,
                    showsAsGrid = step.showsOptionsAsGrid,
                    selectedIds = content.draft.selectedOptionIds,
                    onToggle = { onAction(QuestionnaireRunUiAction.OptionToggled(it)) },
                )
            }
        }
        step.tip?.let { TipCard(it) }
    }
}

@Composable
private fun ReviewTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.run_back))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExitDialog(onAction: (QuestionnaireRunUiAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(QuestionnaireRunUiAction.ExitDismissed) },
        title = { Text(stringResource(R.string.run_exit_title)) },
        text = { Text(stringResource(R.string.run_exit_body)) },
        confirmButton = {
            TextButton(onClick = { onAction(QuestionnaireRunUiAction.ExitConfirmed) }) { Text(stringResource(R.string.run_exit_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(QuestionnaireRunUiAction.ExitDismissed) }) { Text(stringResource(R.string.run_exit_stay)) }
        },
    )
}

/** Unpublished or removed: nothing to retry, so offer the way back to the list. */
@Composable
private fun RunErrorContent(
    error: ContentLoadError,
    onAction: (QuestionnaireRunUiAction) -> Unit,
    modifier: Modifier,
) {
    if (error == ContentLoadError.UNAVAILABLE) {
        MessageContent(
            title = stringResource(R.string.run_unavailable_title),
            body = stringResource(R.string.run_unavailable_body),
            actionLabel = stringResource(R.string.run_back_to_list),
            onAction = { onAction(QuestionnaireRunUiAction.ExitConfirmed) },
            modifier = modifier,
        )
    } else {
        ContentLoadErrorContent(error = error, onRetry = { onAction(QuestionnaireRunUiAction.Retry) }, modifier = modifier)
    }
}
