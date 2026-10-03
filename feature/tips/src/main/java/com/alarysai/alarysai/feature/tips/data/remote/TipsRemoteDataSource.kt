package com.alarysai.alarysai.feature.tips.data.remote

import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.feature.tips.data.model.TipCategoryDto
import com.alarysai.alarysai.feature.tips.data.model.TipDto
import kotlinx.coroutines.flow.Flow

interface TipsRemoteDataSource {
    fun observeActiveCategories(): Flow<RemoteDocumentList<TipCategoryDto>>

    /** All active tips when [categoryId] is null. */
    fun observeActiveTips(categoryId: String?): Flow<RemoteDocumentList<TipDto>>
}
