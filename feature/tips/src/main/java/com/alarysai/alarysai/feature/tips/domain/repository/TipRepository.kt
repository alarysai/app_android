package com.alarysai.alarysai.feature.tips.domain.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.feature.tips.domain.model.Tip
import com.alarysai.alarysai.feature.tips.domain.model.TipCategory
import kotlinx.coroutines.flow.Flow

interface TipRepository {

    /** Active tip categories by `order` (ties by ID). Fails with `ContentLoadException`. */
    fun observeActiveCategories(): Flow<ContentList<TipCategory>>

    /**
     * Active tips by `order` (ties by ID): of one category, or all of them when [categoryId] is null.
     * Fails with `ContentLoadException`.
     */
    fun observeActiveTips(categoryId: String?): Flow<ContentList<Tip>>
}
