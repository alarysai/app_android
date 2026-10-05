package com.alarysai.alarysai.feature.plans.data.billing

/**
 * Product IDs created in the Play Console (Monetize > Products). They must match exactly; a missing
 * or inactive ID is simply not shown. Names, descriptions and prices are edited in the Play Console,
 * not here. See docs/proposta-pagamentos-play.md.
 */
object BillingCatalog {
    /** Subscriptions, each with a monthly base plan. Shown in this order. */
    val subscriptionIds = listOf("alarys_plano_basico", "alarys_plano_pro")

    /** Consumable credit packs (one-time products). Shown in this order. */
    val creditPackIds = listOf("alarys_creditos_50", "alarys_creditos_200")
}
