package com.alarysai.alarysai.feature.plans.data.billing

import android.content.Context
import com.alarysai.alarysai.core.common.session.SessionRepository
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Google Play Billing (library 7). Connects on demand, reads the catalog and the owned purchases,
 * and opens the purchase sheet. It never acknowledges or consumes: that is the server's job after
 * it verifies the purchase (see `PurchaseVerifier`).
 */
@Singleton
class PlayBillingRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val sessionRepository: SessionRepository,
) : BillingRepository {

    private val updates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = 8)

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(PurchasesUpdatedListener { result, purchases -> updates.tryEmit(result.toUpdate(purchases)) })
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    /** Last product details read, needed to open the purchase sheet. */
    private val productDetails = mutableMapOf<String, ProductDetails>()

    override fun purchaseUpdates(): Flow<PurchaseUpdate> = updates.asSharedFlow()

    override suspend fun loadCatalog(): Catalog {
        connect()
        val subscriptions = queryDetails(ProductType.SUBS, BillingCatalog.subscriptionIds)
        val packs = queryDetails(ProductType.INAPP, BillingCatalog.creditPackIds)
        return Catalog(
            plans = BillingCatalog.subscriptionIds.mapNotNull { id -> subscriptions[id]?.toPlan() },
            creditPacks = BillingCatalog.creditPackIds.mapNotNull { id -> packs[id]?.toCreditPack() },
        )
    }

    override suspend fun ownedPurchases(): List<StorePurchase> {
        connect()
        return listOf(ProductType.SUBS, ProductType.INAPP).flatMap { type ->
            val result = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(type).build())
            result.billingResult.throwIfFailed()
            result.purchasesList.map { it.toStorePurchase(isSubscription = type == ProductType.SUBS) }
        }
    }

    override suspend fun launchPurchase(host: PurchaseHost, productId: String, offerToken: String?) {
        val activity = (host as? ActivityPurchaseHost)?.activity ?: throw BillingException(BillingError.UNKNOWN)
        connect()
        val details = productDetails[productId] ?: throw BillingException(BillingError.PRODUCT_NOT_FOUND)
        val product = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply { offerToken?.let(::setOfferToken) }
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(product))
            .apply { obfuscatedAccountId()?.let(::setObfuscatedAccountId) }
            .build()
        val result = withContext(Dispatchers.Main) { client.launchBillingFlow(activity, params) }
        result.throwIfFailed()
    }

    private suspend fun connect() {
        if (client.isReady) return
        val result = suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (continuation.isActive) continuation.resume(result)
                }

                // The next call reconnects.
                override fun onBillingServiceDisconnected() = Unit
            })
        }
        result.throwIfFailed()
    }

    private suspend fun queryDetails(type: String, ids: List<String>): Map<String, ProductDetails> {
        if (ids.isEmpty()) return emptyMap()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(ids.map { QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(type).build() })
            .build()
        val result = client.queryProductDetails(params)
        result.billingResult.throwIfFailed()
        val details = result.productDetailsList.orEmpty().associateBy { it.productId }
        productDetails.putAll(details)
        return details
    }

    /**
     * Links the purchase to the account without exposing the Firebase UID: SHA-256 of the UID
     * (64 hex characters, the Play Store limit). The server recomputes it to match the buyer.
     */
    private suspend fun obfuscatedAccountId(): String? {
        val uid = sessionRepository.observeUserId().first() ?: return null
        return MessageDigest.getInstance("SHA-256").digest(uid.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}

/** The base plan (no promotional offer) with its recurring price, i.e. the last pricing phase. */
private fun ProductDetails.toPlan(): SubscriptionPlan? {
    val offers = subscriptionOfferDetails.orEmpty()
    val offer = offers.firstOrNull { it.offerId == null } ?: offers.firstOrNull() ?: return null
    val recurring = offer.pricingPhases.pricingPhaseList.lastOrNull() ?: return null
    return SubscriptionPlan(
        productId = productId,
        name = name,
        description = description,
        offer = PlanOffer(offer.offerToken, recurring.formattedPrice, BillingPeriod.fromIso(recurring.billingPeriod)),
    )
}

private fun ProductDetails.toCreditPack(): CreditPack? {
    val price = oneTimePurchaseOfferDetails?.formattedPrice ?: return null
    return CreditPack(productId = productId, name = name, description = description, formattedPrice = price)
}

private fun Purchase.toStorePurchase(isSubscription: Boolean) = StorePurchase(
    productIds = products,
    purchaseToken = purchaseToken,
    orderId = orderId,
    isSubscription = isSubscription,
    isPending = purchaseState == Purchase.PurchaseState.PENDING,
    isAcknowledged = isAcknowledged,
)

private fun BillingResult.toUpdate(purchases: List<Purchase>?): PurchaseUpdate = when (responseCode) {
    BillingResponseCode.OK -> PurchaseUpdate.Purchased(
        purchases.orEmpty().map { purchase ->
            // The purchase does not say its type; subscriptions are the IDs listed as such.
            purchase.toStorePurchase(isSubscription = purchase.products.any { it in BillingCatalog.subscriptionIds })
        },
    )
    BillingResponseCode.USER_CANCELED -> PurchaseUpdate.Cancelled
    else -> PurchaseUpdate.Failed(responseCode.toBillingError())
}

private fun BillingResult.throwIfFailed() {
    if (responseCode != BillingResponseCode.OK) throw BillingException(responseCode.toBillingError())
}

internal fun Int.toBillingError(): BillingError = when (this) {
    BillingResponseCode.BILLING_UNAVAILABLE,
    BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingResponseCode.SERVICE_DISCONNECTED,
    BillingResponseCode.FEATURE_NOT_SUPPORTED,
    -> BillingError.UNAVAILABLE
    BillingResponseCode.NETWORK_ERROR -> BillingError.NETWORK
    BillingResponseCode.ITEM_ALREADY_OWNED -> BillingError.ALREADY_OWNED
    BillingResponseCode.ITEM_UNAVAILABLE -> BillingError.PRODUCT_NOT_FOUND
    else -> BillingError.UNKNOWN
}
