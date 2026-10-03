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
import com.alarysai.alarysai.feature.tips.presentation.state.TipCategoryUi
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

    /** Tips flows are handed out per subscription, so category switches and retries can be followed. */
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
    fun `starts with all tips loading and no chips`() {
        val repository = FakeRepository(updates(), updates())
        val viewModel = viewModel(repository)

        assertEquals(TipsContent.Loading, viewModel.uiState.value.content)
        assertEquals(emptyList<TipCategoryUi>(), viewModel.uiState.value.categories)
        assertEquals(listOf<String?>(null), repository.requestedCategoryIds)
    }

    @Test
    fun `shows chips and all tips with their category name in the user language`() {
        val categories = updates<TipCategory>()
        val tips = updates<Tip>()
        val viewModel = viewModel(FakeRepository(categories, tips), Language.EN)

        categories.tryEmit(ContentList(listOf(ethics, knowledge), isFromCache = false))
        tips.tryEmit(ContentList(listOf(citeSources, askExamples), isFromCache = false))

        val state = viewModel.uiState.value
        assertEquals(listOf(TipCategoryUi("etica", "Ethics"), TipCategoryUi("conhecimento", "Conhecimento")), state.categories)
        assertEquals(
            TipsContent.Success(
                tips = listOf(
                    TipItemUi("t1", "Cite the sources.", null, categoryName = "Ethics", isInUserLanguage = true),
                    TipItemUi("t2", "Peça exemplos.", null, categoryName = "Conhecimento", isInUserLanguage = false),
                ),
                isOffline = false,
            ),
            state.content,
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
    fun `selecting a category queries it and hides the category label`() {
        val categories = updates<TipCategory>()
        val ethicsTips = updates<Tip>()
        val repository = FakeRepository(categories, updates(), ethicsTips)
        val viewModel = viewModel(repository)
        categories.tryEmit(ContentList(listOf(ethics, knowledge), isFromCache = false))

        viewModel.onAction(TipsUiAction.CategorySelected("etica"))
        assertEquals(TipsContent.Loading, viewModel.uiState.value.content)
        ethicsTips.tryEmit(ContentList(listOf(citeSources), isFromCache = false))

        assertEquals("etica", viewModel.uiState.value.selectedCategoryId)
        assertEquals(listOf<String?>(null, "etica"), repository.requestedCategoryIds)
        assertEquals(null, (viewModel.uiState.value.content as TipsContent.Success).tips.single().categoryName)
    }

    @Test
    fun `selecting the current category again does not query again`() {
        val repository = FakeRepository(updates(), updates())
        val viewModel = viewModel(repository)

        viewModel.onAction(TipsUiAction.CategorySelected(null))

        assertEquals(listOf<String?>(null), repository.requestedCategoryIds)
    }

    @Test
    fun `a selected category that is deactivated goes back to all`() {
        val categories = updates<TipCategory>()
        val repository = FakeRepository(categories, updates(), updates(), updates())
        val viewModel = viewModel(repository)
        categories.tryEmit(ContentList(listOf(ethics, knowledge), isFromCache = false))
        viewModel.onAction(TipsUiAction.CategorySelected("conhecimento"))

        categories.tryEmit(ContentList(listOf(ethics), isFromCache = false))

        assertEquals(null, viewModel.uiState.value.selectedCategoryId)
        assertEquals(listOf<String?>(null, "conhecimento", null), repository.requestedCategoryIds)
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
    fun `failing categories hide the chips but keep the tips`() {
        val failingCategories = flow<ContentList<TipCategory>> { throw ContentLoadException(ContentLoadError.UNKNOWN) }
        val tips = updates<Tip>()
        val viewModel = viewModel(FakeRepository(failingCategories, tips))

        tips.tryEmit(ContentList(listOf(citeSources), isFromCache = false))

        assertEquals(emptyList<TipCategoryUi>(), viewModel.uiState.value.categories)
        assertEquals(1, (viewModel.uiState.value.content as TipsContent.Success).tips.size)
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
