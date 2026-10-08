package com.alarysai.alarysai.feature.tips.presentation.components

/**
 * Endless carousel over [size] items: the pager gets a very large page count and starts in the
 * middle, so the user can swipe either way for as long as they want; each page shows
 * `items[tipIndex(page)]`. With one item (or none) there is nothing to swipe to.
 */
internal class CarouselPaging(val size: Int) {

    val isEndless: Boolean get() = size > 1

    val pageCount: Int get() = if (isEndless) ENDLESS_PAGE_COUNT else size

    /** Middle page that shows the first item. */
    val initialPage: Int get() = if (isEndless) MIDDLE - MIDDLE % size else 0

    fun tipIndex(page: Int): Int = if (size == 0) 0 else page.mod(size)

    private companion object {
        const val ENDLESS_PAGE_COUNT = Int.MAX_VALUE
        const val MIDDLE = ENDLESS_PAGE_COUNT / 2
    }
}
