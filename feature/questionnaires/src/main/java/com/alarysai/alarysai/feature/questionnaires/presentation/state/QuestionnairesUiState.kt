package com.alarysai.alarysai.feature.questionnaires.presentation.state

import com.alarysai.alarysai.core.common.content.ContentLoadError

data class QuestionnairesUiState(
    /** Title of the screen; comes from the navigation argument. */
    val categoryName: String,
    val content: QuestionnairesContent = QuestionnairesContent.Loading,
)

/** A questionnaire ready to render: texts already resolved to the user's language. */
data class QuestionnaireItemUi(
    val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    /** False when the questionnaire is not fully translated: it shows in Portuguese, with a badge. */
    val isInUserLanguage: Boolean,
    /** Position in the list, so each card keeps a stable color. */
    val accentIndex: Int,
)

sealed interface QuestionnairesContent {
    data object Loading : QuestionnairesContent

    /** [isOffline]: the list came from the offline cache; show a discreet notice. */
    data class Success(
        val questionnaires: List<QuestionnaireItemUi>,
        val isOffline: Boolean,
    ) : QuestionnairesContent

    data class Empty(val isOffline: Boolean) : QuestionnairesContent

    data class Error(val error: ContentLoadError) : QuestionnairesContent
}
