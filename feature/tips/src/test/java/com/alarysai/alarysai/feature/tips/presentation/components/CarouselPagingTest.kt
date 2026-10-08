package com.alarysai.alarysai.feature.tips.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarouselPagingTest {

    @Test
    fun `several tips are endless and start in the middle on the first tip`() {
        val paging = CarouselPaging(size = 3)

        assertTrue(paging.isEndless)
        assertEquals(Int.MAX_VALUE, paging.pageCount)
        assertEquals(0, paging.tipIndex(paging.initialPage))
        assertTrue(paging.initialPage > 1_000_000)
    }

    @Test
    fun `pages wrap around in both directions`() {
        val paging = CarouselPaging(size = 3)
        val start = paging.initialPage

        assertEquals(listOf(0, 1, 2, 0, 1), (start..start + 4).map(paging::tipIndex))
        assertEquals(listOf(2, 1, 0), listOf(start - 1, start - 2, start - 3).map(paging::tipIndex))
    }

    @Test
    fun `one tip is a single still page`() {
        val paging = CarouselPaging(size = 1)

        assertFalse(paging.isEndless)
        assertEquals(1, paging.pageCount)
        assertEquals(0, paging.initialPage)
        assertEquals(0, paging.tipIndex(0))
    }

    @Test
    fun `no tips means no pages`() {
        val paging = CarouselPaging(size = 0)

        assertEquals(0, paging.pageCount)
        assertEquals(0, paging.tipIndex(5))
    }
}
