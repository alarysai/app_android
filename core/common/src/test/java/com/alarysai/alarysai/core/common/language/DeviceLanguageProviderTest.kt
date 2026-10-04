package com.alarysai.alarysai.core.common.language

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DeviceLanguageProviderTest {

    private val original = Locale.getDefault()

    @Before
    fun setUp() {
        Locale.setDefault(Locale.ENGLISH)
    }

    @After
    fun tearDown() {
        Locale.setDefault(original)
    }

    @Test
    fun `uses the device language when the profile has none`() {
        assertEquals(Language.EN, DeviceLanguageProvider(PreferredLanguageHolder()).currentLanguage())
    }

    @Test
    fun `the profile language wins over the device`() {
        val holder = PreferredLanguageHolder().apply { language = Language.ES }

        assertEquals(Language.ES, DeviceLanguageProvider(holder).currentLanguage())
    }
}
