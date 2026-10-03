package com.alarysai.alarysai.feature.questionnaires.domain.usecase

import com.alarysai.alarysai.feature.questionnaires.domain.model.PromptPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuildPromptPreviewUseCaseTest {

    private val preview = BuildPromptPreviewUseCase()

    @Test
    fun `one line per part, instructions then the answer`() {
        val parts = listOf(
            PromptPart("formato", "Post para redes sociais", listOf("Crie uma imagem quadrada.")),
            PromptPart("cena", "Uma barista sorrindo", listOf("Cena:")),
        )

        assertEquals("Crie uma imagem quadrada: Post para redes sociais\nCena: Uma barista sorrindo", preview(parts))
    }

    @Test
    fun `parts with only an answer or only instructions keep what they have`() {
        val parts = listOf(PromptPart("a", "Fotorrealista", emptyList()), PromptPart("v", null, listOf("Considere o vídeo.")))

        assertEquals("Fotorrealista\nConsidere o vídeo.", preview(parts))
    }

    @Test
    fun `nothing in the prompt means no preview`() {
        assertNull(preview(emptyList()))
        assertNull(preview(listOf(PromptPart("x", null, emptyList()))))
    }
}
