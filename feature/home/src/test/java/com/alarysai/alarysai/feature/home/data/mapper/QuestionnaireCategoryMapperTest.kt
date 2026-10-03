package com.alarysai.alarysai.feature.home.data.mapper

import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.home.data.model.QuestionnaireCategoryDto
import com.alarysai.alarysai.feature.home.domain.model.QuestionnaireCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuestionnaireCategoryMapperTest {

    private val activeDto = QuestionnaireCategoryDto(
        name = LocalizedTextDto(pt = "Imagem", en = "Image", es = "Imagen"),
        icon = null,
        order = 2,
        status = "active",
    )

    @Test
    fun `maps an active category`() {
        assertEquals(
            QuestionnaireCategory(
                id = "cat1",
                name = LocalizedText("Imagem", "Image", "Imagen"),
                iconUrl = null,
                order = 2,
            ),
            activeDto.toDomainOrNull("cat1"),
        )
    }

    @Test
    fun `keeps the icon url when there is one`() {
        val dto = activeDto.copy(icon = ImageRefDto(path = "p", url = "https://x/icon.png"))

        assertEquals("https://x/icon.png", dto.toDomainOrNull("cat1")?.iconUrl)
    }

    @Test
    fun `inactive or unknown status is not shown`() {
        assertNull(activeDto.copy(status = "inactive").toDomainOrNull("cat1"))
        assertNull(activeDto.copy(status = "archived").toDomainOrNull("cat1"))
        assertNull(activeDto.copy(status = "").toDomainOrNull("cat1"))
    }

    @Test
    fun `category without a portuguese name is not shown`() {
        assertNull(activeDto.copy(name = null).toDomainOrNull("cat1"))
        assertNull(activeDto.copy(name = LocalizedTextDto(pt = " ")).toDomainOrNull("cat1"))
    }

    @Test
    fun `out of range order is clamped instead of overflowing`() {
        val dto = activeDto.copy(order = Long.MAX_VALUE)

        assertEquals(Int.MAX_VALUE, dto.toDomainOrNull("cat1")?.order)
    }
}
