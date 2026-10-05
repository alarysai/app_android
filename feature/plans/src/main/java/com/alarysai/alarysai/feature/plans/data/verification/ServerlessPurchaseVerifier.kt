package com.alarysai.alarysai.feature.plans.data.verification

import com.alarysai.alarysai.feature.plans.domain.model.StorePurchase
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseVerifier
import com.alarysai.alarysai.feature.plans.domain.repository.VerificationResult
import javax.inject.Inject

/**
 * Placeholder until the verification server exists (docs/proposta-pagamentos-play.md): every
 * purchase is "server unavailable". Nothing is granted or acknowledged, so the Play Store refunds
 * test purchases automatically after 3 days. Replace the binding in PlansModule with the real one.
 */
class ServerlessPurchaseVerifier @Inject constructor() : PurchaseVerifier {
    override suspend fun verify(purchase: StorePurchase): VerificationResult = VerificationResult.UNAVAILABLE
}
