package com.alarysai.alarysai.feature.home.data.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.core.common.content.ContentLoadException
import com.alarysai.alarysai.core.firebase.error.toContentLoadError
import com.alarysai.alarysai.feature.home.data.mapper.toDomainOrNull
import com.alarysai.alarysai.feature.home.data.remote.QuestionnaireCategoryRemoteDataSource
import com.alarysai.alarysai.feature.home.domain.model.QuestionnaireCategory
import com.alarysai.alarysai.feature.home.domain.repository.QuestionnaireCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestionnaireCategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuestionnaireCategoryRemoteDataSource,
) : QuestionnaireCategoryRepository {

    override fun observeActiveCategories(): Flow<ContentList<QuestionnaireCategory>> =
        remoteDataSource.observeActiveCategories()
            .map { remote ->
                ContentList(
                    items = remote.documents
                        .mapNotNull { it.data.toDomainOrNull(it.id) }
                        .sortedWith(compareBy<QuestionnaireCategory> { it.order }.thenBy { it.id }),
                    isFromCache = remote.isFromCache,
                )
            }
            .catch { throw ContentLoadException(it.toContentLoadError(), it) }
}
