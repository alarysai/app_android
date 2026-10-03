package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import com.alarysai.alarysai.feature.questionnaires.domain.model.QuestionnaireProgress
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepAnswer
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepResponse
import com.alarysai.alarysai.feature.questionnaires.domain.option
import com.alarysai.alarysai.feature.questionnaires.domain.question
import com.alarysai.alarysai.feature.questionnaires.domain.video
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvanceQuestionnaireUseCaseTest {

    private val advance = AdvanceQuestionnaireUseCase(ResolveNextStepUseCase())

    private val intro = video("intro", 0)
    private val topic = question("topic", 1, option("ethics", nextStepId = "outro"), option("writing"))
    private val tone = question("tone", 2, option("formal"), option("casual"))
    private val outro = video("outro", 3)
    private val steps = listOf(intro, topic, tone, outro)

    private fun choose(vararg ids: String) = StepResponse(optionIds = ids.toList())
    private val next = StepResponse()

    @Test
    fun `starts on the first step of the sorted list`() {
        assertEquals(QuestionnaireProgress(currentStepId = "intro"), advance.start(steps))
        assertTrue(advance.start(emptyList()).isFinished)
    }

    @Test
    fun `a video continues without an answer`() {
        assertEquals(QuestionnaireProgress("topic", listOf(StepAnswer("intro"))), advance(steps, advance.start(steps), next))
    }

    @Test
    fun `the chosen option of a single choice decides the path`() {
        val onTopic = QuestionnaireProgress("topic", listOf(StepAnswer("intro")))

        assertEquals("outro", advance(steps, onTopic, choose("ethics")).currentStepId)
        assertEquals("tone", advance(steps, onTopic, choose("writing")).currentStepId)
    }

    @Test
    fun `answering the last step finishes and keeps every answer in order`() {
        var progress = advance.start(steps)
        progress = advance(steps, progress, next)
        progress = advance(steps, progress, choose("writing"))
        progress = advance(steps, progress, choose("casual"))
        progress = advance(steps, progress, next)

        assertTrue(progress.isFinished)
        assertEquals(
            listOf(StepAnswer("intro"), StepAnswer("topic", listOf("writing")), StepAnswer("tone", listOf("casual")), StepAnswer("outro")),
            progress.answers,
        )
    }

    @Test
    fun `invalid single choice answers leave the progress unchanged`() {
        val onTopic = QuestionnaireProgress("topic", listOf(StepAnswer("intro")))

        assertSame(onTopic, advance(steps, onTopic, next))
        assertSame(onTopic, advance(steps, onTopic, choose("unknown")))
        assertSame(onTopic, advance(steps, onTopic, choose("ethics", "writing")))
    }

    @Test
    fun `multiple choice needs one or more options, keeps the step order and ignores option jumps`() {
        val things = question("things", 0, option("people", nextStepId = "end"), option("product"), option("city"))
            .copy(answerType = AnswerType.MULTIPLE_CHOICE)
        val flow = listOf(things, video("scene", 1), video("end", 2))
        val start = advance.start(flow)

        assertSame(start, advance(flow, start, next))
        val answered = advance(flow, start, choose("city", "people"))
        assertEquals(listOf("people", "city"), answered.answers.single().optionIds)
        assertEquals("scene", answered.currentStepId)
    }

    @Test
    fun `open text keeps the trimmed text, rejects blank when required and text over the limit`() {
        val scene = question("scene", 0).copy(answerType = AnswerType.OPEN_TEXT, maxLength = 10)
        val flow = listOf(scene, video("end", 1))
        val start = advance.start(flow)

        assertSame(start, advance(flow, start, StepResponse(text = "   ")))
        assertSame(start, advance(flow, start, StepResponse(text = "texto longo demais")))
        assertEquals(StepAnswer("scene", text = "Café"), advance(flow, start, StepResponse(text = "  Café ")).answers.single())
    }

    @Test
    fun `optional open text can be left blank`() {
        val scene = question("scene", 0).copy(answerType = AnswerType.OPEN_TEXT, required = false)

        assertEquals(StepAnswer("scene"), advance(listOf(scene), advance.start(listOf(scene)), StepResponse(text = "")).answers.single())
    }

    @Test
    fun `yes or no follows the chosen option jump`() {
        val real = question("real", 0, option("yes", nextStepId = "warning"), option("no")).copy(answerType = AnswerType.YES_NO)
        val flow = listOf(real, video("review", 1), video("warning", 2))

        assertEquals("warning", advance(flow, advance.start(flow), choose("yes")).currentStepId)
        assertEquals("review", advance(flow, advance.start(flow), choose("no")).currentStepId)
    }

    @Test
    fun `only optional questions can be skipped, and skipping ignores option jumps`() {
        val optional = question("style", 0, option("photo", nextStepId = "end")).copy(required = false)
        val flow = listOf(optional, video("next", 1), video("end", 2))
        val start = advance.start(flow)

        val skipped = advance(flow, start, StepResponse(skipped = true))
        assertEquals(StepAnswer("style"), skipped.answers.single())
        assertEquals("next", skipped.currentStepId)

        // "topic" is required (the default): skipping is refused.
        val onTopic = advance(steps, advance.start(steps), next)
        assertSame(onTopic, advance(steps, onTopic, StepResponse(skipped = true)))
    }

    @Test
    fun `a question left without options continues like a video`() {
        val bare = question("bare", 0)
        val flow = listOf(bare, outro)

        assertEquals("outro", advance(flow, advance.start(flow), next).currentStepId)
    }

    @Test
    fun `missing jumps, cycles and the step limit end the questionnaire`() {
        val missing = listOf(video("a", 0, nextStepId = "deleted"), video("b", 1))
        assertTrue(advance(missing, advance.start(missing), next).isFinished)

        val cycle = listOf(video("a", 0), video("b", 1, nextStepId = "a"))
        val looped = advance(cycle, advance(cycle, advance.start(cycle), next), next)
        assertTrue(looped.isFinished)

        val long = (0..AdvanceQuestionnaireUseCase.MAX_STEPS + 10).map { video("s$it", it) }
        var progress = advance.start(long)
        while (!progress.isFinished) progress = advance(long, progress, next)
        assertEquals(AdvanceQuestionnaireUseCase.MAX_STEPS, progress.answers.size)
    }

    @Test
    fun `counts the steps ahead following the selected option`() {
        val onTopic = QuestionnaireProgress("topic", listOf(StepAnswer("intro")))

        assertEquals(3, advance.countRemaining(steps, onTopic, draftOptionId = null))
        assertEquals(2, advance.countRemaining(steps, onTopic, draftOptionId = "ethics"))
        assertEquals(0, advance.countRemaining(steps, QuestionnaireProgress(null), draftOptionId = null))
    }

    @Test
    fun `back undoes the last answer and reopen undoes that answer and the later ones`() {
        val onOutro = QuestionnaireProgress(
            "outro",
            listOf(StepAnswer("intro"), StepAnswer("topic", listOf("writing")), StepAnswer("tone", listOf("casual"))),
        )

        assertEquals(QuestionnaireProgress("tone", onOutro.answers.take(2)), onOutro.back())
        assertEquals(QuestionnaireProgress("topic", listOf(StepAnswer("intro"))), onOutro.reopen("topic"))
        assertNull(onOutro.reopen("never-answered"))
        assertNull(advance.start(steps).back())
    }
}
