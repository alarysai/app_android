package com.alarysai.alarysai.feature.plans.presentation

import com.alarysai.alarysai.feature.plans.domain.model.BillingError
import com.alarysai.alarysai.feature.plans.domain.model.BillingPeriod
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseHost

data class PlansUiState(
    val content: PlansContent = PlansContent.Loading,
    /** False in release until the verification server exists: buttons read "Coming soon". */
    val purchasesEnabled: Boolean,
    /** Product whose purchase sheet is opening; its button shows a spinner. */
    val purchasingProductId: String? = null,
)

sealed interface PlansContent {
    data object Loading : PlansContent

    /** No Play Store (or the app was not installed from it): nothing can be bought here. */
    data object StoreUnavailable : PlansContent

    /** The Play Console has no active product with the expected IDs yet. */
    data object Empty : PlansContent

    data class Error(val error: BillingError) : PlansContent

    data class Ready(
        val plans: List<PlanUi>,
        val creditPacks: List<CreditPackUi>,
    ) : PlansContent
}

data class PlanUi(
    val productId: String,
    val name: String,
    val description: String,
    val price: String,
    val period: BillingPeriod,
    val offerToken: String,
    /** The user already has this subscription. */
    val isCurrent: Boolean,
)

data class CreditPackUi(
    val productId: String,
    val name: String,
    val description: String,
    val price: String,
)

sealed interface PlansUiAction {
    data class SubscribeClicked(val plan: PlanUi, val host: PurchaseHost) : PlansUiAction
    data class BuyCreditsClicked(val pack: CreditPackUi, val host: PurchaseHost) : PlansUiAction
    data object ManageSubscriptionsClicked : PlansUiAction
    data object Retry : PlansUiAction
}

sealed interface PlansUiEvent {
    data class ShowMessage(val message: PlansMessage) : PlansUiEvent

    /** Google Play's subscription management page (cancel, change payment method). */
    data object OpenManageSubscriptions : PlansUiEvent
}

/** Feedback after a purchase attempt. */
enum class PlansMessage {
    DELIVERED,
    PENDING,

    /** Received, but credits wait for the verification server (nothing charged for good). */
    AWAITING_SERVER,
    REJECTED,
    ALREADY_OWNED,
    STORE_UNAVAILABLE,
    NETWORK,
    UNKNOWN,
}
