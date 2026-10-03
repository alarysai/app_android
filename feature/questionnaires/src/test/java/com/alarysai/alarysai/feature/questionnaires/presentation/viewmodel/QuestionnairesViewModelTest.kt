package com.alarysai.alarysai.feature.questionnaires.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.navigation.QuestionnairesRoute
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire
import com.alarysai.alarysai.feature.questionnaires.domain.model.QuestionnaireWithSteps
import com.alarysai.alarysai.feature.questionnaires.domain.repository.QuestionnaireRepository
import app.cash.turbine.test
import com.alarysai.alarysai.feature.questionnaires.presentation.action.QuestionnairesUiAction
import com.alarysai.alarysai.feature.questionnaires.presentation.event.QuestionnairesUiEvent
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnaireItemUi
import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnairesContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuestionnairesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ethics = Questionnaire(
        id = "q1",
        categoryId = "cat1",
        title = LocalizedText(pt = "Ética na IA", en = "AI ethics"),
        description = LocalizedText(pt = "Use IA com responsabilidade.", en = "Use AI responsibly."),
        imageUrl = null,
        languages = setOf(Language.PT, Language.EN),
        order = 0,
    )
    private val portugueseOnly = Questionnaire(
        id = "q2",
        categoryId = "cat1",
        title = LocalizedText(pt = "Post para redes"),
        description = null,
        imageUrl = null,
        languages = setOf(Language.PT),
        order = 1,
    )

    /** Each subscription gets the next flow, so retry can be tested. */
    private class FakeRepository(vararg flows: Flow<ContentList<Questionnaire>>) : QuestionnaireRepository {
        private val pending = ArrayDeque(flows.toList())
        val requestedCategoryIds = mutableListOf<String>()

        override fun observePublishedQuestionnaires(categoryId: String): Flow<ContentList<Questionnaire>> {
            requestedCategoryIds += categoryId
            return pending.removeFirst()
        }

        override suspend fun getQuestionnaireWithSteps(questionnaireId: String): QuestionnaireWithSteps =
            error("not used by the list")
    }

    private class FixedLanguage(private val language: Language) : LanguageProvider {
        override fun currentLanguage() = language
    }

    /** Buffered so tryEmit succeeds while the ViewModel is subscribed. */
    private fun updates() = MutableSharedFlow<ContentList<Questionnaire>>(extraBufferCapacity = 8)

    private fun args(categoryId: String? = "cat1", categoryName: String? = "Texto") = SavedStateHandle(
        buildMap {
            categoryId?.let { put(QuestionnairesRoute.ARG_CATEGORY_ID, it) }
            categoryName?.let { put(QuestionnairesRoute.ARG_CATEGORY_NAME, it) }
        },
    )

    private fun viewModel(
        repository: QuestionnaireRepository,
        language: Language = Language.PT,
        savedStateHandle: SavedStateHandle = args(),
    ) = QuestionnairesViewModel(savedStateHandle, repository, FixedLanguage(language))

    @Test
    fun `uses the category name as title and loads that category`() {
        val repository = FakeRepository(updates())
        val viewModel = viewModel(repository)

        assertEquals("Texto", viewModel.uiState.value.categoryName)
        assertEquals(QuestionnairesContent.Loading, viewModel.uiState.value.content)
        assertEquals(listOf("cat1"), repository.requestedCategoryIds)
    }

    @Test
    fun `shows questionnaires in the user language and flags the untranslated ones`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates), Language.EN)

        updates.tryEmit(ContentList(listOf(ethics, portugueseOnly), isFromCache = false))

        assertEquals(
            QuestionnairesContent.Success(
                questionnaires = listOf(
                    QuestionnaireItemUi("q1", "AI ethics", "Use AI responsibly.", null, isInUserLanguage = true, accentIndex = 0),
                    QuestionnaireItemUi("q2", "Post para redes", null, null, isInUserLanguage = false, accentIndex = 1),
                ),
                isOffline = false,
            ),
            viewModel.uiState.value.content,
        )
    }

    @Test
    fun `portuguese users see every questionnaire as translated`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates), Language.PT)

        updates.tryEmit(ContentList(listOf(ethics, portugueseOnly), isFromCache = false))

        val items = (viewModel.uiState.value.content as QuestionnairesContent.Success).questionnaires
        assertEquals(listOf(true, true), items.map { it.isInUserLanguage })
    }

    @Test
    fun `cached list is flagged as offline`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))

        updates.tryEmit(ContentList(listOf(ethics), isFromCache = true))

        assertEquals(true, (viewModel.uiState.value.content as QuestionnairesContent.Success).isOffline)
    }

    @Test
    fun `empty category shows the empty state`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))

        updates.tryEmit(ContentList(emptyList(), isFromCache = false))

        assertEquals(QuestionnairesContent.Empty(isOffline = false), viewModel.uiState.value.content)
    }

    @Test
    fun `failure shows the mapped error`() {
        val failing = flow<ContentList<Questionnaire>> { throw ContentLoadException(ContentLoadError.OFFLINE) }
        val viewModel = viewModel(FakeRepository(failing))

        assertEquals(QuestionnairesContent.Error(ContentLoadError.OFFLINE), viewModel.uiState.value.content)
    }

    @Test
    fun `missing category id shows unavailable without querying`() {
        val repository = FakeRepository()
        val viewModel = viewModel(repository, savedStateHandle = args(categoryId = null, categoryName = null))

        assertEquals("", viewModel.uiState.value.categoryName)
        assertEquals(QuestionnairesContent.Error(ContentLoadError.UNAVAILABLE), viewModel.uiState.value.content)
        assertEquals(emptyList<String>(), repository.requestedCategoryIds)
    }

    @Test
    fun `retry subscribes again and recovers`() {
        val failing = flow<ContentList<Questionnaire>> { throw ContentLoadException(ContentLoadError.UNKNOWN) }
        val recovered = updates()
        val repository = FakeRepository(failing, recovered)
        val viewModel = viewModel(repository)

        viewModel.onAction(QuestionnairesUiAction.Retry)
        assertEquals(QuestionnairesContent.Loading, viewModel.uiState.value.content)

        recovered.tryEmit(ContentList(listOf(ethics), isFromCache = false))
        assertEquals(listOf("cat1", "cat1"), repository.requestedCategoryIds)
        assertEquals(1, (viewModel.uiState.value.content as QuestionnairesContent.Success).questionnaires.size)
    }

    @Test
    fun `tapping a questionnaire opens it with the shown title`() = runTest {
        val viewModel = viewModel(FakeRepository(updates()))

        viewModel.events.test {
            viewModel.onAction(
                QuestionnairesUiAction.QuestionnaireClicked(QuestionnaireItemUi("q1", "AI ethics", null, null, true, 0)),
            )
            assertEquals(QuestionnairesUiEvent.OpenQuestionnaire(questionnaireId = "q1", title = "AI ethics"), awaitItem())
        }
    }
}
