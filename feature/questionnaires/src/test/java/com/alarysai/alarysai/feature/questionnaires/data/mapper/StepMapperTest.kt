package com.alarysai.alarysai.feature.questionnaires.data.mapper

import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.questionnaires.data.model.InfoFlagDto
import com.alarysai.alarysai.feature.questionnaires.data.model.StepDto
import com.alarysai.alarysai.feature.questionnaires.data.model.StepOptionDto
import com.alarysai.alarysai.feature.questionnaires.domain.model.InfoFlag
import com.alarysai.alarysai.feature.questionnaires.domain.model.Step
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepOption
import com.alarysai.alarysai.feature.questionnaires.domain.model.StepType
import com.alarysai.alarysai.feature.questionnaires.domain.model.AnswerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StepMapperTest {

    private val questionDto = StepDto(
        order = 2,
        type = "question",
        text = LocalizedTextDto(pt = "Qual tema você quer explorar?", en = "Which topic?"),
        options = listOf(
            StepOptionDto(id = "a1", text = LocalizedTextDto(pt = "Ética"), promptInstruction = "Foque nos dilemas.", nextStepId = "__end__"),
            StepOptionDto(id = "b2", text = LocalizedTextDto(pt = "Redação")),
        ),
        nextStepId = null,
        partOfPrompt = true,
        promptInstruction = "Escreva um texto curto.",
    )

    private val videoDto = StepDto(
        order = 1,
        type = "video",
        text = LocalizedTextDto(pt = "Assista à introdução."),
        videoUrl = "https://www.youtube.com/watch?v=XXXX",
    )

    @Test
    fun `maps a question with its options`() {
        assertEquals(
            Step(
                id = "s2",
                order = 2,
                type = StepType.QUESTION,
                text = LocalizedText("Qual tema você quer explorar?", "Which topic?", null),
                imageUrl = null,
                videoUrl = null,
                options = listOf(
                    StepOption("a1", LocalizedText("Ética"), null, "Foque nos dilemas.", "__end__"),
                    StepOption("b2", LocalizedText("Redação"), null, null, null),
                ),
                nextStepId = null,
                partOfPrompt = true,
                promptInstruction = "Escreva um texto curto.",
            ),
            questionDto.toDomainOrNull("s2"),
        )
    }

    @Test
    fun `maps a video with its link and no options`() {
        val step = videoDto.copy(options = listOf(StepOptionDto(id = "x", text = LocalizedTextDto(pt = "x"))))
            .toDomainOrNull("s1")

        assertEquals(StepType.VIDEO, step?.type)
        assertEquals("https://www.youtube.com/watch?v=XXXX", step?.videoUrl)
        assertEquals(emptyList<StepOption>(), step?.options)
    }

    @Test
    fun `unknown type or a step without text and image is dropped`() {
        assertNull(questionDto.copy(type = "quiz").toDomainOrNull("s"))
        assertNull(questionDto.copy(text = null, image = null).toDomainOrNull("s"))
        assertNull(questionDto.copy(text = LocalizedTextDto(pt = " "), image = ImageRefDto(url = "")).toDomainOrNull("s"))
    }

    @Test
    fun `a step with only an image is kept`() {
        val step = questionDto.copy(text = null, image = ImageRefDto(path = "p", url = "https://x/img.png"))
            .toDomainOrNull("s")

        assertNull(step?.text)
        assertEquals("https://x/img.png", step?.imageUrl)
    }

    @Test
    fun `a video without an https link is dropped`() {
        assertNull(videoDto.copy(videoUrl = null).toDomainOrNull("s"))
        assertNull(videoDto.copy(videoUrl = "http://insecure.example/v").toDomainOrNull("s"))
        assertNull(videoDto.copy(videoUrl = "javascript:alert(1)").toDomainOrNull("s"))
    }

    @Test
    fun `invalid and repeated options are dropped`() {
        val dto = questionDto.copy(
            options = listOf(
                StepOptionDto(id = "", text = LocalizedTextDto(pt = "Sem ID")),
                StepOptionDto(id = "c3", text = null, image = null),
                StepOptionDto(id = "a1", text = LocalizedTextDto(pt = "Primeira")),
                StepOptionDto(id = "a1", text = LocalizedTextDto(pt = "Repetida")),
            ),
        )

        assertEquals(listOf("Primeira"), dto.toDomainOrNull("s")?.options?.map { it.text?.pt })
    }

    @Test
    fun `blank jumps and instructions become null`() {
        val step = questionDto.copy(nextStepId = "  ", promptInstruction = "").toDomainOrNull("s")

        assertNull(step?.nextStepId)
        assertNull(step?.promptInstruction)
    }

    @Test
    fun `maps the informative yes or no with its tip`() {
        val dto = questionDto.copy(
            infoFlag = InfoFlagDto(label = LocalizedTextDto(pt = "Isso é ético?", en = "Is this ethical?"), value = true, tipId = "tip1"),
        )

        assertEquals(
            InfoFlag(LocalizedText("Isso é ético?", "Is this ethical?", null), value = true, tipId = "tip1"),
            dto.toDomainOrNull("s")?.infoFlag,
        )
    }

    @Test
    fun `info flag without a label is dropped but the step stays, and a blank tip id is no tip`() {
        val withoutLabel = questionDto.copy(infoFlag = InfoFlagDto(label = null, value = true, tipId = "tip1"))
        val blankTip = questionDto.copy(infoFlag = InfoFlagDto(label = LocalizedTextDto(pt = "Ético?"), value = false, tipId = " "))

        assertEquals("s", withoutLabel.toDomainOrNull("s")?.id)
        assertNull(withoutLabel.toDomainOrNull("s")?.infoFlag)
        assertEquals(InfoFlag(LocalizedText("Ético?"), value = false, tipId = null), blankTip.toDomainOrNull("s")?.infoFlag)
    }

    @Test
    fun `without the proposed fields a question keeps the original behavior`() {
        val step = questionDto.toDomainOrNull("s")

        assertEquals(AnswerType.SINGLE_CHOICE, step?.answerType)
        assertEquals(true, step?.required)
        assertEquals(Step.DEFAULT_MAX_LENGTH, step?.maxLength)
        assertNull(step?.helpText)
        assertNull(step?.placeholder)
    }

    @Test
    fun `maps the proposed answer types and fields`() {
        val multiple = questionDto.copy(answerType = "multiple_choice", helpText = LocalizedTextDto(pt = "Escolha quantos quiser."), required = false)
            .toDomainOrNull("s")
        assertEquals(AnswerType.MULTIPLE_CHOICE, multiple?.answerType)
        assertEquals("Escolha quantos quiser.", multiple?.helpText?.pt)
        assertEquals(false, multiple?.required)

        val open = questionDto.copy(answerType = "open_text", maxLength = 300, placeholder = LocalizedTextDto(pt = "Ex.: uma barista"))
            .toDomainOrNull("s")
        assertEquals(AnswerType.OPEN_TEXT, open?.answerType)
        assertEquals(emptyList<StepOption>(), open?.options)
        assertEquals(300, open?.maxLength)
        assertEquals("Ex.: uma barista", open?.placeholder?.pt)
    }

    @Test
    fun `yes or no needs exactly two options, unknown types fall back to single choice`() {
        assertEquals(AnswerType.YES_NO, questionDto.copy(answerType = "yes_no").toDomainOrNull("s")?.answerType)
        val threeOptions = questionDto.copy(
            answerType = "yes_no",
            options = questionDto.options + StepOptionDto(id = "c3", text = LocalizedTextDto(pt = "Talvez")),
        )
        assertEquals(AnswerType.SINGLE_CHOICE, threeOptions.toDomainOrNull("s")?.answerType)
        assertEquals(AnswerType.SINGLE_CHOICE, questionDto.copy(answerType = "slider").toDomainOrNull("s")?.answerType)
    }

    @Test
    fun `text limit is kept within bounds and option tips are read`() {
        assertEquals(1, questionDto.copy(maxLength = 0).toDomainOrNull("s")?.maxLength)
        assertEquals(5_000, questionDto.copy(maxLength = 99_999).toDomainOrNull("s")?.maxLength)

        val withTip = questionDto.copy(options = listOf(StepOptionDto(id = "sim", text = LocalizedTextDto(pt = "Sim"), tipId = "etica")))
        assertEquals("etica", withTip.toDomainOrNull("s")?.options?.single()?.tipId)
    }
}
