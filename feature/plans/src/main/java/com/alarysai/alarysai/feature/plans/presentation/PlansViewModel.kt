package com.alarysai.alarysai.feature.plans.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarysai.alarysai.feature.plans.di.PlansModule
import com.alarysai.alarysai.feature.plans.domain.model.BillingError
import com.alarysai.alarysai.feature.plans.domain.model.BillingException
import com.alarysai.alarysai.feature.plans.domain.model.Catalog
import com.alarysai.alarysai.feature.plans.domain.model.PurchaseOutcome
import com.alarysai.alarysai.feature.plans.domain.model.PurchaseUpdate
import com.alarysai.alarysai.feature.plans.domain.model.StorePurchase
import com.alarysai.alarysai.feature.plans.domain.repository.BillingRepository
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseHost
import com.alarysai.alarysai.feature.plans.domain.usecase.ProcessPurchaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

/**
 * The Plans tab: subscriptions and credit packs from the Play Store, the current plan, and the
 * purchase flow. Purchases go to the server through [ProcessPurchaseUseCase]; the app never grants
 * credits itself.
 */
@HiltViewModel
class PlansViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val processPurchase: ProcessPurchaseUseCase,
    @Named(PlansModule.PURCHASES_ENABLED) purchasesEnabled: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlansUiState(purchasesEnabled = purchasesEnabled))
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PlansUiEvent>(extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<PlansUiEvent> = _events.asSharedFlow()

    init {
        billingRepository.purchaseUpdates().onEach(::onPurchaseUpdate).launchIn(viewModelScope)
        load()
    }

    fun onAction(action: PlansUiAction) {
        when (action) {
            is PlansUiAction.SubscribeClicked -> purchase(action.host, action.plan.productId, action.plan.offerToken)
            is PlansUiAction.BuyCreditsClicked -> purchase(action.host, action.pack.productId, offerToken = null)
            PlansUiAction.ManageSubscriptionsClicked -> _events.tryEmit(PlansUiEvent.OpenManageSubscriptions)
            PlansUiAction.Retry -> load()
        }
    }

    private fun load() {
        _uiState.update { it.copy(content = PlansContent.Loading) }
        viewModelScope.launch {
            try {
                val catalog = billingRepository.loadCatalog()
                val owned = try {
                    billingRepository.ownedPurchases()
                } catch (_: BillingException) {
                    emptyList()
                }
                _uiState.update { it.copy(content = catalog.toContent(owned)) }
                retryUndelivered(owned)
            } catch (failure: BillingException) {
                _uiState.update {
                    it.copy(content = if (failure.error == BillingError.UNAVAILABLE) PlansContent.StoreUnavailable else PlansContent.Error(failure.error))
                }
            }
        }
    }

    /** Owned but not delivered (app closed mid-purchase, server down): sent to the server again, quietly. */
    private suspend fun retryUndelivered(owned: List<StorePurchase>) {
        owned.filter { !it.isAcknowledged && !it.isPending }.forEach { processPurchase(it) }
    }

    private fun purchase(host: PurchaseHost, productId: String, offerToken: String?) {
        val state = _uiState.value
        if (!state.purchasesEnabled || state.purchasingProductId != null) return
        _uiState.update { it.copy(purchasingProductId = productId) }
        viewModelScope.launch {
            try {
                billingRepository.launchPurchase(host, productId, offerToken)
            } catch (failure: BillingException) {
                _uiState.update { it.copy(purchasingProductId = null) }
                _events.tryEmit(PlansUiEvent.ShowMessage(failure.error.toMessage()))
            }
        }
    }

    private suspend fun onPurchaseUpdate(update: PurchaseUpdate) {
        _uiState.update { it.copy(purchasingProductId = null) }
        when (update) {
            is PurchaseUpdate.Purchased -> {
                update.purchases.forEach { purchase ->
                    _events.tryEmit(PlansUiEvent.ShowMessage(processPurchase(purchase).toMessage()))
                }
                refreshCurrentPlan()
            }
            PurchaseUpdate.Cancelled -> Unit
            is PurchaseUpdate.Failed -> _events.tryEmit(PlansUiEvent.ShowMessage(update.error.toMessage()))
        }
    }

    private suspend fun refreshCurrentPlan() {
        val ready = _uiState.value.content as? PlansContent.Ready ?: return
        val owned = try {
            billingRepository.ownedPurchases()
        } catch (_: BillingException) {
            return
        }
        val current = owned.currentSubscriptionIds()
        _uiState.update { it.copy(content = ready.copy(plans = ready.plans.map { plan -> plan.copy(isCurrent = plan.productId in current) })) }
    }

    private fun Catalog.toContent(owned: List<StorePurchase>): PlansContent {
        if (isEmpty) return PlansContent.Empty
        val current = owned.currentSubscriptionIds()
        return PlansContent.Ready(
            plans = plans.map { plan ->
                PlanUi(
                    productId = plan.productId,
                    name = plan.name,
                    description = plan.description,
                    price = plan.offer.formattedPrice,
                    period = plan.offer.billingPeriod,
                    offerToken = plan.offer.offerToken,
                    isCurrent = plan.productId in current,
                )
            },
            creditPacks = creditPacks.map { CreditPackUi(it.productId, it.name, it.description, it.formattedPrice) },
        )
    }

    /** Active (not pending) subscriptions, as the Play Store sees them. The server has the final word. */
    private fun List<StorePurchase>.currentSubscriptionIds(): Set<String> =
        filter { it.isSubscription && !it.isPending }.flatMap { it.productIds }.toSet()

    private fun PurchaseOutcome.toMessage() = when (this) {
        PurchaseOutcome.DELIVERED -> PlansMessage.DELIVERED
        PurchaseOutcome.PENDING -> PlansMessage.PENDING
        PurchaseOutcome.AWAITING_SERVER -> PlansMessage.AWAITING_SERVER
        PurchaseOutcome.REJECTED -> PlansMessage.REJECTED
    }

    private fun BillingError.toMessage() = when (this) {
        BillingError.UNAVAILABLE -> PlansMessage.STORE_UNAVAILABLE
        BillingError.NETWORK -> PlansMessage.NETWORK
        BillingError.ALREADY_OWNED -> PlansMessage.ALREADY_OWNED
        BillingError.PRODUCT_NOT_FOUND, BillingError.UNKNOWN -> PlansMessage.UNKNOWN
    }
}
