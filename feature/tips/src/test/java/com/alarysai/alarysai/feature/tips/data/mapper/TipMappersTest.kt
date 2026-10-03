package com.alarysai.alarysai.feature.tips.data.mapper

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.tips.data.model.TipCategoryDto
import com.alarysai.alarysai.feature.tips.data.model.TipDto
import com.alarysai.alarysai.feature.tips.domain.model.Tip
import com.alarysai.alarysai.feature.tips.domain.model.TipCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TipMappersTest {

    private val categoryDto = TipCategoryDto(name = LocalizedTextDto(pt = "Ética", en = "Ethics", es = "Ética"), order = 1, status = "active")

    private val tipDto = TipDto(
        categoryId = "etica",
        text = LocalizedTextDto(pt = "Sempre cite as fontes que a IA usou.", en = "Always cite the sources."),
        image = null,
        languages = listOf("pt", "en"),
        order = 2,
        status = "active",
    )

    @Test
    fun `maps an active category`() {
        assertEquals(TipCategory("etica", LocalizedText("Ética", "Ethics", "Ética"), 1), categoryDto.toDomainOrNull("etica"))
    }

    @Test
    fun `inactive category or one without portuguese name is not shown`() {
        assertNull(categoryDto.copy(status = "inactive").toDomainOrNull("etica"))
        assertNull(categoryDto.copy(name = LocalizedTextDto(pt = "")).toDomainOrNull("etica"))
    }

    @Test
    fun `maps an active tip`() {
        assertEquals(
            Tip(
                id = "t1",
                categoryId = "etica",
                text = LocalizedText("Sempre cite as fontes que a IA usou.", "Always cite the sources.", null),
                imageUrl = null,
                languages = setOf(Language.PT, Language.EN),
                order = 2,
            ),
            tipDto.toDomainOrNull("t1"),
        )
    }

    @Test
    fun `inactive tip, tip without category or without portuguese text is not shown`() {
        assertNull(tipDto.copy(status = "inactive").toDomainOrNull("t1"))
        assertNull(tipDto.copy(categoryId = " ").toDomainOrNull("t1"))
        assertNull(tipDto.copy(text = null).toDomainOrNull("t1"))
    }

    @Test
    fun `keeps the image and ignores unknown languages`() {
        val tip = tipDto.copy(image = ImageRefDto(path = "p", url = "https://x/t.png"), languages = listOf("fr", "es"))
            .toDomainOrNull("t1")

        assertEquals("https://x/t.png", tip?.imageUrl)
        assertEquals(setOf(Language.ES, Language.PT), tip?.languages)
    }
}
