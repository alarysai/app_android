package com.alarysai.alarysai.feature.home.data.remote

import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.feature.home.data.model.QuestionnaireCategoryDto
import kotlinx.coroutines.flow.Flow

interface QuestionnaireCategoryRemoteDataSource {
    fun observeActiveCategories(): Flow<RemoteDocumentList<QuestionnaireCategoryDto>>
}
