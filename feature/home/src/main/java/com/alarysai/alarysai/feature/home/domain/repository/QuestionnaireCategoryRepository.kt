package com.alarysai.alarysai.feature.home.domain.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.feature.home.domain.model.QuestionnaireCategory
import kotlinx.coroutines.flow.Flow

interface QuestionnaireCategoryRepository {

    /**
     * Active categories sorted by `order` (ties broken by ID, like the admin panel).
     * Emits again whenever the panel changes them. Fails with `ContentLoadException`.
     */
    fun observeActiveCategories(): Flow<ContentList<QuestionnaireCategory>>
}
