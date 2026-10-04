package com.alarysai.alarysai.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.toContentLoadErrorOrUnknown
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.core.common.text.matchesSearch
import com.alarysai.alarysai.feature.home.domain.model.QuestionnaireCategory
import com.alarysai.alarysai.feature.home.domain.repository.QuestionnaireCategoryRepository
import com.alarysai.alarysai.feature.home.presentation.action.HomeUiAction
import com.alarysai.alarysai.feature.home.presentation.event.ComingSoonFeature
import com.alarysai.alarysai.feature.home.presentation.event.HomeUiEvent
import com.alarysai.alarysai.feature.home.presentation.state.CategoriesSection
import com.alarysai.alarysai.feature.home.presentation.state.CategoryItemUi
import com.alarysai.alarysai.feature.home.presentation.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val categoryRepository: QuestionnaireCategoryRepository,
    private val languageProvider: LanguageProvider,
    private val sessionRepository: SessionRepository,
    private val profileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeUiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<HomeUiEvent> = _events.asSharedFlow()

    /** Last list from the repository, kept so the search can filter it without re-querying. */
    private var latestCategories: ContentList<CategoryItemUi>? = null
    private var observeJob: Job? = null

    init {
        observeCategories()
        observeUserName()
    }

    /** "Olá, Marina": the profile name, or the sign-in provider name while the profile loads. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUserName() {
        sessionRepository.observeUser()
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(null)
                } else {
                    profileRepository.observeProfile(user.uid)
                        .map { profile -> profile?.displayName ?: user.displayName }
                        .catch { emit(user.displayName) }
                }
            }
            .onEach { name -> _uiState.update { it.copy(userName = name?.substringBefore(' ')?.takeIf { first -> first.isNotBlank() }) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.SearchQueryChanged -> updateSearch(action.query)
            is HomeUiAction.ChatMessageChanged -> _uiState.update { it.copy(chatMessage = action.message) }
            HomeUiAction.ChatSendClicked -> onChatSend()
            HomeUiAction.NotificationsClicked -> showComingSoon(ComingSoonFeature.NOTIFICATIONS)
            HomeUiAction.BuyCreditsClicked -> showComingSoon(ComingSoonFeature.CREDITS)
            HomeUiAction.RetryCategories -> observeCategories()
            is HomeUiAction.CategoryClicked ->
                _events.tryEmit(HomeUiEvent.OpenCategory(action.category.id, action.category.name))
        }
    }

    /** A failed listener stops emitting, so retrying means subscribing again. */
    private fun observeCategories() {
        observeJob?.cancel()
        latestCategories = null
        _uiState.update { it.copy(categories = CategoriesSection.Loading) }
        observeJob = categoryRepository.observeActiveCategories()
            .onEach { list ->
                latestCategories = list.toItemsUi()
                refreshCategoriesSection()
            }
            .catch { error ->
                _uiState.update { it.copy(categories = CategoriesSection.Error(error.toContentLoadErrorOrUnknown())) }
            }
            .launchIn(viewModelScope)
    }

    private fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshCategoriesSection()
    }

    private fun refreshCategoriesSection() {
        val categories = latestCategories ?: return
        _uiState.update { state ->
            state.copy(categories = categories.toSection(state.searchQuery))
        }
    }

    private fun ContentList<CategoryItemUi>.toSection(query: String): CategoriesSection {
        if (items.isEmpty()) return CategoriesSection.Empty(isOffline = isFromCache)
        val visible = items.filter { it.name.matchesSearch(query) }
        return if (visible.isEmpty()) {
            CategoriesSection.NoSearchResults(query.trim())
        } else {
            CategoriesSection.Success(categories = visible, isOffline = isFromCache)
        }
    }

    private fun ContentList<QuestionnaireCategory>.toItemsUi(): ContentList<CategoryItemUi> {
        val language = languageProvider.currentLanguage()
        return ContentList(
            items = items.mapIndexed { index, category ->
                CategoryItemUi(
                    id = category.id,
                    name = category.name.resolve(language),
                    iconUrl = category.iconUrl,
                    accentIndex = index,
                )
            },
            isFromCache = isFromCache,
        )
    }

    private fun onChatSend() {
        if (_uiState.value.chatMessage.isBlank()) return
        showComingSoon(ComingSoonFeature.CHAT)
    }

    private fun showComingSoon(feature: ComingSoonFeature) {
        _events.tryEmit(HomeUiEvent.ShowComingSoon(feature))
    }
}
