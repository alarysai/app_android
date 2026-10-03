package com.alarysai.alarysai.feature.advertisers.presentation.viewmodel

import app.cash.turbine.test
import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser
import com.alarysai.alarysai.feature.advertisers.domain.repository.AdvertiserRepository
import com.alarysai.alarysai.feature.advertisers.domain.usecase.GroupAdvertisersByTypeUseCase
import com.alarysai.alarysai.feature.advertisers.presentation.action.AdvertisersUiAction
import com.alarysai.alarysai.feature.advertisers.presentation.event.AdvertisersUiEvent
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserGroupUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserItemUi
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertisersUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdvertisersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val shop = Advertiser("a1", "Loja X", null, "Parceiro", "https://lojax.com.br/promo", 0)
    private val school = Advertiser("a2", "Escola Y", null, "parceiro", "https://escolay.com.br", 1)
    private val sponsor = Advertiser("a3", "Marca Z", null, "Patrocínio", "https://marcaz.com.br", 2)

    /** Buffered so tryEmit succeeds while the ViewModel is subscribed. */
    private fun updates() = MutableSharedFlow<ContentList<Advertiser>>(extraBufferCapacity = 8)

    private class FakeRepository(vararg flows: Flow<ContentList<Advertiser>>) : AdvertiserRepository {
        private val pending = ArrayDeque(flows.toList())
        var subscriptions = 0
            private set

        override fun observeActiveAdvertisers(): Flow<ContentList<Advertiser>> {
            subscriptions++
            return pending.removeFirst()
        }
    }

    private fun viewModel(repository: AdvertiserRepository) = AdvertisersViewModel(repository, GroupAdvertisersByTypeUseCase())

    @Test
    fun `starts loading`() {
        assertEquals(AdvertisersUiState.Loading, viewModel(FakeRepository(updates())).uiState.value)
    }

    @Test
    fun `shows advertisers grouped by type`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))

        updates.tryEmit(ContentList(listOf(shop, school, sponsor), isFromCache = false))

        assertEquals(
            AdvertisersUiState.Success(
                groups = listOf(
                    AdvertiserGroupUi(
                        "Parceiro",
                        listOf(
                            AdvertiserItemUi("a1", "Loja X", null, "https://lojax.com.br/promo"),
                            AdvertiserItemUi("a2", "Escola Y", null, "https://escolay.com.br"),
                        ),
                    ),
                    AdvertiserGroupUi("Patrocínio", listOf(AdvertiserItemUi("a3", "Marca Z", null, "https://marcaz.com.br"))),
                ),
                isOffline = false,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `empty and cached lists`() {
        val updates = updates()
        val viewModel = viewModel(FakeRepository(updates))

        updates.tryEmit(ContentList(emptyList(), isFromCache = true))
        assertEquals(AdvertisersUiState.Empty(isOffline = true), viewModel.uiState.value)

        updates.tryEmit(ContentList(listOf(shop), isFromCache = true))
        assertEquals(true, (viewModel.uiState.value as AdvertisersUiState.Success).isOffline)
    }

    @Test
    fun `tapping an advertiser opens its link`() = runTest {
        val viewModel = viewModel(FakeRepository(updates()))

        viewModel.events.test {
            viewModel.onAction(AdvertisersUiAction.AdvertiserClicked(AdvertiserItemUi("a1", "Loja X", null, "https://lojax.com.br/promo")))
            assertEquals(AdvertisersUiEvent.OpenLink("https://lojax.com.br/promo"), awaitItem())
        }
    }

    @Test
    fun `failure shows the error and retry subscribes again`() {
        val failing = flow<ContentList<Advertiser>> { throw ContentLoadException(ContentLoadError.OFFLINE) }
        val recovered = updates()
        val repository = FakeRepository(failing, recovered)
        val viewModel = viewModel(repository)
        assertEquals(AdvertisersUiState.Error(ContentLoadError.OFFLINE), viewModel.uiState.value)

        viewModel.onAction(AdvertisersUiAction.Retry)
        recovered.tryEmit(ContentList(listOf(shop), isFromCache = false))

        assertEquals(2, repository.subscriptions)
        assertEquals(1, (viewModel.uiState.value as AdvertisersUiState.Success).groups.size)
    }
}
