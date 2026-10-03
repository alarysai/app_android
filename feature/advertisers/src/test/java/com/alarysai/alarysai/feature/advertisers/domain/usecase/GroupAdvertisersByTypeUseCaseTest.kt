package com.alarysai.alarysai.feature.advertisers.domain.usecase

import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupAdvertisersByTypeUseCaseTest {

    private val group = GroupAdvertisersByTypeUseCase()

    private fun advertiser(id: String, type: String) = Advertiser(id, "Anunciante $id", null, type, "https://x/$id", 0)

    @Test
    fun `same type ignoring case, accents and spaces is one group with the first spelling`() {
        val groups = group(
            listOf(advertiser("a", "Patrocínio"), advertiser("b", "patrocinio "), advertiser("c", "PATROCINIO")),
        )

        assertEquals(1, groups.size)
        assertEquals("Patrocínio", groups.single().type)
        assertEquals(listOf("a", "b", "c"), groups.single().advertisers.map { it.id })
    }

    @Test
    fun `groups are sorted alphabetically in portuguese and keep the incoming order inside`() {
        val groups = group(
            listOf(
                advertiser("1", "Patrocínio"),
                advertiser("2", "Banner"),
                advertiser("3", "Ética"),
                advertiser("4", "banner"),
                advertiser("5", "Escola"),
            ),
        )

        assertEquals(listOf("Banner", "Escola", "Ética", "Patrocínio"), groups.map { it.type })
        assertEquals(listOf("2", "4"), groups.first().advertisers.map { it.id })
    }

    @Test
    fun `advertisers without a type come last in their own group`() {
        val groups = group(listOf(advertiser("x", ""), advertiser("y", "Parceiro")))

        assertEquals(listOf("Parceiro", null), groups.map { it.type })
        assertEquals(listOf("x"), groups.last().advertisers.map { it.id })
    }

    @Test
    fun `no advertisers means no groups`() {
        assertEquals(emptyList<Any>(), group(emptyList()))
    }
}
