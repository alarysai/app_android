package com.alarysai.alarysai.feature.plans.domain

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.alarysai.alarysai.feature.plans.data.billing.toBillingError
import com.alarysai.alarysai.feature.plans.domain.model.BillingError
import com.alarysai.alarysai.feature.plans.domain.model.BillingPeriod
import com.alarysai.alarysai.feature.plans.domain.model.PurchaseOutcome
import com.alarysai.alarysai.feature.plans.domain.model.StorePurchase
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseVerifier
import com.alarysai.alarysai.feature.plans.domain.repository.VerificationResult
import com.alarysai.alarysai.feature.plans.domain.usecase.ProcessPurchaseUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BillingDomainTest {

    private val purchase = StorePurchase(listOf("alarys_creditos_50"), "token", "GPA.1", isSubscription = false, isPending = false, isAcknowledged = false)

    private class FixedVerifier(private val result: VerificationResult) : PurchaseVerifier {
        val verified = mutableListOf<String>()
        override suspend fun verify(purchase: StorePurchase): VerificationResult {
            verified += purchase.purchaseToken
            return result
        }
    }

    @Test
    fun `reads the Play Store billing periods`() {
        assertEquals(BillingPeriod.MONTH, BillingPeriod.fromIso("P1M"))
        assertEquals(BillingPeriod.YEAR, BillingPeriod.fromIso("p1y"))
        assertEquals(BillingPeriod.WEEK, BillingPeriod.fromIso("P1W"))
        assertEquals(BillingPeriod.QUARTER, BillingPeriod.fromIso("P3M"))
        assertEquals(BillingPeriod.SEMESTER, BillingPeriod.fromIso("P6M"))
        assertEquals(BillingPeriod.OTHER, BillingPeriod.fromIso("P2M"))
        assertEquals(BillingPeriod.OTHER, BillingPeriod.fromIso(null))
    }

    @Test
    fun `maps Play Billing response codes`() {
        assertEquals(BillingError.UNAVAILABLE, BillingResponseCode.BILLING_UNAVAILABLE.toBillingError())
        assertEquals(BillingError.UNAVAILABLE, BillingResponseCode.SERVICE_DISCONNECTED.toBillingError())
        assertEquals(BillingError.NETWORK, BillingResponseCode.NETWORK_ERROR.toBillingError())
        assertEquals(BillingError.ALREADY_OWNED, BillingResponseCode.ITEM_ALREADY_OWNED.toBillingError())
        assertEquals(BillingError.PRODUCT_NOT_FOUND, BillingResponseCode.ITEM_UNAVAILABLE.toBillingError())
        assertEquals(BillingError.UNKNOWN, BillingResponseCode.ERROR.toBillingError())
    }

    @Test
    fun `new purchases go to the server and the outcome follows its answer`() = runTest {
        assertEquals(PurchaseOutcome.DELIVERED, ProcessPurchaseUseCase(FixedVerifier(VerificationResult.GRANTED))(purchase))
        assertEquals(PurchaseOutcome.REJECTED, ProcessPurchaseUseCase(FixedVerifier(VerificationResult.REJECTED))(purchase))
        assertEquals(PurchaseOutcome.AWAITING_SERVER, ProcessPurchaseUseCase(FixedVerifier(VerificationResult.UNAVAILABLE))(purchase))
    }

    @Test
    fun `pending and already delivered purchases are not sent to the server`() = runTest {
        val verifier = FixedVerifier(VerificationResult.GRANTED)
        val process = ProcessPurchaseUseCase(verifier)

        assertEquals(PurchaseOutcome.PENDING, process(purchase.copy(isPending = true)))
        assertEquals(PurchaseOutcome.DELIVERED, process(purchase.copy(isAcknowledged = true)))
        assertEquals(emptyList<String>(), verifier.verified)
    }
}
