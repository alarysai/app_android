package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.feature.questionnaires.domain.model.END_OF_QUESTIONNAIRE
import com.alarysai.alarysai.feature.questionnaires.domain.option
import com.alarysai.alarysai.feature.questionnaires.domain.question
import com.alarysai.alarysai.feature.questionnaires.domain.video
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Same cases as the panel's `resolveNext` (android-integration.md section 6). */
class ResolveNextStepUseCaseTest {

    private val resolveNext = ResolveNextStepUseCase()

    @Test
    fun `option jump wins over the step jump and the order`() {
        val chosen = option("a", nextStepId = "s3")
        val step = question("s1", 0, chosen, nextStepId = "s2")
        val ordered = listOf(step, video("s2", 1), video("s3", 2))

        assertEquals("s3", resolveNext(step, chosen, ordered))
    }

    @Test
    fun `step jump is used when the option has none`() {
        val chosen = option("a")
        val step = question("s1", 0, chosen, nextStepId = "s3")
        val ordered = listOf(step, video("s2", 1), video("s3", 2))

        assertEquals("s3", resolveNext(step, chosen, ordered))
    }

    @Test
    fun `without jumps the next step by order follows`() {
        val step = video("s1", 0)
        val ordered = listOf(step, video("s2", 1))

        assertEquals("s2", resolveNext(step, null, ordered))
    }

    @Test
    fun `the last step without jumps ends the questionnaire`() {
        val step = video("s2", 1)

        assertNull(resolveNext(step, null, listOf(video("s1", 0), step)))
    }

    @Test
    fun `end marker ends right away, on the option or on the step`() {
        val endingOption = option("a", nextStepId = END_OF_QUESTIONNAIRE)
        val step = question("s1", 0, endingOption, nextStepId = "s2")
        val ordered = listOf(step, video("s2", 1))

        assertNull(resolveNext(step, endingOption, ordered))
        assertNull(resolveNext(video("s1", 0, nextStepId = END_OF_QUESTIONNAIRE), null, ordered))
    }

    @Test
    fun `option end marker wins even when the step jumps somewhere`() {
        val step = question("s1", 0, option("a", nextStepId = END_OF_QUESTIONNAIRE), nextStepId = "s2")

        assertNull(resolveNext(step, step.options.single(), listOf(step, video("s2", 1))))
    }
}
