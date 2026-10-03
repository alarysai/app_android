package com.alarysai.alarysai.feature.history.domain.model

/** One line of the credit statement. [amount] is negative for usage. */
data class CreditTransaction(
    val id: String,
    val kind: CreditKind,
    val amount: Int,
    val balanceAfter: Int,
    val createdAtMillis: Long?,
)

/** Unknown values become [OTHER] instead of hiding the transaction. */
enum class CreditKind { PURCHASE, USAGE, MONTHLY_GRANT, BONUS, REFUND, OTHER }
