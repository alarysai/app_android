package com.alarysai.alarysai.feature.history.data.mapper

import com.alarysai.alarysai.feature.history.data.model.CreditTransactionDto
import com.alarysai.alarysai.feature.history.data.model.HistoryEntryDto
import com.alarysai.alarysai.feature.history.data.model.HistoryResultDto
import com.alarysai.alarysai.feature.history.data.model.UserCreditsDto
import com.alarysai.alarysai.feature.history.domain.model.CreditKind
import com.alarysai.alarysai.feature.history.domain.model.CreditTransaction
import com.alarysai.alarysai.feature.history.domain.model.HistoryEntry
import com.alarysai.alarysai.feature.history.domain.model.OutputType
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class HistoryMappersTest {

    private val createdAt = Timestamp(Date(1_790_000_000_000))

    @Test
    fun `maps a history entry`() {
        val dto = HistoryEntryDto(
            questionnaireTitle = "Ética na IA",
            outputType = "text",
            result = HistoryResultDto(text = "Um texto curto.", url = "https://storage/x.pdf"),
            creditsSpent = 3,
            createdAt = createdAt,
        )

        assertEquals(
            HistoryEntry("h1", "Ética na IA", OutputType.TEXT, "Um texto curto.", "https://storage/x.pdf", 3, 1_790_000_000_000),
            dto.toDomain("h1"),
        )
    }

    @Test
    fun `odd history values become neutral instead of hiding the entry`() {
        val entry = HistoryEntryDto(
            questionnaireTitle = " ",
            outputType = "podcast",
            result = HistoryResultDto(text = "", url = "http://insecure/x"),
            creditsSpent = -5,
            createdAt = null,
        ).toDomain("h1")

        assertNull(entry.questionnaireTitle)
        assertEquals(OutputType.OTHER, entry.outputType)
        assertNull(entry.resultText)
        assertNull(entry.resultUrl)
        assertEquals(0, entry.creditsSpent)
        assertNull(entry.createdAtMillis)
    }

    @Test
    fun `maps every output type`() {
        val types = listOf("text", "image", "video", "slides").map { HistoryEntryDto(outputType = it).toDomain("h").outputType }

        assertEquals(listOf(OutputType.TEXT, OutputType.IMAGE, OutputType.VIDEO, OutputType.SLIDES), types)
    }

    @Test
    fun `maps a credit transaction and every kind`() {
        assertEquals(
            CreditTransaction("c1", CreditKind.USAGE, -3, 37, 1_790_000_000_000),
            CreditTransactionDto(kind = "usage", amount = -3, balanceAfter = 37, createdAt = createdAt).toDomain("c1"),
        )
        val kinds = listOf("purchase", "usage", "monthly_grant", "bonus", "refund", "gift")
            .map { CreditTransactionDto(kind = it).toDomain("c").kind }
        assertEquals(
            listOf(CreditKind.PURCHASE, CreditKind.USAGE, CreditKind.MONTHLY_GRANT, CreditKind.BONUS, CreditKind.REFUND, CreditKind.OTHER),
            kinds,
        )
    }

    @Test
    fun `balance is zero when never credited or negative`() {
        val missingDocument: UserCreditsDto? = null

        assertEquals(0, missingDocument.toBalance())
        assertEquals(0, UserCreditsDto(creditBalance = null).toBalance())
        assertEquals(0, UserCreditsDto(creditBalance = -2).toBalance())
        assertEquals(40, UserCreditsDto(creditBalance = 40).toBalance())
    }
}
