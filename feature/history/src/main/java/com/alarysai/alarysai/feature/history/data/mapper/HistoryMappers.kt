package com.alarysai.alarysai.feature.history.data.mapper

import com.alarysai.alarysai.feature.history.data.model.CreditTransactionDto
import com.alarysai.alarysai.feature.history.data.model.HistoryEntryDto
import com.alarysai.alarysai.feature.history.data.model.UserCreditsDto
import com.alarysai.alarysai.feature.history.domain.model.CreditKind
import com.alarysai.alarysai.feature.history.domain.model.CreditTransaction
import com.alarysai.alarysai.feature.history.domain.model.HistoryEntry
import com.alarysai.alarysai.feature.history.domain.model.OutputType

/** Server-written data: nothing is dropped, odd values become neutral ones. */
fun HistoryEntryDto.toDomain(id: String) = HistoryEntry(
    id = id,
    questionnaireTitle = questionnaireTitle?.trim()?.takeIf { it.isNotEmpty() },
    outputType = when (outputType) {
        "text" -> OutputType.TEXT
        "image" -> OutputType.IMAGE
        "video" -> OutputType.VIDEO
        "slides" -> OutputType.SLIDES
        else -> OutputType.OTHER
    },
    resultText = result?.text?.trim()?.takeIf { it.isNotEmpty() },
    resultUrl = result?.url?.trim()?.takeIf { it.startsWith("https://") },
    creditsSpent = creditsSpent.toSafeInt().coerceAtLeast(0),
    createdAtMillis = createdAt?.toDate()?.time,
)

fun CreditTransactionDto.toDomain(id: String) = CreditTransaction(
    id = id,
    kind = when (kind) {
        "purchase" -> CreditKind.PURCHASE
        "usage" -> CreditKind.USAGE
        "monthly_grant" -> CreditKind.MONTHLY_GRANT
        "bonus" -> CreditKind.BONUS
        "refund" -> CreditKind.REFUND
        else -> CreditKind.OTHER
    },
    amount = amount.toSafeInt(),
    balanceAfter = balanceAfter.toSafeInt(),
    createdAtMillis = createdAt?.toDate()?.time,
)

/** Missing document or field (never credited) and negative values count as 0. */
fun UserCreditsDto?.toBalance(): Int = (this?.creditBalance ?: 0L).toSafeInt().coerceAtLeast(0)

private fun Long.toSafeInt(): Int = coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
