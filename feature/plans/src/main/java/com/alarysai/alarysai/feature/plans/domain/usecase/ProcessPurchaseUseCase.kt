package com.alarysai.alarysai.feature.plans.domain.usecase

import com.alarysai.alarysai.feature.plans.domain.model.PurchaseOutcome
import com.alarysai.alarysai.feature.plans.domain.model.StorePurchase
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseVerifier
import com.alarysai.alarysai.feature.plans.domain.repository.VerificationResult
import javax.inject.Inject

/**
 * Decides what a new purchase means for the user. Pending payments wait; everything else goes to
 * the server. Already acknowledged purchases were delivered before and are not sent again.
 */
class ProcessPurchaseUseCase @Inject constructor(
    private val verifier: PurchaseVerifier,
) {
    suspend operator fun invoke(purchase: StorePurchase): PurchaseOutcome = when {
        purchase.isPending -> PurchaseOutcome.PENDING
        purchase.isAcknowledged -> PurchaseOutcome.DELIVERED
        else -> when (verifier.verify(purchase)) {
            VerificationResult.GRANTED -> PurchaseOutcome.DELIVERED
            VerificationResult.REJECTED -> PurchaseOutcome.REJECTED
            VerificationResult.UNAVAILABLE -> PurchaseOutcome.AWAITING_SERVER
        }
    }
}
