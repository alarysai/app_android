package com.alarysai.alarysai.feature.plans.domain.repository

import com.alarysai.alarysai.feature.plans.domain.model.Catalog
import com.alarysai.alarysai.feature.plans.domain.model.PurchaseUpdate
import com.alarysai.alarysai.feature.plans.domain.model.StorePurchase
import kotlinx.coroutines.flow.Flow

/** The screen that hosts the Play Store purchase sheet (an Activity, wrapped by the data layer). */
interface PurchaseHost

interface BillingRepository {

    /** Plans and credit packs from the Play Console. Fails with `BillingException`. */
    suspend fun loadCatalog(): Catalog

    /** Purchases the user still owns (active subscriptions, unconsumed packs). Fails with `BillingException`. */
    suspend fun ownedPurchases(): List<StorePurchase>

    /**
     * Opens the Play Store purchase sheet. [offerToken] is required for subscriptions. The result
     * arrives through [purchaseUpdates]. Fails with `BillingException` when the sheet cannot open.
     */
    suspend fun launchPurchase(host: PurchaseHost, productId: String, offerToken: String?)

    fun purchaseUpdates(): Flow<PurchaseUpdate>
}
