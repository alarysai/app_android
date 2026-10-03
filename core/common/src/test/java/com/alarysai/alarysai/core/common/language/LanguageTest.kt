package com.alarysai.alarysai.core.common.language

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageTest {

    @Test
    fun `known codes resolve to their language`() {
        assertEquals(Language.PT, Language.fromCode("pt"))
        assertEquals(Language.EN, Language.fromCode("en"))
        assertEquals(Language.ES, Language.fromCode("es"))
    }

    @Test
    fun `regional and upper case codes use the primary language`() {
        assertEquals(Language.EN, Language.fromCode("en-US"))
        assertEquals(Language.PT, Language.fromCode("pt_BR"))
        assertEquals(Language.ES, Language.fromCode(" ES "))
    }

    @Test
    fun `unknown, blank or missing codes fall back to portuguese`() {
        assertEquals(Language.PT, Language.fromCode("fr"))
        assertEquals(Language.PT, Language.fromCode(""))
        assertEquals(Language.PT, Language.fromCode(null))
    }

    @Test
    fun `strict parsing returns null for unknown codes`() {
        assertEquals(Language.EN, Language.fromCodeOrNull("en"))
        assertNull(Language.fromCodeOrNull("fr"))
        assertNull(Language.fromCodeOrNull(null))
    }
}
