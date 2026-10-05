package com.alarysai.alarysai.feature.plans.domain.model

/** A purchase as the Play Store reports it. */
data class StorePurchase(
    val productIds: List<String>,
    /** Proof of the purchase, sent to the server to be verified with the Google Play API. */
    val purchaseToken: String,
    val orderId: String?,
    val isSubscription: Boolean,
    /** Pending: paid with a slow method (cash, bank slip); nothing is granted until it clears. */
    val isPending: Boolean,
    /** Acknowledged by the server; unacknowledged purchases are refunded by Google after 3 days. */
    val isAcknowledged: Boolean,
)

/** What came back from the Play Store purchase screen. */
sealed interface PurchaseUpdate {
    data class Purchased(val purchases: List<StorePurchase>) : PurchaseUpdate
    data object Cancelled : PurchaseUpdate
    data class Failed(val error: BillingError) : PurchaseUpdate
}

enum class BillingError {
    /** Play Store missing, outdated or not signed in, or the app was not installed from it. */
    UNAVAILABLE,
    NETWORK,

    /** The user already has this subscription. */
    ALREADY_OWNED,

    /** The product does not exist in the Play Console (or is not active yet). */
    PRODUCT_NOT_FOUND,
    UNKNOWN,
}

class BillingException(val error: BillingError) : Exception("Billing failed: $error")

/** Result of handling one purchase, shown to the user. */
enum class PurchaseOutcome {
    /** The server verified it and granted the plan or the credits. */
    DELIVERED,

    /** Payment not cleared yet (cash/bank slip). */
    PENDING,

    /** Received, but the verification server does not exist yet: nothing granted, not acknowledged. */
    AWAITING_SERVER,

    /** The server refused the purchase (invalid or already used token). */
    REJECTED,
}
