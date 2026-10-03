package com.alarysai.alarysai.feature.questionnaires.data.mapper

import com.alarysai.alarysai.core.common.language.LocalizedText
import com.alarysai.alarysai.core.firebase.model.ImageRefDto
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.questionnaires.data.model.TipDto
import com.alarysai.alarysai.feature.questionnaires.domain.model.Tip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TipMapperTest {

    private val activeDto = TipDto(
        text = LocalizedTextDto(pt = "Sempre cite as fontes que a IA usou.", en = "Always cite the sources."),
        image = ImageRefDto(path = "p", url = "https://x/tip.png"),
        status = "active",
    )

    @Test
    fun `maps an active tip`() {
        assertEquals(
            Tip("tip1", LocalizedText("Sempre cite as fontes que a IA usou.", "Always cite the sources.", null), "https://x/tip.png"),
            activeDto.toDomainOrNull("tip1"),
        )
    }

    @Test
    fun `inactive or unknown status is not shown`() {
        assertNull(activeDto.copy(status = "inactive").toDomainOrNull("tip1"))
        assertNull(activeDto.copy(status = "").toDomainOrNull("tip1"))
    }

    @Test
    fun `tip without a portuguese text is not shown`() {
        assertNull(activeDto.copy(text = null).toDomainOrNull("tip1"))
        assertNull(activeDto.copy(text = LocalizedTextDto(pt = "")).toDomainOrNull("tip1"))
    }
}
