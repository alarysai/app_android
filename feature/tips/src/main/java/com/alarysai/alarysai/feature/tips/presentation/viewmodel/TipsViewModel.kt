package com.alarysai.alarysai.feature.tips.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.toContentLoadErrorOrUnknown
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.feature.tips.domain.model.Tip
import com.alarysai.alarysai.feature.tips.domain.model.TipCategory
import com.alarysai.alarysai.feature.tips.domain.repository.TipRepository
import com.alarysai.alarysai.feature.tips.presentation.action.TipsUiAction
import com.alarysai.alarysai.feature.tips.presentation.state.TipItemUi
import com.alarysai.alarysai.feature.tips.presentation.state.TipsContent
import com.alarysai.alarysai.feature.tips.presentation.state.TipsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * All active tips, each labeled with its category. Categories and tips are two live queries: if
 * the categories fail, the tips keep working without the label.
 */
@HiltViewModel
class TipsViewModel @Inject constructor(
    private val repository: TipRepository,
    private val languageProvider: LanguageProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TipsUiState())
    val uiState: StateFlow<TipsUiState> = _uiState.asStateFlow()

    private var categoriesJob: Job? = null
    private var tipsJob: Job? = null

    /** Category names in the user's language, by ID. */
    private var categoryNames: Map<String, String> = emptyMap()

    /** Last tips received, kept to re-render when the category names arrive after them. */
    private var latestTips: ContentList<Tip>? = null

    init {
        observeCategories()
        observeTips()
    }

    fun onAction(action: TipsUiAction) {
        when (action) {
            TipsUiAction.Retry -> {
                observeCategories()
                observeTips()
            }
        }
    }

    private fun observeCategories() {
        categoriesJob?.cancel()
        categoriesJob = repository.observeActiveCategories()
            .onEach { list -> onCategories(list.items) }
            .catch { onCategories(emptyList()) }
            .launchIn(viewModelScope)
    }

    private fun onCategories(categories: List<TipCategory>) {
        val language = languageProvider.currentLanguage()
        categoryNames = categories.associate { it.id to it.name.resolve(language) }
        latestTips?.let(::showTips)
    }

    /** A failed listener stops emitting, so retrying means subscribing again. */
    private fun observeTips() {
        tipsJob?.cancel()
        latestTips = null
        _uiState.update { it.copy(content = TipsContent.Loading) }
        tipsJob = repository.observeActiveTips(categoryId = null)
            .onEach { list ->
                latestTips = list
                showTips(list)
            }
            .catch { error -> _uiState.update { it.copy(content = TipsContent.Error(error.toContentLoadErrorOrUnknown())) } }
            .launchIn(viewModelScope)
    }

    private fun showTips(list: ContentList<Tip>) {
        val language = languageProvider.currentLanguage()
        val content = if (list.items.isEmpty()) {
            TipsContent.Empty(isOffline = list.isFromCache)
        } else {
            TipsContent.Success(tips = list.items.map { it.toItemUi(language) }, isOffline = list.isFromCache)
        }
        _uiState.update { it.copy(content = content) }
    }

    private fun Tip.toItemUi(language: Language) = TipItemUi(
        id = id,
        text = text.resolve(language),
        imageUrl = imageUrl,
        categoryName = categoryNames[categoryId],
        isInUserLanguage = language in languages,
    )
}
