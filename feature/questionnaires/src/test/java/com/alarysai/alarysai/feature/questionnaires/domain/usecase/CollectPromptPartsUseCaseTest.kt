package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.domain.model.PromptPart
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepAnswer
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepOption
import com.alarysai.alarysai.feature.questionnaires.domain.question
import com.alarysai.alarysai.feature.questionnaires.domain.video
import org.junit.Assert.assertEquals
import org.junit.Test

/** Same cases as the panel's `promptParts` tests (prompt-preview.test.ts), plus the proposed answer types. */
class CollectPromptPartsUseCaseTest {

    private val collect = CollectPromptPartsUseCase()

    private val intro = video("intro", 1).copy(promptInstruction = "ignore")
    private val ethics = StepOption("etica", LocalizedText(pt = "Ética", en = "Ethics"), null, "foque em ética", null)
    private val other = StepOption("outro", LocalizedText(pt = "Outro"), null, null, null)
    private val theme = question("tema", 2, ethics, other)
        .copy(partOfPrompt = true, promptInstruction = "Escreva um texto curto")
    private val steps = listOf(intro, theme)

    @Test
    fun `collects answers and instructions only from steps marked as part of the prompt`() {
        val parts = collect(listOf(StepAnswer("intro"), StepAnswer("tema", listOf("etica"))), steps, Language.EN)

        assertEquals(listOf(PromptPart("tema", "Ethics", listOf("Escreva um texto curto", "foque em ética"))), parts)
    }

    @Test
    fun `falls back to portuguese and skips empty instructions`() {
        val parts = collect(listOf(StepAnswer("tema", listOf("outro"))), steps, Language.ES)

        assertEquals(listOf(PromptPart("tema", "Outro", listOf("Escreva um texto curto"))), parts)
    }

    @Test
    fun `ignores steps that no longer exist`() {
        assertEquals(emptyList<PromptPart>(), collect(listOf(StepAnswer("gone")), steps, Language.PT))
    }

    @Test
    fun `multiple choice joins the options and every option instruction`() {
        val things = theme.copy(answerType = AnswerType.MULTIPLE_CHOICE)

        assertEquals(
            listOf(PromptPart("tema", "Ética, Outro", listOf("Escreva um texto curto", "foque em ética"))),
            collect(listOf(StepAnswer("tema", listOf("etica", "outro"))), listOf(things), Language.PT),
        )
    }

    @Test
    fun `open text goes as written and a skipped question has no answer`() {
        val scene = question("cena", 0).copy(answerType = AnswerType.OPEN_TEXT, partOfPrompt = true, promptInstruction = "Cena:")

        assertEquals(
            listOf(PromptPart("cena", "Uma barista sorrindo", listOf("Cena:"))),
            collect(listOf(StepAnswer("cena", text = "Uma barista sorrindo")), listOf(scene), Language.EN),
        )
        assertEquals(
            listOf(PromptPart("cena", answer = null, instructions = listOf("Cena:"))),
            collect(listOf(StepAnswer("cena")), listOf(scene), Language.PT),
        )
    }

    @Test
    fun `keeps the order in which the steps were answered`() {
        val first = question("b", 2, other).copy(partOfPrompt = true)
        val second = question("a", 1, ethics).copy(partOfPrompt = true)

        val parts = collect(listOf(StepAnswer("b", listOf("outro")), StepAnswer("a", listOf("etica"))), listOf(second, first), Language.PT)

        assertEquals(listOf("b", "a"), parts.map { it.stepId })
    }
}
