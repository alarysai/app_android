package com.alarysai.alarysai.core.common.language

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizedTextTest {

    private val fullyTranslated = LocalizedText(pt = "Imagem", en = "Image", es = "Imagen")
    private val portugueseOnly = LocalizedText(pt = "Ética na IA")

    @Test
    fun `resolves the requested language when translated`() {
        assertEquals("Imagem", fullyTranslated.resolve(Language.PT))
        assertEquals("Image", fullyTranslated.resolve(Language.EN))
        assertEquals("Imagen", fullyTranslated.resolve(Language.ES))
    }

    @Test
    fun `falls back to portuguese when the translation is missing`() {
        assertEquals("Ética na IA", portugueseOnly.resolve(Language.EN))
        assertEquals("Ética na IA", portugueseOnly.resolve(Language.ES))
    }
}
