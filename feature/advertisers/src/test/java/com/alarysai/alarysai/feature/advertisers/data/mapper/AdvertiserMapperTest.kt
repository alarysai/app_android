package com.alarysai.alarysai.feature.advertisers.data.mapper

import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.feature.advertisers.data.model.AdvertiserDto
import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdvertiserMapperTest {

    private val activeDto = AdvertiserDto(
        name = "Loja X",
        image = null,
        type = "Parceiro",
        link = "https://lojax.com.br/promo",
        order = 1,
        status = "active",
    )

    @Test
    fun `maps an active advertiser`() {
        assertEquals(
            Advertiser("a1", "Loja X", null, "Parceiro", "https://lojax.com.br/promo", 1),
            activeDto.toDomainOrNull("a1"),
        )
    }

    @Test
    fun `tidies the type like the panel`() {
        assertEquals("Banner topo", activeDto.copy(type = "  Banner   topo ").toDomainOrNull("a1")?.type)
    }

    @Test
    fun `inactive advertiser is not shown`() {
        assertNull(activeDto.copy(status = "inactive").toDomainOrNull("a1"))
        assertNull(activeDto.copy(status = "").toDomainOrNull("a1"))
    }

    @Test
    fun `only https links are accepted`() {
        assertNull(activeDto.copy(link = "http://lojax.com.br").toDomainOrNull("a1"))
        assertNull(activeDto.copy(link = "javascript:alert(1)").toDomainOrNull("a1"))
        assertNull(activeDto.copy(link = "").toDomainOrNull("a1"))
    }

    @Test
    fun `needs a name or an image`() {
        assertNull(activeDto.copy(name = " ", image = null).toDomainOrNull("a1"))

        val imageOnly = activeDto.copy(name = null, image = ImageRefDto(path = "p", url = "https://x/logo.png")).toDomainOrNull("a1")
        assertNull(imageOnly?.name)
        assertEquals("https://x/logo.png", imageOnly?.imageUrl)
    }
}
