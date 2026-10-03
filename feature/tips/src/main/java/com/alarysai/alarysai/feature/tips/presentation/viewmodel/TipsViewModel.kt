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
import com.alarysai.alarysai.feature.tips.presentation.state.TipCategoryUi
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
 * Tips with category chips. Categories and tips are two live queries: if the categories fail,
 * the chips simply disappear and the tips (all of them) keep working.
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

    /** Last tips received, kept to re-render when the category names arrive after them. */
    private var latestTips: ContentList<Tip>? = null

    init {
        observeCategories()
        observeTips()
    }

    fun onAction(action: TipsUiAction) {
        when (action) {
            is TipsUiAction.CategorySelected -> selectCategory(action.categoryId)
            TipsUiAction.Retry -> {
                observeCategories()
                observeTips()
            }
        }
    }

    private fun selectCategory(categoryId: String?) {
        if (categoryId == _uiState.value.selectedCategoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        observeTips()
    }

    private fun observeCategories() {
        categoriesJob?.cancel()
        categoriesJob = repository.observeActiveCategories()
            .onEach { list -> onCategories(list.items) }
            .catch { _uiState.update { it.copy(categories = emptyList()) } }
            .launchIn(viewModelScope)
    }

    private fun onCategories(categories: List<TipCategory>) {
        val language = languageProvider.currentLanguage()
        val chips = categories.map { TipCategoryUi(it.id, it.name.resolve(language)) }
        val selected = _uiState.value.selectedCategoryId
        _uiState.update { it.copy(categories = chips) }
        if (selected != null && chips.none { it.id == selected }) {
            // The selected category was deactivated in the panel: go back to "All".
            _uiState.update { it.copy(selectedCategoryId = null) }
            observeTips()
        } else {
            latestTips?.let(::showTips)
        }
    }

    /** A failed listener stops emitting, so retrying or switching category means subscribing again. */
    private fun observeTips() {
        tipsJob?.cancel()
        latestTips = null
        _uiState.update { it.copy(content = TipsContent.Loading) }
        tipsJob = repository.observeActiveTips(_uiState.value.selectedCategoryId)
            .onEach { list ->
                latestTips = list
                showTips(list)
            }
            .catch { error -> _uiState.update { it.copy(content = TipsContent.Error(error.toContentLoadErrorOrUnknown())) } }
            .launchIn(viewModelScope)
    }

    private fun showTips(list: ContentList<Tip>) {
        val language = languageProvider.currentLanguage()
        _uiState.update { state ->
            state.copy(
                content = if (list.items.isEmpty()) {
                    TipsContent.Empty(isOffline = list.isFromCache)
                } else {
                    TipsContent.Success(
                        tips = list.items.map { it.toItemUi(language, state) },
                        isOffline = list.isFromCache,
                    )
                },
            )
        }
    }

    private fun Tip.toItemUi(language: Language, state: TipsUiState) = TipItemUi(
        id = id,
        text = text.resolve(language),
        imageUrl = imageUrl,
        categoryName = if (state.selectedCategoryId == null) {
            state.categories.firstOrNull { it.id == categoryId }?.name
        } else {
            null
        },
        isInUserLanguage = language in languages,
    )
}
