package com.alarysai.alarysai.feature.questionnaires.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.toContentLoadErrorOrUnknown
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.navigation.QuestionnairesRoute
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire
import com.alarysai.alarysai.feature.questionnaires.domain.repository.QuestionnaireRepository
import com.alarysai.alarysai.feature.questionnaires.presentation.action.QuestionnairesUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.event.QuestionnairesUiEvent
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnaireItemUi
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnairesContent
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnairesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Lists every published questionnaire of the category, including the ones not fully translated
 * into the user's language: those show in Portuguese with a badge instead of being hidden.
 */
@HiltViewModel
class QuestionnairesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuestionnaireRepository,
    private val languageProvider: LanguageProvider,
) : ViewModel() {

    private val categoryId: String? =
        savedStateHandle.get<String>(QuestionnairesRoute.ARG_CATEGORY_ID)?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(
        QuestionnairesUiState(
            categoryName = savedStateHandle.get<String>(QuestionnairesRoute.ARG_CATEGORY_NAME).orEmpty(),
        ),
    )
    val uiState: StateFlow<QuestionnairesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuestionnairesUiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<QuestionnairesUiEvent> = _events.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observeQuestionnaires()
    }

    fun onAction(action: QuestionnairesUiAction) {
        when (action) {
            QuestionnairesUiAction.Retry -> observeQuestionnaires()
            is QuestionnairesUiAction.QuestionnaireClicked -> _events.tryEmit(
                QuestionnairesUiEvent.OpenQuestionnaire(action.questionnaire.id, action.questionnaire.title),
            )
        }
    }

    /** A failed listener stops emitting, so retrying means subscribing again. */
    private fun observeQuestionnaires() {
        val id = categoryId ?: return updateContent(QuestionnairesContent.Error(ContentLoadError.UNAVAILABLE))
        observeJob?.cancel()
        updateContent(QuestionnairesContent.Loading)
        observeJob = repository.observePublishedQuestionnaires(id)
            .onEach { updateContent(it.toContent(languageProvider.currentLanguage())) }
            .catch { updateContent(QuestionnairesContent.Error(it.toContentLoadErrorOrUnknown())) }
            .launchIn(viewModelScope)
    }

    private fun updateContent(content: QuestionnairesContent) {
        _uiState.update { it.copy(content = content) }
    }

    private fun ContentList<Questionnaire>.toContent(language: Language): QuestionnairesContent =
        if (items.isEmpty()) {
            QuestionnairesContent.Empty(isOffline = isFromCache)
        } else {
            QuestionnairesContent.Success(
                questionnaires = items.mapIndexed { index, questionnaire -> questionnaire.toItemUi(language, index) },
                isOffline = isFromCache,
            )
        }

    private fun Questionnaire.toItemUi(language: Language, index: Int) = QuestionnaireItemUi(
        id = id,
        title = title.resolve(language),
        description = description?.resolve(language),
        imageUrl = imageUrl,
        isInUserLanguage = language in languages,
        accentIndex = index,
    )
}
