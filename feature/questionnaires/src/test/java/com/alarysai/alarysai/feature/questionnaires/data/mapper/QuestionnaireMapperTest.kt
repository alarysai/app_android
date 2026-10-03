package com.alarysai.alarysai.feature.questionnaires.data.mapper

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.questionnaires.data.model.QuestionnaireDto
import com.alarysai.alarysai.feature.questionnaires.domain.model.Questionnaire
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuestionnaireMapperTest {

    private val publishedDto = QuestionnaireDto(
        title = LocalizedTextDto(pt = "Ética na IA", en = "AI ethics"),
        description = LocalizedTextDto(pt = "Descubra como usar IA com responsabilidade."),
        categoryId = "cat1",
        image = null,
        languages = listOf("pt", "en"),
        order = 1,
        status = "published",
    )

    @Test
    fun `maps a published questionnaire`() {
        assertEquals(
            Questionnaire(
                id = "q1",
                categoryId = "cat1",
                title = LocalizedText("Ética na IA", "AI ethics", null),
                description = LocalizedText("Descubra como usar IA com responsabilidade."),
                imageUrl = null,
                languages = setOf(Language.PT, Language.EN),
                order = 1,
            ),
            publishedDto.toDomainOrNull("q1"),
        )
    }

    @Test
    fun `drafts and unknown status are not shown`() {
        assertNull(publishedDto.copy(status = "draft").toDomainOrNull("q1"))
        assertNull(publishedDto.copy(status = "").toDomainOrNull("q1"))
    }

    @Test
    fun `questionnaire without portuguese title or category is not shown`() {
        assertNull(publishedDto.copy(title = null).toDomainOrNull("q1"))
        assertNull(publishedDto.copy(title = LocalizedTextDto(pt = " ")).toDomainOrNull("q1"))
        assertNull(publishedDto.copy(categoryId = "").toDomainOrNull("q1"))
    }

    @Test
    fun `missing or blank description becomes null`() {
        assertNull(publishedDto.copy(description = null).toDomainOrNull("q1")?.description)
        assertNull(publishedDto.copy(description = LocalizedTextDto(pt = "")).toDomainOrNull("q1")?.description)
    }

    @Test
    fun `unknown language codes are ignored and portuguese is always present`() {
        val dto = publishedDto.copy(languages = listOf("es", "fr", ""))

        assertEquals(setOf(Language.ES, Language.PT), dto.toDomainOrNull("q1")?.languages)
    }

    @Test
    fun `keeps the image url when there is one`() {
        val dto = publishedDto.copy(image = ImageRefDto(path = "p", url = "https://x/capa.png"))

        assertEquals("https://x/capa.png", dto.toDomainOrNull("q1")?.imageUrl)
    }

    @Test
    fun `reads the proposed credit cost, ignoring negative values`() {
        assertNull(publishedDto.toDomainOrNull("q1")?.creditCost)
        assertEquals(4, publishedDto.copy(creditCost = 4).toDomainOrNull("q1")?.creditCost)
        assertNull(publishedDto.copy(creditCost = -1).toDomainOrNull("q1")?.creditCost)
    }
}
