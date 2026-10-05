package com.alarysai.alarysai.feature.plans.di

import com.alarysai.alarysai.feature.plans.BuildConfig
import com.alarysai.alarysai.feature.plans.data.billing.PlayBillingRepository
import com.alarysai.alarysai.feature.plans.data.verification.ServerlessPurchaseVerifier
import com.alarysai.alarysai.feature.plans.domain.repository.BillingRepository
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseVerifier
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
abstract class PlansModule {

    @Binds
    abstract fun bindsBillingRepository(impl: PlayBillingRepository): BillingRepository

    /** Swap for the server implementation once it exists. */
    @Binds
    abstract fun bindsPurchaseVerifier(impl: ServerlessPurchaseVerifier): PurchaseVerifier

    companion object {
        const val PURCHASES_ENABLED = "purchasesEnabled"

        /** Debug: on (Play license testers). Release: off until the verification server exists. */
        @Provides
        @Named(PURCHASES_ENABLED)
        fun providesPurchasesEnabled(): Boolean = BuildConfig.PURCHASES_ENABLED
    }
}
