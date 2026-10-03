package com.alarysai.alarysai.feature.home.presentation.viewmodel

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.home.domain.model.QuestionnaireCategory
import com.alarysai.alarysai.feature.home.domain.repository.QuestionnaireCategoryRepository
import com.alarysai.alarysai.feature.home.presentation.action.HomeUiAction
import com.alarysai.alarysai.feature.home.presentation.event.ComingSoonFeature
import com.alarysai.alarysai.feature.home.presentation.event.HomeUiEvent
import com.alarysai.alarysai.feature.home.presentation.state.CategoriesSection
import com.alarysai.alarysai.feature.home.presentation.state.CategoryItemUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val image = QuestionnaireCategory("img", LocalizedText(pt = "Imagem", en = "Image"), null, 0)
    private val video = QuestionnaireCategory("vid", LocalizedText(pt = "Vídeo"), null, 1)
    private val text = QuestionnaireCategory("txt", LocalizedText(pt = "Texto", en = "Text"), null, 2)

    /** Each subscription gets the next flow, so retry can be tested. */
    private class FakeRepository(vararg flows: Flow<ContentList<QuestionnaireCategory>>) :
        QuestionnaireCategoryRepository {
        private val pending = ArrayDeque(flows.toList())
        var subscriptions = 0
            private set

        override fun observeActiveCategories(): Flow<ContentList<QuestionnaireCategory>> {
            subscriptions++
            return pending.removeFirst()
        }
    }

    private class FixedLanguage(private val language: Language) : LanguageProvider {
        override fun currentLanguage() = language
    }

    /** Buffered so tryEmit succeeds while the ViewModel is subscribed. */
    private fun updates() = MutableSharedFlow<ContentList<QuestionnaireCategory>>(extraBufferCapacity = 8)

    private fun viewModel(repository: QuestionnaireCategoryRepository, language: Language = Language.PT) =
        HomeViewModel(repository, FixedLanguage(language))

    private fun HomeViewModel.successCategories() =
        (uiState.value.categories as CategoriesSection.Success).categories

    @Test
    fun `starts with categories loading and no user or credits data`() {
        val viewModel = viewModel(FakeRepository(updates()))

        val state = viewModel.uiState.value
        assertEquals(CategoriesSection.Loading, state.categories)
        assertNull(state.userName)
        assertNull(state.planUsage)
    }

    @Test
    fun `shows categories in the user language with a stable accent per position`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates), Language.EN)

        updates.tryEmit(ContentList(listOf(image, video), isFromCache = false))

        assertEquals(
            CategoriesSection.Success(
                categories = listOf(CategoryItemUi("img", "Image", null, 0), CategoryItemUi("vid", "Vídeo", null, 1)),
                isOffline = false,
            ),
            viewModel.uiState.value.categories,
        )
    }

    @Test
    fun `cached list is flagged as offline`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))

        updates.tryEmit(ContentList(listOf(image), isFromCache = true))

        assertEquals(true, (viewModel.uiState.value.categories as CategoriesSection.Success).isOffline)
    }

    @Test
    fun `empty list shows the empty section`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))

        updates.tryEmit(ContentList(emptyList(), isFromCache = false))

        assertEquals(CategoriesSection.Empty(isOffline = false), viewModel.uiState.value.categories)
    }

    @Test
    fun `search filters ignoring case and accents and keeps each card accent`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))
        updates.tryEmit(ContentList(listOf(image, video, text), isFromCache = false))

        viewModel.onAction(HomeUiAction.SearchQueryChanged("VIDEO"))

        assertEquals("VIDEO", viewModel.uiState.value.searchQuery)
        assertEquals(listOf(CategoryItemUi("vid", "Vídeo", null, 1)), viewModel.successCategories())
    }

    @Test
    fun `search without matches shows no results, clearing it shows everything again`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))
        updates.tryEmit(ContentList(listOf(image, video), isFromCache = false))

        viewModel.onAction(HomeUiAction.SearchQueryChanged(" música "))
        assertEquals(CategoriesSection.NoSearchResults("música"), viewModel.uiState.value.categories)

        viewModel.onAction(HomeUiAction.SearchQueryChanged(""))
        assertEquals(2, viewModel.successCategories().size)
    }

    @Test
    fun `search typed before the list arrives is applied when it arrives`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))

        viewModel.onAction(HomeUiAction.SearchQueryChanged("tex"))
        assertEquals(CategoriesSection.Loading, viewModel.uiState.value.categories)

        updates.tryEmit(ContentList(listOf(image, text), isFromCache = false))
        assertEquals(listOf("txt"), viewModel.successCategories().map { it.id })
    }

    @Test
    fun `live updates keep the current search`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))
        updates.tryEmit(ContentList(listOf(image), isFromCache = false))
        viewModel.onAction(HomeUiAction.SearchQueryChanged("vid"))

        updates.tryEmit(ContentList(listOf(image, video), isFromCache = false))

        assertEquals(listOf("vid"), viewModel.successCategories().map { it.id })
    }

    @Test
    fun `failure shows the mapped error and unexpected failures the generic one`() {
        val offline = flow<ContentList<QuestionnaireCategory>> { throw ContentLoadException(ContentLoadError.OFFLINE) }
        assertEquals(
            CategoriesSection.Error(ContentLoadError.OFFLINE),
            viewModel(FakeRepository(offline)).uiState.value.categories,
        )

        val unexpected = flow<ContentList<QuestionnaireCategory>> { throw IllegalStateException() }
        assertEquals(
            CategoriesSection.Error(ContentLoadError.UNKNOWN),
            viewModel(FakeRepository(unexpected)).uiState.value.categories,
        )
    }

    @Test
    fun `retry subscribes again and recovers`() {
        val failing = flow<ContentList<QuestionnaireCategory>> { throw ContentLoadException(ContentLoadError.UNKNOWN) }
        val recovered = updates()
        val repository = FakeRepository(failing, recovered)
        val viewModel = viewModel(repository)

        viewModel.onAction(HomeUiAction.RetryCategories)
        assertEquals(CategoriesSection.Loading, viewModel.uiState.value.categories)

        recovered.tryEmit(ContentList(listOf(image), isFromCache = false))
        assertEquals(2, repository.subscriptions)
        assertEquals(1, viewModel.successCategories().size)
    }

    @Test
    fun `chat message is kept in the state`() {
        val viewModel = viewModel(FakeRepository(updates()))

        viewModel.onAction(HomeUiAction.ChatMessageChanged("Crie um post"))

        assertEquals("Crie um post", viewModel.uiState.value.chatMessage)
    }

    @Test
    fun `sending a chat message announces it is coming soon, a blank one does nothing`() = runTest {
        val viewModel = viewModel(FakeRepository(updates()))

        viewModel.events.test {
            viewModel.onAction(HomeUiAction.ChatSendClicked)
            expectNoEvents()

            viewModel.onAction(HomeUiAction.ChatMessageChanged("Oi"))
            viewModel.onAction(HomeUiAction.ChatSendClicked)
            assertEquals(HomeUiEvent.ShowComingSoon(ComingSoonFeature.CHAT), awaitItem())
        }
    }

    @Test
    fun `notifications and buy credits announce they are coming soon`() = runTest {
        val viewModel = viewModel(FakeRepository(updates()))

        viewModel.events.test {
            viewModel.onAction(HomeUiAction.NotificationsClicked)
            assertEquals(HomeUiEvent.ShowComingSoon(ComingSoonFeature.NOTIFICATIONS), awaitItem())

            viewModel.onAction(HomeUiAction.BuyCreditsClicked)
            assertEquals(HomeUiEvent.ShowComingSoon(ComingSoonFeature.CREDITS), awaitItem())
        }
    }

    @Test
    fun `tapping a category opens its questionnaires with the shown name`() = runTest {
        val viewModel = viewModel(FakeRepository(updates()))

        viewModel.events.test {
            viewModel.onAction(HomeUiAction.CategoryClicked(CategoryItemUi("img", "Image", null, 0)))
            assertEquals(HomeUiEvent.OpenCategory(categoryId = "img", categoryName = "Image"), awaitItem())
        }
    }
}
