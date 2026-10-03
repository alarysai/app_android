package com.alarysai.alarysai.feature.questionnaires.presentation.action

import com.alarysai.alarysai.feature.questionnaires.presentation.state.QuestionnaireItemUi

sealed interface QuestionnairesUiAction {
    data object Retry : QuestionnairesUiAction
    data class QuestionnaireClicked(val questionnaire: QuestionnaireItemUi) : QuestionnairesUiAction
}
