package com.alarysai.alarysai.core.firebase.mapper

import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentMappersTest {

    @Test
    fun `maps every filled translation`() {
        val dto = LocalizedTextDto(pt = "Imagem", en = "Image", es = "Imagen")

        assertEquals(LocalizedText("Imagem", "Image", "Imagen"), dto.toDomainOrNull())
    }

    @Test
    fun `blank translations become missing so they fall back to portuguese`() {
        val dto = LocalizedTextDto(pt = " Imagem ", en = "  ", es = "")

        assertEquals(LocalizedText("Imagem", null, null), dto.toDomainOrNull())
    }

    @Test
    fun `blank portuguese makes the text unusable`() {
        assertNull(LocalizedTextDto(pt = "", en = "Image").toDomainOrNull())
    }

    @Test
    fun `image url is null when the reference or the url is missing`() {
        val missing: ImageRefDto? = null

        assertNull(missing.toUrlOrNull())
        assertNull(ImageRefDto(path = "content/a.png", url = " ").toUrlOrNull())
        assertEquals("https://x/a.png", ImageRefDto(url = "https://x/a.png").toUrlOrNull())
    }
}
