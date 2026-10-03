package com.alarysai.alarysai.core.common.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTextTest {

    @Test
    fun `normalization removes accents, case and extra spaces`() {
        assertEquals("video ia", "  Vídeo   IA ".normalizedForSearch())
        assertEquals("apresentacao", "APRESENTAÇÃO".normalizedForSearch())
    }

    @Test
    fun `matches ignoring case and accents`() {
        assertTrue("Vídeo".matchesSearch("vid"))
        assertTrue("Video".matchesSearch("VÍDEO"))
        assertTrue("Recursos avançados".matchesSearch("avancados"))
    }

    @Test
    fun `blank query matches everything`() {
        assertTrue("Imagem".matchesSearch(""))
        assertTrue("Imagem".matchesSearch("   "))
    }

    @Test
    fun `different text does not match`() {
        assertFalse("Imagem".matchesSearch("texto"))
    }
}
