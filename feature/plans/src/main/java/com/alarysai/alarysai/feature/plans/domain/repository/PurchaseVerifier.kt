package com.alarysai.alarysai.feature.plans.domain.repository

import com.alarysai.alarysai.feature.plans.domain.model.StorePurchase

enum class VerificationResult {
    /** Verified with the Google Play API; plan or credits granted and the purchase acknowledged. */
    GRANTED,
    REJECTED,

    /** The verification server is not reachable or does not exist yet. */
    UNAVAILABLE,
}

/**
 * Sends a purchase to the server, which verifies it with the Google Play Developer API, grants
 * credits (`users/{uid}/credits`, `creditBalance`) and acknowledges/consumes it. The app never
 * grants credits itself: the Firestore rules only let the server write them.
 */
interface PurchaseVerifier {
    suspend fun verify(purchase: StorePurchase): VerificationResult
}
