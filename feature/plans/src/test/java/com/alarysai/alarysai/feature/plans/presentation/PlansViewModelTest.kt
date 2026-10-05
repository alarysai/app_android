package com.alarysai.alarysai.feature.plans.presentation

import app.cash.turbine.test
import com.alarysai.alarysai.core.testing.MainDispatcherRule
import com.alarysai.alarysai.feature.plans.domain.model.BillingError
import com.alarysai.alarysai.feature.plans.domain.model.BillingException
import com.alarysai.alarysai.feature.plans.domain.model.BillingPeriod
import com.alarysai.alarysai.feature.plans.domain.model.Catalog
import com.alarysai.alarysai.feature.plans.domain.model.CreditPack
import com.alarysai.alarysai.feature.plans.domain.model.PlanOffer
import com.alarysai.alarysai.feature.plans.domain.model.PurchaseUpdate
import com.alarysai.alarysai.feature.plans.domain.model.StorePurchase
import com.alarysai.alarysai.feature.plans.domain.model.SubscriptionPlan
import com.alarysai.alarysai.feature.plans.domain.repository.BillingRepository
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseHost
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseVerifier
import com.alarysai.alarysai.feature.plans.domain.repository.VerificationResult
import com.alarysai.alarysai.feature.plans.domain.usecase.ProcessPurchaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PlansViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val basic = SubscriptionPlan("alarys_plano_basico", "Básico", "40 créditos por mês", PlanOffer("offer-basic", "R$ 19,90", BillingPeriod.MONTH))
    private val pro = SubscriptionPlan("alarys_plano_pro", "Pro", "150 créditos por mês", PlanOffer("offer-pro", "R$ 49,90", BillingPeriod.MONTH))
    private val pack = CreditPack("alarys_creditos_50", "50 créditos", "Use quando quiser", "R$ 14,90")
    private val host = object : PurchaseHost {}

    private fun purchase(id: String, subscription: Boolean, pending: Boolean = false, acknowledged: Boolean = false) =
        StorePurchase(listOf(id), "token-$id", null, subscription, pending, acknowledged)

    private class FakeBilling(
        var catalog: () -> Catalog,
        var owned: List<StorePurchase> = emptyList(),
        var launchError: BillingError? = null,
    ) : BillingRepository {
        val updates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = 8)
        val launched = mutableListOf<Pair<String, String?>>()

        override suspend fun loadCatalog() = catalog()
        override suspend fun ownedPurchases() = owned
        override suspend fun launchPurchase(host: PurchaseHost, productId: String, offerToken: String?) {
            launchError?.let { throw BillingException(it) }
            launched += productId to offerToken
        }
        override fun purchaseUpdates(): Flow<PurchaseUpdate> = updates
    }

    private class FakeVerifier(var result: VerificationResult = VerificationResult.UNAVAILABLE) : PurchaseVerifier {
        val verified = mutableListOf<String>()
        override suspend fun verify(purchase: StorePurchase): VerificationResult {
            verified += purchase.purchaseToken
            return result
        }
    }

    private val verifier = FakeVerifier()

    private fun viewModel(billing: BillingRepository, enabled: Boolean = true) =
        PlansViewModel(billing, ProcessPurchaseUseCase(verifier), enabled)

    private fun PlansViewModel.ready() = uiState.value.content as PlansContent.Ready

    @Test
    fun `shows plans and packs from the Play Store, marking the current plan`() {
        val billing = FakeBilling({ Catalog(listOf(basic, pro), listOf(pack)) }, owned = listOf(purchase(basic.productId, subscription = true, acknowledged = true)))
        val viewModel = viewModel(billing)

        assertEquals(
            PlansContent.Ready(
                plans = listOf(
                    PlanUi(basic.productId, "Básico", "40 créditos por mês", "R$ 19,90", BillingPeriod.MONTH, "offer-basic", isCurrent = true),
                    PlanUi(pro.productId, "Pro", "150 créditos por mês", "R$ 49,90", BillingPeriod.MONTH, "offer-pro", isCurrent = false),
                ),
                creditPacks = listOf(CreditPackUi(pack.productId, "50 créditos", "Use quando quiser", "R$ 14,90")),
            ),
            viewModel.uiState.value.content,
        )
    }

    @Test
    fun `no products, no Play Store and other failures have their own states`() {
        assertEquals(PlansContent.Empty, viewModel(FakeBilling({ Catalog(emptyList(), emptyList()) })).uiState.value.content)
        assertEquals(
            PlansContent.StoreUnavailable,
            viewModel(FakeBilling({ throw BillingException(BillingError.UNAVAILABLE) })).uiState.value.content,
        )
        assertEquals(
            PlansContent.Error(BillingError.NETWORK),
            viewModel(FakeBilling({ throw BillingException(BillingError.NETWORK) })).uiState.value.content,
        )
    }

    @Test
    fun `retry loads the catalog again`() {
        val billing = FakeBilling({ throw BillingException(BillingError.NETWORK) })
        val viewModel = viewModel(billing)

        billing.catalog = { Catalog(listOf(basic), emptyList()) }
        viewModel.onAction(PlansUiAction.Retry)

        assertEquals(1, viewModel.ready().plans.size)
    }

    @Test
    fun `subscribing opens the purchase sheet with the plan offer, buying a pack without one`() {
        val billing = FakeBilling({ Catalog(listOf(basic), listOf(pack)) })
        val viewModel = viewModel(billing)
        val plan = viewModel.ready().plans.single()

        viewModel.onAction(PlansUiAction.SubscribeClicked(plan, host))
        assertEquals(basic.productId, viewModel.uiState.value.purchasingProductId)
        billing.updates.tryEmit(PurchaseUpdate.Cancelled)
        viewModel.onAction(PlansUiAction.BuyCreditsClicked(viewModel.ready().creditPacks.single(), host))

        assertEquals(listOf(basic.productId to "offer-basic", pack.productId to null), billing.launched)
    }

    @Test
    fun `release builds cannot buy yet`() {
        val billing = FakeBilling({ Catalog(listOf(basic), emptyList()) })
        val viewModel = viewModel(billing, enabled = false)

        viewModel.onAction(PlansUiAction.SubscribeClicked(viewModel.ready().plans.single(), host))

        assertEquals(emptyList<Pair<String, String?>>(), billing.launched)
        assertNull(viewModel.uiState.value.purchasingProductId)
    }

    @Test
    fun `a new purchase goes to the server and the user is told it awaits delivery`() = runTest {
        val billing = FakeBilling({ Catalog(emptyList(), listOf(pack)) })
        val viewModel = viewModel(billing)

        viewModel.events.test {
            billing.updates.tryEmit(PurchaseUpdate.Purchased(listOf(purchase(pack.productId, subscription = false))))
            assertEquals(PlansUiEvent.ShowMessage(PlansMessage.AWAITING_SERVER), awaitItem())
        }
        assertEquals(listOf("token-${pack.productId}"), verifier.verified)
    }

    @Test
    fun `a granted subscription becomes the current plan`() = runTest {
        verifier.result = VerificationResult.GRANTED
        val billing = FakeBilling({ Catalog(listOf(basic, pro), emptyList()) })
        val viewModel = viewModel(billing)
        val bought = purchase(pro.productId, subscription = true)

        viewModel.events.test {
            billing.owned = listOf(bought)
            billing.updates.tryEmit(PurchaseUpdate.Purchased(listOf(bought)))
            assertEquals(PlansUiEvent.ShowMessage(PlansMessage.DELIVERED), awaitItem())
        }
        assertEquals(listOf(false, true), viewModel.ready().plans.map { it.isCurrent })
    }

    @Test
    fun `pending payments, failures and launch errors are reported`() = runTest {
        val billing = FakeBilling({ Catalog(listOf(basic), listOf(pack)) }, launchError = BillingError.ALREADY_OWNED)
        val viewModel = viewModel(billing)

        viewModel.events.test {
            billing.updates.tryEmit(PurchaseUpdate.Purchased(listOf(purchase(pack.productId, subscription = false, pending = true))))
            assertEquals(PlansUiEvent.ShowMessage(PlansMessage.PENDING), awaitItem())

            billing.updates.tryEmit(PurchaseUpdate.Failed(BillingError.NETWORK))
            assertEquals(PlansUiEvent.ShowMessage(PlansMessage.NETWORK), awaitItem())

            viewModel.onAction(PlansUiAction.SubscribeClicked(viewModel.ready().plans.single(), host))
            assertEquals(PlansUiEvent.ShowMessage(PlansMessage.ALREADY_OWNED), awaitItem())
        }
        assertNull(viewModel.uiState.value.purchasingProductId)
    }

    @Test
    fun `undelivered purchases are sent to the server again on load, pending ones are not`() {
        viewModel(
            FakeBilling(
                { Catalog(listOf(basic), emptyList()) },
                owned = listOf(
                    purchase("a", subscription = false),
                    purchase("b", subscription = false, pending = true),
                    purchase("c", subscription = true, acknowledged = true),
                ),
            ),
        )

        assertEquals(listOf("token-a"), verifier.verified)
    }

    @Test
    fun `manage subscriptions opens Google Play`() = runTest {
        val viewModel = viewModel(FakeBilling({ Catalog(listOf(basic), emptyList()) }))

        viewModel.events.test {
            viewModel.onAction(PlansUiAction.ManageSubscriptionsClicked)
            assertEquals(PlansUiEvent.OpenManageSubscriptions, awaitItem())
        }
    }
}
