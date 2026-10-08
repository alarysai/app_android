package com.alarysai.alarysai.feature.tips.presentation.viewmodel

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LanguageProvider
import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.tips.domain.model.Tip
import com.alarysai.alarysai.feature.tips.domain.model.TipCategory
import com.alarysai.alarysai.feature.tips.domain.repository.TipRepository
import com.alarysai.alarysai.feature.tips.presentation.action.TipsUiAction
import com.alarysai.alarysai.feature.tips.presentation.state.TipItemUi
import com.alarysai.alarysai.feature.tips.presentation.state.TipsContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TipsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ethics = TipCategory("etica", LocalizedText(pt = "Ética", en = "Ethics"), 0)
    private val knowledge = TipCategory("conhecimento", LocalizedText(pt = "Conhecimento"), 1)
    private val citeSources = Tip(
        id = "t1",
        categoryId = "etica",
        text = LocalizedText(pt = "Cite as fontes.", en = "Cite the sources."),
        imageUrl = null,
        languages = setOf(Language.PT, Language.EN),
        order = 0,
    )
    private val askExamples = Tip("t2", "conhecimento", LocalizedText(pt = "Peça exemplos."), null, setOf(Language.PT), 1)

    /** Buffered so tryEmit succeeds while the ViewModel is subscribed. */
    private fun <T> updates() = MutableSharedFlow<ContentList<T>>(extraBufferCapacity = 8)

    /** Tips flows are handed out per subscription, so retries can be followed. */
    private class FakeRepository(
        private val categories: Flow<ContentList<TipCategory>>,
        vararg tips: Flow<ContentList<Tip>>,
    ) : TipRepository {
        private val pendingTips = ArrayDeque(tips.toList())
        val requestedCategoryIds = mutableListOf<String?>()

        override fun observeActiveCategories() = categories

        override fun observeActiveTips(categoryId: String?): Flow<ContentList<Tip>> {
            requestedCategoryIds += categoryId
            return pendingTips.removeFirst()
        }
    }

    private class FixedLanguage(private val language: Language) : LanguageProvider {
        override fun currentLanguage() = language
    }

    private fun viewModel(repository: TipRepository, language: Language = Language.PT) =
        TipsViewModel(repository, FixedLanguage(language))

    @Test
    fun `starts loading all tips`() {
        val repository = FakeRepository(updates(), updates())
        val viewModel = viewModel(repository)

        assertEquals(TipsContent.Loading, viewModel.uiState.value.content)
        assertEquals(listOf<String?>(null), repository.requestedCategoryIds)
    }

    @Test
    fun `shows all tips with their category name in the user language`() {
        val categories = updates<TipCategory>()
        val tips = updates<Tip>()
        val viewModel = viewModel(FakeRepository(categories, tips), Language.EN)

        categories.tryEmit(ContentList(listOf(ethics, knowledge), isFromCache = false))
        tips.tryEmit(ContentList(listOf(citeSources, askExamples), isFromCache = false))

        assertEquals(
            TipsContent.Success(
                tips = listOf(
                    TipItemUi("t1", "Cite the sources.", null, categoryName = "Ethics", isInUserLanguage = true),
                    TipItemUi("t2", "Peça exemplos.", null, categoryName = "Conhecimento", isInUserLanguage = false),
                ),
                isOffline = false,
            ),
            viewModel.uiState.value.content,
        )
    }

    @Test
    fun `category names arriving after the tips fill the labels in`() {
        val categories = updates<TipCategory>()
        val tips = updates<Tip>()
        val viewModel = viewModel(FakeRepository(categories, tips))

        tips.tryEmit(ContentList(listOf(citeSources), isFromCache = false))
        categories.tryEmit(ContentList(listOf(ethics), isFromCache = false))

        val tip = (viewModel.uiState.value.content as TipsContent.Success).tips.single()
        assertEquals("Ética", tip.categoryName)
    }

    @Test
    fun `a deactivated category drops its label`() {
        val categories = updates<TipCategory>()
        val tips = updates<Tip>()
        val viewModel = viewModel(FakeRepository(categories, tips))
        categories.tryEmit(ContentList(listOf(ethics), isFromCache = false))
        tips.tryEmit(ContentList(listOf(citeSources), isFromCache = false))

        categories.tryEmit(ContentList(emptyList(), isFromCache = false))

        assertEquals(null, (viewModel.uiState.value.content as TipsContent.Success).tips.single().categoryName)
    }

    @Test
    fun `empty and cached lists`() {
        val tips = updates<Tip>()
        val viewModel = viewModel(FakeRepository(updates(), tips))

        tips.tryEmit(ContentList(emptyList(), isFromCache = true))
        assertEquals(TipsContent.Empty(isOffline = true), viewModel.uiState.value.content)

        tips.tryEmit(ContentList(listOf(citeSources), isFromCache = true))
        assertEquals(true, (viewModel.uiState.value.content as TipsContent.Success).isOffline)
    }

    @Test
    fun `failing categories keep the tips without labels`() {
        val failingCategories = flow<ContentList<TipCategory>> { throw ContentLoadException(ContentLoadError.UNKNOWN) }
        val tips = updates<Tip>()
        val viewModel = viewModel(FakeRepository(failingCategories, tips))

        tips.tryEmit(ContentList(listOf(citeSources), isFromCache = false))

        assertEquals(null, (viewModel.uiState.value.content as TipsContent.Success).tips.single().categoryName)
    }

    @Test
    fun `failing tips show the error and retry subscribes again`() {
        val failing = flow<ContentList<Tip>> { throw ContentLoadException(ContentLoadError.OFFLINE) }
        val recovered = updates<Tip>()
        val repository = FakeRepository(updates(), failing, recovered)
        val viewModel = viewModel(repository)
        assertEquals(TipsContent.Error(ContentLoadError.OFFLINE), viewModel.uiState.value.content)

        viewModel.onAction(TipsUiAction.Retry)
        recovered.tryEmit(ContentList(listOf(citeSources), isFromCache = false))

        assertEquals(listOf<String?>(null, null), repository.requestedCategoryIds)
        assertEquals(1, (viewModel.uiState.value.content as TipsContent.Success).tips.size)
    }
}
