package com.alarysai.alarysai.feature.history.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/** `users/{uid}/credits/{transactionId}`. `historyEntryId` and `purchaseRef` are not shown. */
@IgnoreExtraProperties
data class CreditTransactionDto(
    val kind: String = "",
    val amount: Long = 0,
    val balanceAfter: Long = 0,
    val createdAt: Timestamp? = null,
)

/** The only field of `users/{uid}` this feature reads. */
@IgnoreExtraProperties
data class UserCreditsDto(
    val creditBalance: Long? = null,
)
