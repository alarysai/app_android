package com.alarysai.alarysai.feature.plans.domain.model

/**
 * What the Play Store sells, as configured in the Play Console: monthly/yearly plans
 * (subscriptions) and one-off credit packs (consumable products). Names, descriptions and
 * prices come from the Play Console, already localized and in the user's currency.
 */
data class Catalog(
    val plans: List<SubscriptionPlan>,
    val creditPacks: List<CreditPack>,
) {
    val isEmpty: Boolean get() = plans.isEmpty() && creditPacks.isEmpty()
}

data class SubscriptionPlan(
    val productId: String,
    val name: String,
    val description: String,
    /** Offer used to subscribe (the base plan or a promotion), with its recurring price. */
    val offer: PlanOffer,
)

data class PlanOffer(
    /** Opaque token that tells the Play Store which base plan/offer to buy. */
    val offerToken: String,
    val formattedPrice: String,
    val billingPeriod: BillingPeriod,
)

/** From the ISO 8601 period of the Play Store ("P1M"); [OTHER] for anything else. */
enum class BillingPeriod {
    WEEK, MONTH, QUARTER, SEMESTER, YEAR, OTHER;

    companion object {
        fun fromIso(period: String?): BillingPeriod = when (period?.uppercase()) {
            "P1W", "P7D" -> WEEK
            "P1M", "P4W" -> MONTH
            "P3M" -> QUARTER
            "P6M" -> SEMESTER
            "P1Y", "P12M" -> YEAR
            else -> OTHER
        }
    }
}

data class CreditPack(
    val productId: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
)
